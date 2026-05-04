// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Low-level wrapper for libclang using Java 21+ FFM API.
 * Part of GRA-3: Conversion of libclang wrapper from Java to Kotlin.
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

    // Cursor function handles (GRA-4)
    private var clang_getTranslationUnitCursor: MethodHandle? = null
    private var clang_getCursorKind: MethodHandle? = null
    private var clang_getCursorSpelling: MethodHandle? = null
    private var clang_getCursorLocation: MethodHandle? = null
    private var clang_getCursorSemanticParent: MethodHandle? = null
    private var clang_getCursorUSR: MethodHandle? = null
    private var clang_getCursorChildren: MethodHandle? = null
    private var clang_disposeCursor: MethodHandle? = null

    // Diagnostic function handles (GRA-4)
    private var clang_getDiagnosticSeverity: MethodHandle? = null
    private var clang_getDiagnosticSpelling: MethodHandle? = null
    private var clang_getDiagnosticLocation: MethodHandle? = null
    private var clang_getDiagnosticCategory: MethodHandle? = null
    private var clang_getDiagnosticOption: MethodHandle? = null

    // String function handles
    private var clang_getCString: MethodHandle? = null
    private var clang_disposeString: MethodHandle? = null

    // Source location function handles (GRA-4)
    private var clang_getFileLocation: MethodHandle? = null
    private var clang_getExpansionLocation: MethodHandle? = null

    // Function descriptors for libclang functions
    // Note: On macOS with Apple Clang, parameter types may differ from standard LLVM Clang
    private val CREATE_INDEX_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXIndex*
        ValueLayout.JAVA_INT, // excludeDeclsFromPCH (int, treated as bool)
        ValueLayout.JAVA_INT // displayDiagnostics (int, treated as bool)
    )

    private val PARSE_TU_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXTranslationUnit*
        ValueLayout.ADDRESS, // CXIndex*
        ValueLayout.ADDRESS, // source_filename (const char*)
        ValueLayout.ADDRESS, // command_line_args (const char* const*)
        ValueLayout.JAVA_INT, // num_command_line_args (int)
        ValueLayout.ADDRESS, // unsaved_files (CXUnsavedFile*)
        ValueLayout.JAVA_INT, // num_unsaved_files (unsigned)
        ValueLayout.JAVA_INT // options (unsigned)
    )

    private val DISPOSE_INDEX_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS // CXIndex*
    )

    private val DISPOSE_TU_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS // CXTranslationUnit*
    )

    private val GET_NUM_DIAG_DESC = FunctionDescriptor.of(
        ValueLayout.JAVA_INT, // unsigned int
        ValueLayout.ADDRESS // CXTranslationUnit*
    )

    // Cursor function descriptors (GRA-4)
    private val GET_TU_CURSOR_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXCursor
        ValueLayout.ADDRESS // CXTranslationUnit*
    )

    private val GET_CURSOR_KIND_DESC = FunctionDescriptor.of(
        ValueLayout.JAVA_INT, // CXCursorKind (int)
        ValueLayout.ADDRESS // CXCursor
    )

    private val GET_CURSOR_SPELLING_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXString
        ValueLayout.ADDRESS // CXCursor
    )

    private val GET_CURSOR_LOCATION_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXSourceLocation
        ValueLayout.ADDRESS // CXCursor
    )

    private val GET_CURSOR_SEMANTIC_PARENT_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXCursor
        ValueLayout.ADDRESS // CXCursor
    )

    private val GET_CURSOR_USR_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXString
        ValueLayout.ADDRESS // CXCursor
    )

    // clang_getCursorChildren: void clang_getCursorChildren(CXCursor, CXCursor **, unsigned int *)
    private val GET_CURSOR_CHILDREN_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS, // CXCursor parent
        ValueLayout.ADDRESS, // CXCursor ** children (output array pointer)
        ValueLayout.ADDRESS // unsigned int * num_children (output count pointer)
    )

    private val DISPOSE_CURSOR_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS // CXCursor
    )

    // Diagnostic function descriptors (GRA-4)
    private val GET_DIAG_SEVERITY_DESC = FunctionDescriptor.of(
        ValueLayout.JAVA_INT, // CXDiagnosticSeverity (int)
        ValueLayout.ADDRESS // CXDiagnostic
    )

    private val GET_DIAG_SPELLING_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXString
        ValueLayout.ADDRESS // CXDiagnostic
    )

    private val GET_DIAG_LOCATION_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXSourceLocation
        ValueLayout.ADDRESS // CXDiagnostic
    )

    private val GET_DIAG_CATEGORY_DESC = FunctionDescriptor.of(
        ValueLayout.JAVA_INT, // unsigned int
        ValueLayout.ADDRESS // CXDiagnostic
    )

    private val GET_DIAG_OPTION_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // CXString
        ValueLayout.ADDRESS // CXDiagnostic
    )

    // String function descriptors
    private val GET_CSTRING_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS, // const char*
        ValueLayout.ADDRESS // CXString
    )

    private val DISPOSE_STRING_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS // CXString
    )

    // Source location function descriptors (GRA-4)
    // clang_getFileLocation: void clang_getFileLocation(CXSourceLocation, CXFile **, unsigned int *, unsigned int *, unsigned int *)
    private val GET_FILE_LOCATION_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS, // CXSourceLocation
        ValueLayout.ADDRESS, // CXFile **
        ValueLayout.ADDRESS, // unsigned int * line
        ValueLayout.ADDRESS, // unsigned int * column
        ValueLayout.ADDRESS // unsigned int * offset
    )

    // clang_getExpansionLocation: void clang_getExpansionLocation(CXSourceLocation, CXFile **, unsigned int *, unsigned int *, unsigned int *, unsigned int *)
    private val GET_EXPANSION_LOCATION_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS, // CXSourceLocation
        ValueLayout.ADDRESS, // CXFile **
        ValueLayout.ADDRESS, // unsigned int * line
        ValueLayout.ADDRESS, // unsigned int * column
        ValueLayout.ADDRESS, // unsigned int * offset
        ValueLayout.ADDRESS // unsigned int * macroArgIndex
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
                "FFM requires Java 21+. Current version: " + Runtime.version()
            )
        }

        try {
            println("[ClangFFMWrapper] Starting initialization...")
            println("[ClangFFMWrapper] java.library.path: " + System.getProperty("java.library.path"))
            println("[ClangFFMWrapper] Current working directory: " + System.getProperty("user.dir"))

            var loader = linker.defaultLookup()

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
                                libLookup.find("clang_disposeTranslationUnit").isPresent
                            ) {
                                clangLib = libLookup.find("clang_createIndex").orElse(null)
                                loader = libLookup
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
                        "Check java.library.path and PATH environment variables."
                )
            }

            // Resolve required function symbols
            clang_createIndex = resolveSymbol(loader, "clang_createIndex", CREATE_INDEX_DESC)
            clang_parseTranslationUnit = resolveSymbol(loader, "clang_parseTranslationUnit", PARSE_TU_DESC)
            clang_disposeIndex = resolveSymbol(loader, "clang_disposeIndex", DISPOSE_INDEX_DESC)
            clang_disposeTranslationUnit =
                resolveSymbol(loader, "clang_disposeTranslationUnit", DISPOSE_TU_DESC)

            // Resolve cursor function symbols (GRA-4)
            try {
                clang_getTranslationUnitCursor =
                    resolveSymbol(loader, "clang_getTranslationUnitCursor", GET_TU_CURSOR_DESC)
            } catch (e: ClangInitializationException) {
                clang_getTranslationUnitCursor = null
            }

            try {
                clang_getCursorKind = resolveSymbol(loader, "clang_getCursorKind", GET_CURSOR_KIND_DESC)
            } catch (e: ClangInitializationException) {
                clang_getCursorKind = null
            }

            try {
                clang_getCursorSpelling = resolveSymbol(loader, "clang_getCursorSpelling", GET_CURSOR_SPELLING_DESC)
            } catch (e: ClangInitializationException) {
                clang_getCursorSpelling = null
            }

            try {
                clang_getCursorLocation =
                    resolveSymbol(loader, "clang_getCursorLocation", GET_CURSOR_LOCATION_DESC)
            } catch (e: ClangInitializationException) {
                clang_getCursorLocation = null
            }

            try {
                clang_getCursorSemanticParent =
                    resolveSymbol(loader, "clang_getCursorSemanticParent", GET_CURSOR_SEMANTIC_PARENT_DESC)
            } catch (e: ClangInitializationException) {
                clang_getCursorSemanticParent = null
            }

            try {
                clang_getCursorUSR = resolveSymbol(loader, "clang_getCursorUSR", GET_CURSOR_USR_DESC)
            } catch (e: ClangInitializationException) {
                clang_getCursorUSR = null
            }

            try {
                clang_disposeCursor = resolveSymbol(loader, "clang_disposeCursor", DISPOSE_CURSOR_DESC)
            } catch (e: ClangInitializationException) {
                clang_disposeCursor = null
            }

            try {
                clang_getCursorChildren =
                    resolveSymbol(loader, "clang_getCursorChildren", GET_CURSOR_CHILDREN_DESC)
            } catch (e: ClangInitializationException) {
                clang_getCursorChildren = null
            }

            // Resolve diagnostic function symbols (GRA-4)
            try {
                clang_getDiagnosticSeverity =
                    resolveSymbol(loader, "clang_getDiagnosticSeverity", GET_DIAG_SEVERITY_DESC)
            } catch (e: ClangInitializationException) {
                clang_getDiagnosticSeverity = null
            }

            try {
                clang_getDiagnosticSpelling =
                    resolveSymbol(loader, "clang_getDiagnosticSpelling", GET_DIAG_SPELLING_DESC)
            } catch (e: ClangInitializationException) {
                clang_getDiagnosticSpelling = null
            }

            try {
                clang_getDiagnosticLocation =
                    resolveSymbol(loader, "clang_getDiagnosticLocation", GET_DIAG_LOCATION_DESC)
            } catch (e: ClangInitializationException) {
                clang_getDiagnosticLocation = null
            }

            try {
                clang_getDiagnosticCategory =
                    resolveSymbol(loader, "clang_getDiagnosticCategory", GET_DIAG_CATEGORY_DESC)
            } catch (e: ClangInitializationException) {
                clang_getDiagnosticCategory = null
            }

            try {
                clang_getDiagnosticOption =
                    resolveSymbol(loader, "clang_getDiagnosticOption", GET_DIAG_OPTION_DESC)
            } catch (e: ClangInitializationException) {
                clang_getDiagnosticOption = null
            }

            // Resolve string function symbols
            try {
                clang_getCString = resolveSymbol(loader, "clang_getCString", GET_CSTRING_DESC)
            } catch (e: ClangInitializationException) {
                clang_getCString = null
            }

            try {
                clang_disposeString = resolveSymbol(loader, "clang_disposeString", DISPOSE_STRING_DESC)
            } catch (e: ClangInitializationException) {
                clang_disposeString = null
            }

            // Resolve source location function symbols (GRA-4)
            try {
                clang_getFileLocation =
                    resolveSymbol(loader, "clang_getFileLocation", GET_FILE_LOCATION_DESC)
            } catch (e: ClangInitializationException) {
                clang_getFileLocation = null
            }

            try {
                clang_getExpansionLocation =
                    resolveSymbol(loader, "clang_getExpansionLocation", GET_EXPANSION_LOCATION_DESC)
            } catch (e: ClangInitializationException) {
                clang_getExpansionLocation = null
            }

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
    private fun resolveSymbol(
        loader: SymbolLookup,
        symbolName: String,
        desc: FunctionDescriptor
    ): MethodHandle {
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
                "$operationName returned NULL pointer. Check libclang version and parameters."
            )
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
    @JvmOverloads
    fun parseTranslationUnit(
        index: MemorySegment?,
        sourceFilePath: String?,
        commandLineArgs: Array<String>? = null
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
                index,
                filePath,
                argsArray,
                args.size,
                MemorySegment.NULL,
                0,
                0
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
            clang_getNumDiagnostics!!.invoke(translationUnit) as Int
        } catch (t: Throwable) {
            -1
        }
    }

    @JvmStatic
    fun ensureInitialized() {
        if (!initialized) {
            throw ClangInitializationException(
                "ClangFFMWrapper not initialized. Call initialize() first."
            )
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

    // ==================== Helper Functions (GRA-4) ====================

    /**
     * Data class to hold extracted source location information.
     */
    data class SourceLocationData(
        val file: String?,
        val line: Int,
        val column: Int,
        val offset: Long
    )

    /**
     * Extract a Kotlin String from a CXString handle.
     * Automatically disposes the CXString after extraction.
     *
     * @param cxStringHandle The CXString MemorySegment
     * @return The extracted String, or empty string if CXString is NULL
     */
    @JvmStatic
    fun extractStringFromCXString(cxStringHandle: MemorySegment?): String {
        if (cxStringHandle == null || cxStringHandle == MemorySegment.NULL) {
            return ""
        }

        if (clang_getCString == null) {
            return "<clang_getCString not available>"
        }

        try {
            val cStringPtr = clang_getCString!!.invoke(cxStringHandle) as MemorySegment
            if (cStringPtr == MemorySegment.NULL) {
                return ""
            }
            return cStringPtr.getString(0, StandardCharsets.UTF_8)
        } catch (t: Throwable) {
            return "<error extracting string: ${t.message}>"
        } finally {
            // Dispose the CXString
            try {
                if (clang_disposeString != null) {
                    clang_disposeString!!.invoke(cxStringHandle)
                }
            } catch (t: Throwable) {
                // Ignore dispose errors
            }
        }
    }

    /**
     * Get the cursor kind from a CXCursor handle.
     *
     * @param cursorHandle The CXCursor MemorySegment
     * @return The CursorKind, or UNKNOWN if not available
     */
    @JvmStatic
    fun getCursorKindFromHandle(cursorHandle: MemorySegment?): CursorKind {
        if (cursorHandle == null || cursorHandle == MemorySegment.NULL) {
            return CursorKind.UNKNOWN
        }

        if (clang_getCursorKind == null) {
            return CursorKind.UNKNOWN
        }

        try {
            val kindValue = clang_getCursorKind!!.invoke(cursorHandle) as Int
            return CursorKind.fromValue(kindValue)
        } catch (t: Throwable) {
            return CursorKind.UNKNOWN
        }
    }

    /**
     * Get the spelling (name) from a CXCursor handle.
     *
     * @param cursorHandle The CXCursor MemorySegment
     * @return The spelling as a String
     */
    @JvmStatic
    fun getCursorSpellingFromHandle(cursorHandle: MemorySegment?): String {
        if (cursorHandle == null || cursorHandle == MemorySegment.NULL) {
            return ""
        }

        if (clang_getCursorSpelling == null) {
            return "<clang_getCursorSpelling not available>"
        }

        try {
            val cxString = clang_getCursorSpelling!!.invoke(cursorHandle) as MemorySegment
            return extractStringFromCXString(cxString)
        } catch (t: Throwable) {
            return "<error getting cursor spelling: ${t.message}>"
        }
    }

    /**
     * Get the location from a CXCursor handle.
     *
     * @param cursorHandle The CXCursor MemorySegment
     * @return MemorySegment pointing to CXSourceLocation
     */
    @JvmStatic
    fun getCursorLocationFromHandle(cursorHandle: MemorySegment?): MemorySegment {
        if (cursorHandle == null || cursorHandle == MemorySegment.NULL) {
            return MemorySegment.NULL
        }

        if (clang_getCursorLocation == null) {
            return MemorySegment.NULL
        }

        try {
            return clang_getCursorLocation!!.invoke(cursorHandle) as MemorySegment
        } catch (t: Throwable) {
            return MemorySegment.NULL
        }
    }

    /**
     * Get the semantic parent from a CXCursor handle.
     *
     * @param cursorHandle The CXCursor MemorySegment
     * @return MemorySegment pointing to parent CXCursor, or NULL
     */
    @JvmStatic
    fun getCursorSemanticParentFromHandle(cursorHandle: MemorySegment?): MemorySegment {
        if (cursorHandle == null || cursorHandle == MemorySegment.NULL) {
            return MemorySegment.NULL
        }

        if (clang_getCursorSemanticParent == null) {
            return MemorySegment.NULL
        }

        try {
            return clang_getCursorSemanticParent!!.invoke(cursorHandle) as MemorySegment
        } catch (t: Throwable) {
            return MemorySegment.NULL
        }
    }

    /**
     * Get the USR from a CXCursor handle.
     *
     * @param cursorHandle The CXCursor MemorySegment
     * @return The USR as a String
     */
    @JvmStatic
    fun getCursorUSRFromHandle(cursorHandle: MemorySegment?): String {
        if (cursorHandle == null || cursorHandle == MemorySegment.NULL) {
            return ""
        }

        if (clang_getCursorUSR == null) {
            return "<clang_getCursorUSR not available>"
        }

        try {
            val cxString = clang_getCursorUSR!!.invoke(cursorHandle) as MemorySegment
            return extractStringFromCXString(cxString)
        } catch (t: Throwable) {
            return "<error getting cursor USR: ${t.message}>"
        }
    }

    /**
     * Dispose a cursor handle.
     *
     * @param cursorHandle The CXCursor MemorySegment to dispose
     */
    @JvmStatic
    fun disposeCursor(cursorHandle: MemorySegment?) {
        if (cursorHandle == null || cursorHandle == MemorySegment.NULL) {
            return
        }

        if (clang_disposeCursor == null) {
            return
        }

        try {
            clang_disposeCursor!!.invoke(cursorHandle)
        } catch (t: Throwable) {
            // Ignore dispose errors
        }
    }

    /**
     * Get all direct child cursors from a parent cursor handle.
     * Uses clang_getCursorChildren which returns an array of cursors.
     *
     * @param parentHandle The parent CXCursor MemorySegment
     * @return List of child cursor MemorySegments, or empty list if not available
     */
    @JvmStatic
    fun getCursorChildrenFromHandle(parentHandle: MemorySegment?): List<MemorySegment> {
        if (parentHandle == null || parentHandle == MemorySegment.NULL) {
            return emptyList()
        }

        if (clang_getCursorChildren == null) {
            return emptyList()
        }

        try {
            Arena.ofConfined().use { arena ->
                // Allocate memory for the output array pointer
                val childrenPtrPtr = arena.allocate(ValueLayout.ADDRESS)
                // Allocate memory for the output count
                val numChildrenPtr = arena.allocate(ValueLayout.JAVA_INT)

                // Call clang_getCursorChildren
                clang_getCursorChildren!!.invoke(
                    parentHandle,
                    childrenPtrPtr,
                    numChildrenPtr
                )

                // Get the children array pointer and count
                val childrenArrayPtr = childrenPtrPtr.get(ValueLayout.ADDRESS, 0)
                val numChildren = numChildrenPtr.get(ValueLayout.JAVA_INT, 0)

                if (childrenArrayPtr == MemorySegment.NULL || numChildren <= 0) {
                    return emptyList()
                }

                // Read the array of cursor pointers
                val children = mutableListOf<MemorySegment>()
                for (i in 0 until numChildren) {
                    val childHandle = childrenArrayPtr.getAtIndex(ValueLayout.ADDRESS, i.toLong())
                    if (childHandle != MemorySegment.NULL) {
                        children.add(childHandle)
                    }
                }

                return children
            }
        } catch (t: Throwable) {
            return emptyList()
        }
    }

    /**
     * Get the translation unit cursor from a translation unit handle.
     *
     * @param tuHandle The CXTranslationUnit MemorySegment
     * @return MemorySegment pointing to CXCursor
     */
    @JvmStatic
    fun getTranslationUnitCursorFromHandle(tuHandle: MemorySegment?): MemorySegment {
        if (tuHandle == null || tuHandle == MemorySegment.NULL) {
            return MemorySegment.NULL
        }

        if (clang_getTranslationUnitCursor == null) {
            return MemorySegment.NULL
        }

        try {
            return clang_getTranslationUnitCursor!!.invoke(tuHandle) as MemorySegment
        } catch (t: Throwable) {
            return MemorySegment.NULL
        }
    }

    // ==================== Diagnostic Functions (GRA-4) ====================

    /**
     * Get the severity from a diagnostic handle.
     *
     * @param diagnosticHandle The CXDiagnostic MemorySegment
     * @return The Severity
     */
    @JvmStatic
    fun getDiagnosticSeverityFromHandle(diagnosticHandle: MemorySegment?): Severity {
        if (diagnosticHandle == null || diagnosticHandle == MemorySegment.NULL) {
            return Severity.UNKNOWN
        }

        if (clang_getDiagnosticSeverity == null) {
            return Severity.UNKNOWN
        }

        try {
            val severityValue = clang_getDiagnosticSeverity!!.invoke(diagnosticHandle) as Int
            return Severity.fromValue(severityValue)
        } catch (t: Throwable) {
            return Severity.UNKNOWN
        }
    }

    /**
     * Get the spelling (message) from a diagnostic handle.
     *
     * @param diagnosticHandle The CXDiagnostic MemorySegment
     * @return The diagnostic message as a String
     */
    @JvmStatic
    fun getDiagnosticSpellingFromHandle(diagnosticHandle: MemorySegment?): String {
        if (diagnosticHandle == null || diagnosticHandle == MemorySegment.NULL) {
            return ""
        }

        if (clang_getDiagnosticSpelling == null) {
            return "<clang_getDiagnosticSpelling not available>"
        }

        try {
            val cxString = clang_getDiagnosticSpelling!!.invoke(diagnosticHandle) as MemorySegment
            return extractStringFromCXString(cxString)
        } catch (t: Throwable) {
            return "<error getting diagnostic spelling: ${t.message}>"
        }
    }

    /**
     * Get the location from a diagnostic handle.
     *
     * @param diagnosticHandle The CXDiagnostic MemorySegment
     * @return MemorySegment pointing to CXSourceLocation
     */
    @JvmStatic
    fun getDiagnosticLocationFromHandle(diagnosticHandle: MemorySegment?): MemorySegment {
        if (diagnosticHandle == null || diagnosticHandle == MemorySegment.NULL) {
            return MemorySegment.NULL
        }

        if (clang_getDiagnosticLocation == null) {
            return MemorySegment.NULL
        }

        try {
            return clang_getDiagnosticLocation!!.invoke(diagnosticHandle) as MemorySegment
        } catch (t: Throwable) {
            return MemorySegment.NULL
        }
    }

    /**
     * Get the category from a diagnostic handle.
     *
     * @param diagnosticHandle The CXDiagnostic MemorySegment
     * @return The category as a String
     */
    @JvmStatic
    fun getDiagnosticCategoryFromHandle(diagnosticHandle: MemorySegment?): String {
        if (diagnosticHandle == null || diagnosticHandle == MemorySegment.NULL) {
            return ""
        }

        if (clang_getDiagnosticCategory == null) {
            return "<clang_getDiagnosticCategory not available>"
        }

        try {
            val categoryValue = clang_getDiagnosticCategory!!.invoke(diagnosticHandle) as Int
            return categoryValue.toString()
        } catch (t: Throwable) {
            return "<error getting diagnostic category: ${t.message}>"
        }
    }

    /**
     * Get the option from a diagnostic handle.
     *
     * @param diagnosticHandle The CXDiagnostic MemorySegment
     * @return The option as a String
     */
    @JvmStatic
    fun getDiagnosticOptionFromHandle(diagnosticHandle: MemorySegment?): String {
        if (diagnosticHandle == null || diagnosticHandle == MemorySegment.NULL) {
            return ""
        }

        if (clang_getDiagnosticOption == null) {
            return "<clang_getDiagnosticOption not available>"
        }

        try {
            val cxString = clang_getDiagnosticOption!!.invoke(diagnosticHandle) as MemorySegment
            return extractStringFromCXString(cxString)
        } catch (t: Throwable) {
            return "<error getting diagnostic option: ${t.message}>"
        }
    }

    // ==================== SourceLocation Functions (GRA-4) ====================

    /**
     * Extract file, line, column, offset from a CXSourceLocation handle.
     *
     * @param locationHandle The CXSourceLocation MemorySegment
     * @return SourceLocationData with all location info, or null if failed
     */
    @JvmStatic
    fun extractLocationData(locationHandle: MemorySegment?): SourceLocationData? {
        if (locationHandle == null || locationHandle == MemorySegment.NULL) {
            return null
        }

        // Try clang_getFileLocation first (returns file, line, column, offset)
        if (clang_getFileLocation != null) {
            try {
                Arena.ofConfined().use { arena ->
                    val filePtrPtr = arena.allocate(ValueLayout.ADDRESS)
                    val linePtr = arena.allocate(ValueLayout.JAVA_INT)
                    val columnPtr = arena.allocate(ValueLayout.JAVA_INT)
                    val offsetPtr = arena.allocate(ValueLayout.JAVA_LONG)

                    clang_getFileLocation!!.invoke(
                        locationHandle,
                        filePtrPtr,
                        linePtr,
                        columnPtr,
                        offsetPtr
                    )

                    val filePtr = filePtrPtr.get(ValueLayout.ADDRESS, 0)
                    val fileName = if (filePtr != MemorySegment.NULL) {
                        filePtr.getString(0, StandardCharsets.UTF_8)
                    } else {
                        null
                    }

                    val line = linePtr.get(ValueLayout.JAVA_INT, 0)
                    val column = columnPtr.get(ValueLayout.JAVA_INT, 0)
                    val offset = offsetPtr.get(ValueLayout.JAVA_LONG, 0)

                    return SourceLocationData(fileName, line, column, offset)
                }
            } catch (t: Throwable) {
                // Try expansion location as fallback
            }
        }

        // Try clang_getExpansionLocation as fallback
        if (clang_getExpansionLocation != null) {
            try {
                Arena.ofConfined().use { arena ->
                    val filePtrPtr = arena.allocate(ValueLayout.ADDRESS)
                    val linePtr = arena.allocate(ValueLayout.JAVA_INT)
                    val columnPtr = arena.allocate(ValueLayout.JAVA_INT)
                    val offsetPtr = arena.allocate(ValueLayout.JAVA_LONG)
                    val macroArgPtr = arena.allocate(ValueLayout.JAVA_INT)

                    clang_getExpansionLocation!!.invoke(
                        locationHandle,
                        filePtrPtr,
                        linePtr,
                        columnPtr,
                        offsetPtr,
                        macroArgPtr
                    )

                    val filePtr = filePtrPtr.get(ValueLayout.ADDRESS, 0)
                    val fileName = if (filePtr != MemorySegment.NULL) {
                        filePtr.getString(0, StandardCharsets.UTF_8)
                    } else {
                        null
                    }

                    val line = linePtr.get(ValueLayout.JAVA_INT, 0)
                    val column = columnPtr.get(ValueLayout.JAVA_INT, 0)
                    val offset = offsetPtr.get(ValueLayout.JAVA_LONG, 0)

                    return SourceLocationData(fileName, line, column, offset)
                }
            } catch (t: Throwable) {
                // Ignore
            }
        }

        return null
    }
}
