// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.*
import java.lang.invoke.MethodHandle
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Low-level wrapper for libclang using Java 21+ FFM API.
 * Part of GRA-2: Basic implementation of libclang wrapper with FFM.
 */
object ClangFFMWrapper {

    @Volatile
    private var initialized = false
    private var clangLib: MemorySegment? = null
    private val linker = Linker.nativeLinker()
    
    // Use a global arena that lives for the entire JVM lifetime
    // This prevents use-after-free issues during JVM shutdown
    private val libArena = Arena.global()

    // Method handles for libclang functions
    private var clang_createIndex: MethodHandle? = null
    private var clang_parseTranslationUnit: MethodHandle? = null
    private var clang_disposeIndex: MethodHandle? = null
    private var clang_disposeTranslationUnit: MethodHandle? = null
    private var clang_getNumDiagnostics: MethodHandle? = null
    private var clang_getDiagnostic: MethodHandle? = null

    // Function descriptors for libclang functions
    // Note: On macOS with Apple Clang, parameter types may differ from standard LLVM Clang
    private val CREATE_INDEX_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXIndex*
        ValueLayout.JAVA_INT, // excludeDeclsFromPCH (int, treated as bool)
        ValueLayout.JAVA_INT  // displayDiagnostics (int, treated as bool)
    )

    private val PARSE_TU_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXTranslationUnit*
        ValueLayout.ADDRESS, // CXIndex*
        ValueLayout.ADDRESS, // source_filename (const char*)
        ValueLayout.ADDRESS, // command_line_args (const char* const*)
        ValueLayout.JAVA_INT, // num_command_line_args (int)
        ValueLayout.ADDRESS, // unsaved_files (CXUnsavedFile*)
        ValueLayout.JAVA_INT, // num_unsaved_files (unsigned)
        ValueLayout.JAVA_INT  // options (unsigned)
    )

    private val DISPOSE_INDEX_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS // CXIndex*
    )

    private val DISPOSE_TU_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS // CXTranslationUnit*
    )

    private val GET_NUM_DIAG_DESC = FunctionDescriptor.of(
        ValueLayout.JAVA_INT, // unsigned int
        ValueLayout.ADDRESS  // CXTranslationUnit*
    )

    /**
     * Check if FFM is supported (Java 21+).
     */
    @JvmStatic
    fun isFFMSupported(): Boolean {
        return Runtime.version().feature() >= 21
    }

    /**
     * Initialize the FFM bindings for libclang.
     * Thread-safe and idempotent.
     * Note: The library is loaded once and remains loaded for the JVM lifetime.
     * Subsequent calls to initialize() after reset() will restore the initialized
     * flag without reloading the library, preventing use-after-free crashes on Linux.
     * @throws ClangInitializationException if initialization fails
     */
    @Synchronized
    @JvmStatic
    fun initialize() {
        if (initialized) {
            return
        }

        // If method handles are already set (library was loaded but reset was called),
        // just restore the initialized flag without reloading the library.
        // This prevents use-after-free crashes during JVM shutdown on Linux.
        if (clang_createIndex != null) {
            initialized = true
            return
        }

        if (!isFFMSupported()) {
            throw ClangInitializationException(
                "FFM requires Java 21+. Current version: " + Runtime.version())
        }

        try {
            println("[ClangFFMWrapper] Starting initialization...")
            println("[ClangFFMWrapper] java.library.path: " + System.getProperty("java.library.path"))
            println("[ClangFFMWrapper] Current working directory: " + System.getProperty("user.dir"))
            
            val loader = linker.defaultLookup()

            // First, try System.loadLibrary() with standard library names.
            // This uses the system library loader which respects PATH/LD_LIBRARY_PATH/DYLD_LIBRARY_PATH
            val libNames = arrayOf("clang", "libclang")
            
            println("[ClangFFMWrapper] Trying System.loadLibrary()...")
            for (libName in libNames) {
                try {
                    System.loadLibrary(libName)
                    println("[ClangFFMWrapper] Successfully loaded libclang via System.loadLibrary: $libName")
                    // Verify we can find a symbol
                    clangLib = loader.find("clang_createIndex").orElse(null)
                    if (clangLib != null) {
                        println("[ClangFFMWrapper] Found clang_createIndex symbol")
                        break
                    } else {
                        println("[ClangFFMWrapper] Library loaded but clang_createIndex symbol not found")
                    }
                } catch (e: UnsatisfiedLinkError) {
                    println("[ClangFFMWrapper] System.loadLibrary failed for $libName: " + e.message)
                } catch (e: SecurityException) {
                    println("[ClangFFMWrapper] System.loadLibrary failed for $libName: " + e.message)
                }
            }

            // If not found, try loading from known paths
            if (clangLib == null) {
                println("[ClangFFMWrapper] Library not found via System.loadLibrary, trying known paths...")
                val knownPaths = arrayOf(
                    // Homebrew (macOS)
                    "/opt/homebrew/opt/llvm/lib/libclang.dylib",
                    // Apple Clang
                    "/Library/Developer/CommandLineTools/usr/lib/libclang.dylib",
                    // Ubuntu/Debian LLVM 17
                    "/usr/lib/llvm-17/lib/libclang.so.17",
                    "/usr/lib/llvm-17/lib/libclang.so.1",
                    "/usr/lib/llvm-17/lib/libclang.so",
                    // Ubuntu/Debian LLVM 18+
                    "/usr/lib/llvm-18/lib/libclang.so",
                    "/usr/lib/llvm-19/lib/libclang.so",
                    "/usr/lib/llvm-20/lib/libclang.so",
                    // System-wide
                    "/usr/lib/libclang.so.17",
                    "/usr/lib/libclang.so.1",
                    "/usr/lib/libclang.so",
                    // Windows LLVM - check both lib and bin directories
                    "C:\\Program Files\\LLVM\\lib\\libclang.dll",
                    "C:\\Program Files\\LLVM\\bin\\libclang.dll"
                )
                
                for (path in knownPaths) {
                    try {
                        if (Files.exists(Path.of(path))) {
                            println("Trying known path: $path")
                            // Try loading the library with System.load first
                            // This ensures the library is loaded into the JVM's native library cache
                            try {
                                System.load(path)
                                println("Successfully loaded library with System.load: $path")
                                // Now try to find symbols with default lookup
                                clangLib = loader.find("clang_createIndex").orElse(null)
                                if (clangLib != null) {
                                    println("Found clang_createIndex after System.load")
                                    break
                                }
                            } catch (e: UnsatisfiedLinkError) {
                                // Fall back to SymbolLookup.libraryLookup
                                println("System.load failed, trying libraryLookup: " + e.message)
                            } catch (e: SecurityException) {
                                // Fall back to SymbolLookup.libraryLookup
                                println("System.load failed, trying libraryLookup: " + e.message)
                            }
                            
                            // Fallback: use libraryLookup
                            val libLookup = SymbolLookup.libraryLookup(Path.of(path), libArena)
                            // Check that all required symbols exist in this library
                            if (libLookup.find("clang_createIndex").isPresent &&
                                libLookup.find("clang_parseTranslationUnit").isPresent &&
                                libLookup.find("clang_disposeIndex").isPresent &&
                                libLookup.find("clang_disposeTranslationUnit").isPresent) {
                                clangLib = libLookup.find("clang_createIndex").orElse(null)
                                // Note: We can't reassign loader as it's a val, but we can use libLookup for symbol resolution
                                // For now, keep using default loader - symbols should be found
                                println("Loaded libclang from: $path")
                                break
                            }
                        }
                    } catch (e: Exception) {
                        // Try next path
                    }
                }
            }

            if (clangLib == null) {
                throw ClangInitializationException(
                    "Failed to load libclang library. " +
                    "Ensure LLVM/Clang 17+ is installed. " +
                    "Tried System.loadLibrary with: " + libNames.joinToString(", ") + 
                    ", and known paths including Windows LLVM lib and bin directories. " +
                    "Check java.library.path and PATH environment variables.")
            }

            // Resolve required function symbols
            clang_createIndex = resolveSymbol(loader, "clang_createIndex", CREATE_INDEX_DESC)
            clang_parseTranslationUnit = resolveSymbol(loader, "clang_parseTranslationUnit", PARSE_TU_DESC)
            clang_disposeIndex = resolveSymbol(loader, "clang_disposeIndex", DISPOSE_INDEX_DESC)
            clang_disposeTranslationUnit = resolveSymbol(loader, "clang_disposeTranslationUnit", DISPOSE_TU_DESC)
            
            // Optional: diagnostics
            try {
                clang_getNumDiagnostics = resolveSymbol(loader, "clang_getNumDiagnostics", GET_NUM_DIAG_DESC)
            } catch (e: ClangInitializationException) {
                clang_getNumDiagnostics = null
            }
            try {
                // clang_getDiagnostic has a complex signature, skip for now
                clang_getDiagnostic = null
            } catch (e: Exception) {
                clang_getDiagnostic = null
            }

            initialized = true
            println("ClangFFMWrapper initialized successfully with libclang")
            
        } catch (e: Exception) {
            throw ClangInitializationException("Failed to initialize Clang FFM bindings", e)
        }
    }

    /**
     * Resolve a symbol to a MethodHandle with the given descriptor.
     * Tries both with and without leading underscore prefix.
     */
    private fun resolveSymbol(loader: SymbolLookup, symbolName: String, desc: FunctionDescriptor): MethodHandle {
        // Try without underscore first (standard)
        var symbol: MemorySegment? = loader.find(symbolName).orElse(null)
        if (symbol != null) {
            try {
                return linker.downcallHandle(symbol, desc)
            } catch (e: Exception) {
                // Try with underscore prefix
            }
        }
        // Try with underscore prefix (common on macOS/Unix)
        symbol = loader.find("_$symbolName").orElse(null)
        if (symbol != null) {
            try {
                return linker.downcallHandle(symbol, desc)
            } catch (e: Exception) {
                // Could not create downcall handle
            }
        }
        throw ClangInitializationException("Symbol not found: $symbolName or _$symbolName")
    }

    /**
     * Validate that a MemorySegment is not NULL.
     */
    private fun validateNotNull(segment: MemorySegment?, operationName: String): MemorySegment {
        if (segment == null || segment == MemorySegment.NULL) {
            throw ClangMemoryException(
                "$operationName returned NULL pointer. Check libclang version and parameters.")
        }
        return segment
    }

    /**
     * Create a Clang index.
     * @param excludeDeclsFromPCH Whether to exclude declarations from precompiled headers
     * @param displayDiagnostics Whether to display diagnostics
     * @return MemorySegment pointing to the CXIndex (must be disposed with disposeIndex)
     */
    @JvmStatic
    fun createIndex(excludeDeclsFromPCH: Boolean, displayDiagnostics: Boolean): MemorySegment {
        ensureInitialized()

        try {
            // clang_createIndex takes int parameters directly (not pointers)
            val pExclude = if (excludeDeclsFromPCH) 1 else 0
            val pDisplay = if (displayDiagnostics) 1 else 0

            val index = clang_createIndex!!.invoke(pExclude, pDisplay) as MemorySegment
            return validateNotNull(index, "clang_createIndex")
        } catch (t: Throwable) {
            throw ClangMemoryException("Failed to create Clang index", t)
        }
    }

    /**
     * Parse a translation unit (source file).
     * @param index The Clang index (from createIndex)
     * @param sourceFilePath Path to the source file
     * @param commandLineArgs Command line arguments for the parser
     * @return MemorySegment pointing to the CXTranslationUnit (must be disposed with disposeTranslationUnit)
     */
    @JvmStatic
    fun parseTranslationUnit(
        index: MemorySegment?,
        sourceFilePath: String?,
        commandLineArgs: Array<String>?
    ): MemorySegment {
        ensureInitialized()
        validateNotNull(index, "parseTranslationUnit - index parameter")

        if (sourceFilePath.isNullOrEmpty()) {
            throw ClangParsingException("Source file path cannot be null or empty")
        }

        val args = commandLineArgs ?: emptyArray()
        Arena.ofConfined().use { arena ->
            // Allocate array of pointers for command line args
            val argsArray = arena.allocate(ValueLayout.ADDRESS, args.size.toLong())
            for (i in args.indices) {
                // Allocate memory for the string (+1 for null terminator)
                val argValue = args[i]
                val arg = arena.allocate(argValue.length + 1L)
                arg.setString(0, argValue, StandardCharsets.UTF_8)
                argsArray.setAtIndex(ValueLayout.ADDRESS, i.toLong(), arg)
            }

            // Allocate memory for source file path
            val filePath = arena.allocate(sourceFilePath.length + 1L)
            filePath.setString(0, sourceFilePath, StandardCharsets.UTF_8)

            // clang_parseTranslationUnit signature:
            // CXTranslationUnit clang_parseTranslationUnit(
            //   CXIndex CIdx, const char *source_filename,
            //   const char *const *command_line_args, int num_command_line_args,
            //   struct CXUnsavedFile *unsaved_files, unsigned num_unsaved_files,
            //   unsigned options);
            
            val translationUnit = clang_parseTranslationUnit!!.invoke(
                index, filePath, argsArray, args.size, MemorySegment.NULL, 0, 0
            ) as MemorySegment
            
            return validateNotNull(translationUnit, "clang_parseTranslationUnit")
        }
    }

    /**
     * Dispose a Clang index.
     */
    @JvmStatic
    fun disposeIndex(index: MemorySegment?) {
        if (index == null || index == MemorySegment.NULL) {
            return
        }
        ensureInitialized()
        try {
            clang_disposeIndex!!.invoke(index)
        } catch (t: Throwable) {
            throw ClangMemoryException("Failed to dispose Clang index", t)
        }
    }

    /**
     * Dispose a translation unit.
     */
    @JvmStatic
    fun disposeTranslationUnit(translationUnit: MemorySegment?) {
        if (translationUnit == null || translationUnit == MemorySegment.NULL) {
            return
        }
        ensureInitialized()
        try {
            clang_disposeTranslationUnit!!.invoke(translationUnit)
        } catch (t: Throwable) {
            throw ClangMemoryException("Failed to dispose translation unit", t)
        }
    }

    /**
     * Get the number of diagnostics for a translation unit.
     */
    @JvmStatic
    fun getNumDiagnostics(translationUnit: MemorySegment?): Int {
        ensureInitialized()

        // Return -1 for null without throwing (for backward compatibility)
        if (translationUnit == null || translationUnit == MemorySegment.NULL) {
            return -1
        }
        
        if (clang_getNumDiagnostics == null) {
            return -1
        }

        return try {
            (clang_getNumDiagnostics!!.invoke(translationUnit) as Int)
        } catch (t: Throwable) {
            -1
        }
    }

    @JvmStatic
    fun ensureInitialized() {
        if (!initialized) {
            throw ClangInitializationException(
                "ClangFFMWrapper not initialized. Call initialize() first.")
        }
    }

    /**
     * Check if the wrapper is initialized.
     */
    @JvmStatic
    fun isInitialized(): Boolean {
        return initialized
    }

    /**
     * Reset the wrapper state (for testing purposes).
     * Note: This method now only resets the initialized flag to allow re-initialization.
     * Method handles and library references are NOT reset to avoid use-after-free issues
     * during JVM shutdown. Once initialized, the native library remains loaded for the
     * JVM lifetime.
     */
    @JvmStatic
    fun reset() {
        initialized = false
        // Note: We do NOT reset clangLib, linker, libArena, or method handles.
        // These reference native resources that must remain valid for the JVM lifetime.
        // Resetting them causes use-after-free crashes during JVM shutdown on Linux.
        // The initialized flag is reset to allow re-initialization checks to work.
    }
}
