// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.MemorySegment

/**
 * High-level wrapper for a Clang CXTranslationUnit.
 * Represents a parsed translation unit (source file).
 */
class ClangTranslationUnit internal constructor(
    /** The underlying MemorySegment handle. */
    handle: MemorySegment?
) : AutoCloseable {
    val handle: MemorySegment
    private var disposed = false

    /**
     * Creates a new ClangTranslationUnit from a MemorySegment handle.
     * @param handle The MemorySegment handle to the CXTranslationUnit
     * @throws IllegalArgumentException if handle is null or NULL
     */
    init {
        if (handle == null || handle == MemorySegment.NULL) {
            throw IllegalArgumentException("Translation unit handle cannot be null or NULL")
        }
        this.handle = handle
    }

    /**
     * Gets the number of diagnostics for this translation unit.
     * @return Number of diagnostics, or -1 if not supported
     */
    fun getNumDiagnostics(): Int {
        return ClangFFMWrapper.getNumDiagnostics(handle)
    }

    /**
     * The root cursor for this translation unit.
     * The root cursor represents the entire translation unit.
     */
    val cursor: ClangCursor by lazy {
        // TODO: GRA-4 - Implement using clang_getTranslationUnitCursor
        // For now, return a placeholder cursor
        ClangCursor(handle)
    }

    /**
     * Gets all diagnostics for this translation unit.
     *
     * @return List of ClangDiagnostic objects
     */
    fun getDiagnostics(): List<ClangDiagnostic> {
        // TODO: GRA-4 - Implement using clang_getNumDiagnostics and clang_getDiagnostic
        val numDiags = getNumDiagnostics()
        if (numDiags <= 0) {
            return emptyList()
        }
        // Placeholder: return empty list until full implementation
        return emptyList()
    }

    /**
     * Checks if this translation unit has been disposed.
     * @return true if disposed
     */
    fun isDisposed(): Boolean {
        return disposed
    }

    /**
     * Disposes this translation unit and releases associated resources.
     */
    override fun close() {
        if (disposed) {
            return
        }
        ClangFFMWrapper.disposeTranslationUnit(handle)
        disposed = true
    }

    /**
     * Disposes this translation unit.
     */
    fun dispose() {
        close()
    }
}
