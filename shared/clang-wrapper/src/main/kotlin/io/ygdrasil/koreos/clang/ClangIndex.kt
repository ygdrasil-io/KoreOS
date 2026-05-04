// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.MemorySegment

/**
 * High-level wrapper for a Clang CXIndex.
 * Represents a Clang index object used for parsing source code.
 */
class ClangIndex private constructor(
    /** The underlying MemorySegment handle. */
    val handle: MemorySegment,
    /** Whether this index has been disposed. */
    var disposed: Boolean = false
) : AutoCloseable {

    init {
        ClangFFMWrapper.ensureInitialized()
    }

    /**
     * Creates a new Clang index.
     * @param excludeDeclsFromPCH Whether to exclude declarations from precompiled headers
     * @param displayDiagnostics Whether to display diagnostics
     * @throws ClangInitializationException if ClangFFMWrapper is not initialized
     * @throws ClangMemoryException if index creation fails
     */
    constructor(excludeDeclsFromPCH: Boolean, displayDiagnostics: Boolean) : this(
        ClangFFMWrapper.createIndex(excludeDeclsFromPCH, displayDiagnostics)
    ) {
        ClangFFMWrapper.ensureInitialized()
    }

    /**
     * Creates a new Clang index with default settings.
     */
    constructor() : this(false, false)

    /**
     * Parses a translation unit (source file).
     * @param sourceFilePath Path to the source file
     * @param commandLineArgs Command line arguments for the parser
     * @return A new ClangTranslationUnit
     * @throws ClangParsingException if parsing fails
     * @throws ClangMemoryException if result is NULL
     */
    fun parseTranslationUnit(sourceFilePath: String, commandLineArgs: Array<String>): ClangTranslationUnit {
        val tu = ClangFFMWrapper.parseTranslationUnit(handle, sourceFilePath, commandLineArgs)
        return ClangTranslationUnit(tu)
    }

    /**
     * Parses a translation unit with default command line arguments.
     * @param sourceFilePath Path to the source file
     * @return A new ClangTranslationUnit
     */
    fun parseTranslationUnit(sourceFilePath: String): ClangTranslationUnit {
        return parseTranslationUnit(sourceFilePath, emptyArray())
    }

    /**
     * Checks if this index has been disposed.
     * @return true if disposed
     */
    fun isDisposed(): Boolean {
        return disposed
    }

    /**
     * Disposes this index and releases associated resources.
     */
    override fun close() {
        if (disposed) {
            return
        }
        ClangFFMWrapper.disposeIndex(handle)
        disposed = true
    }

    /**
     * Disposes this index.
     */
    fun dispose() {
        close()
    }
}
