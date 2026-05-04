// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import java.lang.foreign.MemorySegment;

/**
 * Basic wrapper for a Clang CXDiagnostic.
 * Represents a diagnostic message from Clang.
 * 
 * Note: Full diagnostic functionality will be implemented in GRA-3.
 */
public final class ClangDiagnostic {

    private final MemorySegment handle;

    /**
     * Creates a new ClangDiagnostic from a MemorySegment handle.
     * @param handle The MemorySegment handle to the CXDiagnostic
     * @throws IllegalArgumentException if handle is null or NULL
     */
    public ClangDiagnostic(MemorySegment handle) {
        if (handle == null || handle.equals(MemorySegment.NULL)) {
            throw new IllegalArgumentException("Diagnostic handle cannot be null or NULL");
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

    // Placeholder for future diagnostic functionality (GRA-3)
    // public Severity getSeverity() { ... }
    // public String getMessage() { ... }
    // public SourceLocation getLocation() { ... }
}
