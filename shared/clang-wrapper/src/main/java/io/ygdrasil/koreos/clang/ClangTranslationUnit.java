// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import java.lang.foreign.MemorySegment;

/**
 * High-level wrapper for a Clang CXTranslationUnit.
 * Represents a parsed translation unit (source file).
 */
public final class ClangTranslationUnit implements AutoCloseable {

    private final MemorySegment handle;
    private boolean disposed = false;

    /**
     * Creates a new ClangTranslationUnit from a MemorySegment handle.
     * @param handle The MemorySegment handle to the CXTranslationUnit
     * @throws IllegalArgumentException if handle is null or NULL
     */
    ClangTranslationUnit(MemorySegment handle) {
        if (handle == null || handle.equals(MemorySegment.NULL)) {
            throw new IllegalArgumentException("Translation unit handle cannot be null or NULL");
        }
        this.handle = handle;
    }

    /**
     * Gets the underlying MemorySegment handle.
     * For advanced use only.
     * @return The MemorySegment handle
     */
    public MemorySegment getHandle() {
        return handle;
    }

    /**
     * Gets the number of diagnostics for this translation unit.
     * @return Number of diagnostics, or -1 if not supported
     */
    public int getNumDiagnostics() {
        return ClangFFMWrapper.getNumDiagnostics(handle);
    }

    /**
     * Checks if this translation unit has been disposed.
     * @return true if disposed
     */
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * Disposes this translation unit and releases associated resources.
     */
    @Override
    public void close() {
        if (disposed) {
            return;
        }
        ClangFFMWrapper.disposeTranslationUnit(handle);
        disposed = true;
    }

    /**
     * Disposes this translation unit.
     */
    public void dispose() {
        close();
    }


}
