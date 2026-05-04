// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.MemorySegment

/**
 * Basic wrapper for a Clang CXCursor.
 * Represents a cursor in the Clang AST.
 * 
 * Note: Full cursor functionality will be implemented in GRA-3.
 */
class ClangCursor(
    /** The underlying MemorySegment handle. */
    handle: MemorySegment?
) {
    val handle: MemorySegment

    /**
     * Creates a new ClangCursor from a MemorySegment handle.
     * @param handle The MemorySegment handle to the CXCursor
     * @throws IllegalArgumentException if handle is null or NULL
     */
    init {
        if (handle == null || handle == MemorySegment.NULL) {
            throw IllegalArgumentException("Cursor handle cannot be null or NULL")
        }
        this.handle = handle
    }

    // Placeholder for future cursor functionality (GRA-3)
    // fun getKind(): CursorKind { ... }
    // fun getSpelling(): String { ... }
    // fun getChildren(): List<ClangCursor> { ... }
}
