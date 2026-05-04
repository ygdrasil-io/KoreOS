// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.MemorySegment

/**
 * High-level wrapper for a Clang CXTranslationUnit.
 * Represents a parsed translation unit (source file).
 */
class ClangTranslationUnit internal constructor(
    /** The underlying MemorySegment handle. */
    val handle: MemorySegment
) : AutoCloseable {

    private var disposed = false

    /**
     * Creates a new ClangTranslationUnit from a MemorySegment handle.
     * @param handle The MemorySegment handle to the CXTranslationUnit
     * @throws IllegalArgumentException if handle is null or NULL
     */
    init {
        if (handle == MemorySegment.NULL) {
            throw IllegalArgumentException("Translation unit handle cannot be null or NULL")
        }
    }

    /**
     * Gets the number of diagnostics for this translation unit.
     * @return Number of diagnostics, or -1 if not supported
     */
    fun getNumDiagnostics(): Int {
        return ClangFFMWrapper.getNumDiagnostics(handle)
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
