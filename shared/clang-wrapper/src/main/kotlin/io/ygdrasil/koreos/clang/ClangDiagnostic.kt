// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.MemorySegment

/**
 * Basic wrapper for a Clang CXDiagnostic.
 * Represents a diagnostic message from Clang.
 * 
 * Note: Full diagnostic functionality will be implemented in GRA-3.
 */
class ClangDiagnostic(
    /** The underlying MemorySegment handle. */
    val handle: MemorySegment
) {
    /**
     * Creates a new ClangDiagnostic from a MemorySegment handle.
     * @param handle The MemorySegment handle to the CXDiagnostic
     * @throws IllegalArgumentException if handle is null or NULL
     */
    init {
        if (handle == MemorySegment.NULL) {
            throw IllegalArgumentException("Diagnostic handle cannot be null or NULL")
        }
    }

    // Placeholder for future diagnostic functionality (GRA-3)
    // fun getSeverity(): Severity { ... }
    // fun getMessage(): String { ... }
    // fun getLocation(): SourceLocation { ... }
}
