// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.lang.foreign.MemorySegment

/**
 * Wrapper for a Clang CXDiagnostic.
 * Represents a diagnostic message from Clang.
 *
 * Part of GRA-4: Support des types complexes de Clang (AST, diagnostics)
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

    /**
     * Check if this diagnostic is null (handle is NULL).
     */
    fun isNull(): Boolean {
        return handle == MemorySegment.NULL
    }

    /**
     * Get the severity of this diagnostic.
     *
     * @return The Severity of this diagnostic
     */
    fun getSeverity(): Severity {
        return ClangFFMWrapper.getDiagnosticSeverityFromHandle(handle)
    }

    /**
     * Get the message text of this diagnostic.
     *
     * @return The diagnostic message
     */
    fun getMessage(): String {
        return ClangFFMWrapper.getDiagnosticSpellingFromHandle(handle)
    }

    /**
     * Get the source location where this diagnostic occurred.
     *
     * @return The SourceLocation of this diagnostic
     */
    fun getLocation(): SourceLocation {
        val locationHandle = ClangFFMWrapper.getDiagnosticLocationFromHandle(handle)
        return SourceLocation(locationHandle)
    }

    /**
     * Get the category text of this diagnostic.
     *
     * @return The category string
     */
    fun getCategory(): String {
        return ClangFFMWrapper.getDiagnosticCategoryFromHandle(handle)
    }

    /**
     * Get the option string for this diagnostic.
     * This is the command-line option that can be used to disable this diagnostic.
     *
     * @return The option string
     */
    fun getOption(): String {
        return ClangFFMWrapper.getDiagnosticOptionFromHandle(handle)
    }

    /**
     * Check if this diagnostic represents an error.
     */
    fun isError(): Boolean {
        return getSeverity().isError
    }

    /**
     * Check if this diagnostic represents a warning.
     */
    fun isWarning(): Boolean {
        return getSeverity() == Severity.WARNING
    }

    /**
     * Check equality with another diagnostic.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClangDiagnostic) return false
        return handle == other.handle
    }

    /**
     * Hash code based on handle.
     */
    override fun hashCode(): Int {
        return handle.hashCode()
    }
}
