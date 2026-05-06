// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.MemorySegment

/**
 * Represents a source location in Clang.
 * Wraps the CXSourceLocation type from libclang.
 *
 * See: https://clang.llvm.org/doxygen/structCXSourceLocation.html
 */
class SourceLocation(
    /** The underlying MemorySegment handle to the CXSourceLocation. */
    handle: MemorySegment?
) {
    val handle: MemorySegment

    /** File name where the location is. */
    val file: String?

    /** Line number (1-indexed). */
    val line: Int

    /** Column number (1-indexed). */
    val column: Int

    /** Byte offset from the start of the file. */
    val offset: Int

    /**
     * Creates a new SourceLocation from a MemorySegment handle.
     * @param handle The MemorySegment handle to the CXSourceLocation
     * @throws IllegalArgumentException if handle is null or NULL
     */
    init {
        if (handle == null || handle == MemorySegment.NULL) {
            throw IllegalArgumentException("SourceLocation handle cannot be null or NULL")
        }
        this.handle = handle

        // Extract actual data from the location handle
        val locationData = ClangFFMWrapper.extractLocationData(handle)
        this.file = locationData?.file
        this.line = locationData?.line ?: 0
        this.column = locationData?.column ?: 0
        this.offset = (locationData?.offset ?: 0L).toInt()
    }

    /**
     * Check if this location is valid (non-null and points to a real location).
     */
    val isValid: Boolean
        get() = handle != MemorySegment.NULL

    /**
     * Returns a string representation of this location.
     * Format: "file:line:column" or "<unknown>:line:column" if file is null.
     */
    override fun toString(): String {
        val fileName = file ?: "<unknown>"
        return "$fileName:$line:$column"
    }

    /**
     * Check if this location is in the same file as another location.
     */
    fun isSameFileAs(other: SourceLocation): Boolean {
        if (!this.isValid || !other.isValid) return false
        return this.file == other.file
    }

    /**
     * Check if this location comes before another location in the same file.
     */
    fun isBefore(other: SourceLocation): Boolean {
        if (!isSameFileAs(other)) return false
        if (this.line < other.line) return true
        if (this.line > other.line) return false
        return this.column < other.column
    }
}
