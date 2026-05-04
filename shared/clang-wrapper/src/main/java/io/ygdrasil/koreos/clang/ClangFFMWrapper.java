// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Low-level wrapper for libclang using Java 21+ FFM API.
 * Part of GRA-2: Basic implementation of libclang wrapper with FFM.
 */
public final class ClangFFMWrapper {

    private static volatile boolean initialized = false;
    private static MemorySegment clangLib;
    private static Linker linker;
    private static Arena libArena; // Keep arena alive for library lookup

    // Method handles for libclang functions
    private static MethodHandle clang_createIndex;
    private static MethodHandle clang_parseTranslationUnit;
    private static MethodHandle clang_disposeIndex;
    private static MethodHandle clang_disposeTranslationUnit;
    private static MethodHandle clang_getNumDiagnostics;
    private static MethodHandle clang_getDiagnostic;

    // Function descriptors for libclang functions
    // Note: On macOS with Apple Clang, parameter types may differ from standard LLVM Clang
    private static final FunctionDescriptor CREATE_INDEX_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXIndex*
        ValueLayout.JAVA_INT, // excludeDeclsFromPCH (int, treated as bool)
        ValueLayout.JAVA_INT  // displayDiagnostics (int, treated as bool)
    );

    private static final FunctionDescriptor PARSE_TU_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXTranslationUnit*
        ValueLayout.ADDRESS, // CXIndex*
        ValueLayout.ADDRESS, // source_filename (const char*)
        ValueLayout.ADDRESS, // command_line_args (const char* const*)
        ValueLayout.JAVA_INT, // num_command_line_args (int)
        ValueLayout.ADDRESS, // unsaved_files (CXUnsavedFile*)
        ValueLayout.JAVA_INT, // num_unsaved_files (unsigned)
        ValueLayout.JAVA_INT  // options (unsigned)
    );

    private static final FunctionDescriptor DISPOSE_INDEX_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS // CXIndex*
    );

    private static final FunctionDescriptor DISPOSE_TU_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS // CXTranslationUnit*
    );

    private static final FunctionDescriptor GET_NUM_DIAG_DESC = FunctionDescriptor.of(
        ValueLayout.JAVA_INT, // unsigned int
        ValueLayout.ADDRESS  // CXTranslationUnit*
    );

    // Prevent instantiation
    private ClangFFMWrapper() {}

    /**
     * Check if FFM is supported (Java 21+).
     */
    public static boolean isFFMSupported() {
        return Runtime.version().feature() >= 21;
    }

    /**
     * Initialize the FFM bindings for libclang.
     * Thread-safe and idempotent.
     * @throws ClangInitializationException if initialization fails
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        if (!isFFMSupported()) {
            throw new ClangInitializationException(
                "FFM requires Java 21+. Current version: " + Runtime.version());
        }

        try {
            linker = Linker.nativeLinker();
            libArena = Arena.ofAuto();
            SymbolLookup loader = linker.defaultLookup();

            // Try multiple library names for cross-platform support
            String[] libNames = {
                // Linux (versioned)
                "libclang.so.17", "libclang.so.18", "libclang.so.1", 
                // Linux (unversioned)
                "libclang.so",
                // macOS
                "libclang.dylib",
                // Windows
                "clang.dll", "libclang.dll"
            };
            
            for (String libName : libNames) {
                try {
                    clangLib = loader.find(libName).orElse(null);
                    if (clangLib != null) {
                        break;
                    }
                } catch (Exception e) {
                    // Try next library name
                }
            }

            // If not found, try loading from known paths
            if (clangLib == null) {
                String[] knownPaths = {
                    // Homebrew (macOS)
                    "/opt/homebrew/opt/llvm/lib/libclang.dylib",
                    // Apple Clang
                    "/Library/Developer/CommandLineTools/usr/lib/libclang.dylib",
                    // Ubuntu/Debian LLVM 17
                    "/usr/lib/llvm-17/lib/libclang.so.17",
                    "/usr/lib/llvm-17/lib/libclang.so.1",
                    "/usr/lib/llvm-17/lib/libclang.so",
                    // System-wide
                    "/usr/lib/libclang.so.17",
                    "/usr/lib/libclang.so.1",
                    "/usr/lib/libclang.so",
                    // Windows LLVM
                    "C:\\Program Files\\LLVM\\lib\\libclang.dll"
                };
                for (String path : knownPaths) {
                    try {
                        if (Files.exists(Path.of(path))) {
                            // Load the library and get its symbol lookup
                            SymbolLookup libLookup = SymbolLookup.libraryLookup(Path.of(path), libArena);
                            // Check that all required symbols exist in this library
                            if (libLookup.find("clang_createIndex").isPresent() &&
                                libLookup.find("clang_parseTranslationUnit").isPresent() &&
                                libLookup.find("clang_disposeIndex").isPresent() &&
                                libLookup.find("clang_disposeTranslationUnit").isPresent()) {
                                clangLib = libLookup.find("clang_createIndex").orElse(null);
                                // Switch to using this library's lookup for all symbols
                                loader = libLookup;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // Try next path
                    }
                }
            }

            if (clangLib == null) {
                throw new ClangInitializationException(
                    "Failed to load libclang library. " +
                    "Ensure LLVM/Clang 17+ is installed. " +
                    "Tried: " + String.join(", ", libNames));
            }

            // Resolve required function symbols
            clang_createIndex = resolveSymbol(loader, "clang_createIndex", CREATE_INDEX_DESC);
            clang_parseTranslationUnit = resolveSymbol(loader, "clang_parseTranslationUnit", PARSE_TU_DESC);
            clang_disposeIndex = resolveSymbol(loader, "clang_disposeIndex", DISPOSE_INDEX_DESC);
            clang_disposeTranslationUnit = resolveSymbol(loader, "clang_disposeTranslationUnit", DISPOSE_TU_DESC);
            
            // Optional: diagnostics
            try {
                clang_getNumDiagnostics = resolveSymbol(loader, "clang_getNumDiagnostics", GET_NUM_DIAG_DESC);
            } catch (ClangInitializationException e) {
                clang_getNumDiagnostics = null;
            }
            try {
                // clang_getDiagnostic has a complex signature, skip for now
                clang_getDiagnostic = null;
            } catch (Exception e) {
                clang_getDiagnostic = null;
            }

            initialized = true;
            System.out.println("ClangFFMWrapper initialized successfully with libclang");
            
        } catch (Exception e) {
            throw new ClangInitializationException("Failed to initialize Clang FFM bindings", e);
        }
    }

    /**
     * Resolve a symbol to a MethodHandle with the given descriptor.
     * Tries both with and without leading underscore prefix.
     */
    private static MethodHandle resolveSymbol(SymbolLookup loader, String symbolName, FunctionDescriptor desc) {
        // Try without underscore first (standard)
        MemorySegment symbol = loader.find(symbolName).orElse(null);
        if (symbol != null) {
            try {
                return linker.downcallHandle(symbol, desc);
            } catch (Exception e) {
                // Try with underscore prefix
            }
        }
        // Try with underscore prefix (common on macOS/Unix)
        symbol = loader.find("_" + symbolName).orElse(null);
        if (symbol != null) {
            try {
                return linker.downcallHandle(symbol, desc);
            } catch (Exception e) {
                // Could not create downcall handle
            }
        }
        throw new ClangInitializationException("Symbol not found: " + symbolName + " or _" + symbolName);
    }

    /**
     * Validate that a MemorySegment is not NULL.
     */
    private static MemorySegment validateNotNull(MemorySegment segment, String operationName) {
        if (segment == null || segment.equals(MemorySegment.NULL)) {
            throw new ClangMemoryException(
                operationName + " returned NULL pointer. Check libclang version and parameters.");
        }
        return segment;
    }

    /**
     * Create a Clang index.
     * @param excludeDeclsFromPCH Whether to exclude declarations from precompiled headers
     * @param displayDiagnostics Whether to display diagnostics
     * @return MemorySegment pointing to the CXIndex (must be disposed with disposeIndex)
     */
    public static MemorySegment createIndex(boolean excludeDeclsFromPCH, boolean displayDiagnostics) {
        ensureInitialized();

        try {
            // clang_createIndex takes int parameters directly (not pointers)
            int pExclude = excludeDeclsFromPCH ? 1 : 0;
            int pDisplay = displayDiagnostics ? 1 : 0;

            MemorySegment index = (MemorySegment) clang_createIndex.invoke(pExclude, pDisplay);
            return validateNotNull(index, "clang_createIndex");
        } catch (Throwable t) {
            throw new ClangMemoryException("Failed to create Clang index", t);
        }
    }

    /**
     * Parse a translation unit (source file).
     * @param index The Clang index (from createIndex)
     * @param sourceFilePath Path to the source file
     * @param commandLineArgs Command line arguments for the parser
     * @return MemorySegment pointing to the CXTranslationUnit (must be disposed with disposeTranslationUnit)
     */
    public static MemorySegment parseTranslationUnit(
            MemorySegment index, String sourceFilePath, String[] commandLineArgs) {
        ensureInitialized();
        validateNotNull(index, "parseTranslationUnit - index parameter");

        if (sourceFilePath == null || sourceFilePath.isEmpty()) {
            throw new ClangParsingException("Source file path cannot be null or empty");
        }

        try (Arena arena = Arena.ofConfined()) {
            // Allocate array of pointers for command line args
            MemorySegment argsArray = arena.allocate(ValueLayout.ADDRESS, commandLineArgs.length);
            for (int i = 0; i < commandLineArgs.length; i++) {
                if (commandLineArgs[i] != null) {
                    // Allocate memory for the string (+1 for null terminator)
                    MemorySegment arg = arena.allocate(commandLineArgs[i].length() + 1);
                    arg.setString(0, commandLineArgs[i], StandardCharsets.UTF_8);
                    argsArray.setAtIndex(ValueLayout.ADDRESS, i, arg);
                }
            }

            // Allocate memory for source file path
            MemorySegment filePath = arena.allocate(sourceFilePath.length() + 1);
            filePath.setString(0, sourceFilePath, StandardCharsets.UTF_8);

            // clang_parseTranslationUnit signature:
            // CXTranslationUnit clang_parseTranslationUnit(
            //   CXIndex CIdx, const char *source_filename,
            //   const char *const *command_line_args, int num_command_line_args,
            //   struct CXUnsavedFile *unsaved_files, unsigned num_unsaved_files,
            //   unsigned options);
            
            MemorySegment translationUnit = (MemorySegment) clang_parseTranslationUnit.invoke(
                index, filePath, argsArray, commandLineArgs.length, MemorySegment.NULL, 0, 0);
            
            return validateNotNull(translationUnit, "clang_parseTranslationUnit");
        } catch (Throwable t) {
            throw new ClangParsingException("Failed to parse translation unit: " + sourceFilePath, t);
        }
    }

    /**
     * Dispose a Clang index.
     */
    public static void disposeIndex(MemorySegment index) {
        if (index == null || index.equals(MemorySegment.NULL)) {
            return;
        }
        ensureInitialized();
        try {
            clang_disposeIndex.invoke(index);
        } catch (Throwable t) {
            throw new ClangMemoryException("Failed to dispose Clang index", t);
        }
    }

    /**
     * Dispose a translation unit.
     */
    public static void disposeTranslationUnit(MemorySegment translationUnit) {
        if (translationUnit == null || translationUnit.equals(MemorySegment.NULL)) {
            return;
        }
        ensureInitialized();
        try {
            clang_disposeTranslationUnit.invoke(translationUnit);
        } catch (Throwable t) {
            throw new ClangMemoryException("Failed to dispose translation unit", t);
        }
    }

    /**
     * Get the number of diagnostics for a translation unit.
     */
    public static int getNumDiagnostics(MemorySegment translationUnit) {
        ensureInitialized();

        // Return -1 for null without throwing (for backward compatibility)
        if (translationUnit == null || translationUnit.equals(MemorySegment.NULL)) {
            return -1;
        }
        
        if (clang_getNumDiagnostics == null) {
            return -1;
        }

        try {
            return (int) clang_getNumDiagnostics.invoke(translationUnit);
        } catch (Throwable t) {
            return -1;
        }
    }

    static void ensureInitialized() {
        if (!initialized) {
            throw new ClangInitializationException(
                "ClangFFMWrapper not initialized. Call initialize() first.");
        }
    }

    /**
     * Check if the wrapper is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Reset the wrapper state (for testing purposes).
     */
    static void reset() {
        initialized = false;
        clangLib = null;
        linker = null;
        clang_createIndex = null;
        clang_parseTranslationUnit = null;
        clang_disposeIndex = null;
        clang_disposeTranslationUnit = null;
        clang_getNumDiagnostics = null;
        clang_getDiagnostic = null;
    }
}
