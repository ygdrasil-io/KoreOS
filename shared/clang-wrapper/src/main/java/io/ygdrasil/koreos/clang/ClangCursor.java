// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import java.lang.foreign.MemorySegment;

/**
 * Basic wrapper for a Clang CXCursor.
 * Represents a cursor in the Clang AST.
 * 
 * Note: Full cursor functionality will be implemented in GRA-3.
 */
public final class ClangCursor {

    private final MemorySegment handle;

    /**
     * Creates a new ClangCursor from a MemorySegment handle.
     * @param handle The MemorySegment handle to the CXCursor
     * @throws IllegalArgumentException if handle is null or NULL
     */
    public ClangCursor(MemorySegment handle) {
        if (handle == null || handle.equals(MemorySegment.NULL)) {
            throw new IllegalArgumentException("Cursor handle cannot be null or NULL");
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

    // Placeholder for future cursor functionality (GRA-3)
    // public CursorKind getKind() { ... }
    // public String getSpelling() { ... }
    // public ClangCursor[] getChildren() { ... }
}
