// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.MemorySegment

/**
 * Basic wrapper for a Clang CXDiagnostic.
 * Represents a diagnostic message from Clang.
 */
class ClangDiagnostic(
    /** The underlying MemorySegment handle. */
    handle: MemorySegment?
) {
    val handle: MemorySegment

    /**
     * Creates a new ClangDiagnostic from a MemorySegment handle.
     * @param handle The MemorySegment handle to the CXDiagnostic
     * @throws IllegalArgumentException if handle is null or NULL
     */
    init {
        if (handle == null || handle == MemorySegment.NULL) {
            throw IllegalArgumentException("Diagnostic handle cannot be null or NULL")
        }
        this.handle = handle
    }

    // Placeholder for future diagnostic functionality (GRA-3)
    // fun getSeverity(): Severity { ... }
    // fun getMessage(): String { ... }
    // fun getLocation(): SourceLocation { ... }
}
