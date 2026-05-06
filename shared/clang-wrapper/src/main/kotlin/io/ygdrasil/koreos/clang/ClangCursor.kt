// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.MemorySegment

/**
 * Wrapper for a Clang CXCursor.
 * Represents a cursor in the Clang AST.
 *
 * Part of GRA-4: Support des types complexes de Clang (AST, diagnostics)
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

    /**
     * Check if this cursor is null (handle is NULL).
     */
    fun isNull(): Boolean {
        return handle == MemorySegment.NULL
    }

    /**
     * Get the kind of this cursor.
     *
     * @return The CursorKind representing the type of this cursor
     */
    fun getKind(): CursorKind {
        return ClangFFMWrapper.getCursorKindFromHandle(handle)
    }

    /**
     * Get the spelling (name) of this cursor.
     * Returns the name of a declaration or the text of a token.
     *
     * @return The spelling as a String
     */
    fun getSpelling(): String {
        return ClangFFMWrapper.getCursorSpellingFromHandle(handle)
    }

    /**
     * Get the source location of this cursor.
     *
     * @return The SourceLocation of this cursor
     */
    fun getLocation(): SourceLocation {
        val locationHandle = ClangFFMWrapper.getCursorLocationFromHandle(handle)
        return SourceLocation(locationHandle)
    }

    /**
     * Get the semantic parent of this cursor.
     * The semantic parent is the cursor that semantically contains this one.
     *
     * @return The semantic parent cursor, or null if none
     */
    fun getSemanticParent(): ClangCursor? {
        val parentHandle = ClangFFMWrapper.getCursorSemanticParentFromHandle(handle)
        return if (parentHandle == MemorySegment.NULL) {
            null
        } else {
            ClangCursor(parentHandle)
        }
    }

    /**
     * Get all direct child cursors of this cursor.
     *
     * @return List of child cursors
     */
    fun getChildren(): List<ClangCursor> {
        val childrenHandles = ClangFFMWrapper.getCursorChildrenFromHandle(handle)
        return childrenHandles.map { ClangCursor(it) }
    }

    /**
     * Get the USR (Unified Symbol Resolution) for this cursor.
     * USRs uniquely identify entities in the AST.
     *
     * @return The USR string, or empty string if none
     */
    fun getUSR(): String {
        return ClangFFMWrapper.getCursorUSRFromHandle(handle)
    }

    /**
     * Traverse the AST starting from this cursor.
     * Visits all nodes recursively.
     *
     * @param visitor Function to call for each cursor. Return false to stop traversal.
     */
    fun traverse(visitor: (ClangCursor) -> Boolean) {
        // TODO: GRA-4 - Implement recursive traversal
        // Simple implementation: visit current, then children
        if (!visitor(this)) {
            return
        }
        for (child in getChildren()) {
            child.traverse(visitor)
        }
    }

    /**
     * Find all cursors of a specific kind in the subtree.
     *
     * @param kind The CursorKind to find
     * @return List of matching cursors
     */
    fun findAll(kind: CursorKind): List<ClangCursor> {
        val result = mutableListOf<ClangCursor>()
        traverse { cursor ->
            if (cursor.getKind() == kind) {
                result.add(cursor)
            }
            true
        }
        return result
    }

    /**
     * Check equality with another cursor.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClangCursor) return false
        return handle == other.handle
    }

    /**
     * Hash code based on handle.
     */
    override fun hashCode(): Int {
        return handle.hashCode()
    }
}
