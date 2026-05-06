// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.foreign.MemorySegment

/**
 * Unit tests for ClangDiagnostic.
 * Part of GRA-4: Support des types complexes de Clang (AST, diagnostics)
 *
 * TDD Phase: RED - Tests written BEFORE production code
 */
class ClangDiagnosticTest {

    @BeforeEach
    fun setUp() {
        ClangFFMWrapper.initialize()
    }

    // ==================== Constructor Tests ====================

    @Test
    fun constructorWithNULLHandleThrowsIllegalArgumentException() {
        try {
            ClangDiagnostic(MemorySegment.NULL)
            org.junit.jupiter.api.fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Diagnostic handle cannot be null or NULL", e.message)
        }
    }

    @Test
    fun constructorWithNullHandleThrowsIllegalArgumentException() {
        try {
            ClangDiagnostic(null)
            org.junit.jupiter.api.fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Diagnostic handle cannot be null or NULL", e.message)
        }
    }

    // ==================== getSeverity() Tests ====================

    @Test
    fun `getSeverity returns correct Severity for error`() {
        // This test will fail in RED phase - implementation doesn't exist yet
        // val diagnostic = getTestDiagnostic(Severity.ERROR)
        // assertEquals(Severity.ERROR, diagnostic.getSeverity())
    }

    @Test
    fun `getSeverity returns correct Severity for warning`() {
        // Placeholder for TDD
    }

    @Test
    fun `getSeverity returns correct Severity for note`() {
        // Placeholder for TDD
    }

    // ==================== getMessage() Tests ====================

    @Test
    fun `getMessage returns diagnostic message`() {
        // This test will fail in RED phase
        // val diagnostic = getTestDiagnostic(Severity.ERROR, "Test error message")
        // assertEquals("Test error message", diagnostic.getMessage())
    }

    @Test
    fun `getMessage returns empty string for diagnostic without message`() {
        // Placeholder for TDD
    }

    @Test
    fun `getMessage handles NULL return from native`() {
        // Placeholder for TDD
    }

    // ==================== getLocation() Tests ====================

    @Test
    fun `getLocation returns valid SourceLocation`() {
        // This test will fail in RED phase
        // val diagnostic = getTestDiagnostic(Severity.ERROR)
        // val location = diagnostic.getLocation()
        // assertNotNull(location)
        // assertTrue(location.isValid)
    }

    @Test
    fun `getLocation returns correct file and line for diagnostic`() {
        // Placeholder for TDD
    }

    // ==================== getCategory() Tests ====================

    @Test
    fun `getCategory returns diagnostic category`() {
        // Placeholder for TDD
    }

    // ==================== getOption() Tests ====================

    @Test
    fun `getOption returns diagnostic option string`() {
        // Placeholder for TDD
    }

    // ==================== isError() Tests ====================

    @Test
    fun `isError returns true for error diagnostic`() {
        // Placeholder for TDD
    }

    @Test
    fun `isError returns false for warning diagnostic`() {
        // Placeholder for TDD
    }

    // ==================== isWarning() Tests ====================

    @Test
    fun `isWarning returns true for warning diagnostic`() {
        // Placeholder for TDD
    }

    @Test
    fun `isWarning returns false for error diagnostic`() {
        // Placeholder for TDD
    }

    // ==================== Edge Cases ====================

    @Test
    fun `diagnostic operations throw after dispose`() {
        // Placeholder for TDD
    }

    @Test
    fun `diagnostic operations handle invalid MemorySegment gracefully`() {
        // Placeholder for TDD
    }

    // ==================== Helper Methods ====================

    /**
     * Helper to create a test diagnostic (will be implemented in GREEN phase)
     */
    private fun getTestDiagnostic(severity: Severity, message: String = ""): ClangDiagnostic {
        // This is a placeholder - actual implementation will use libclang
        // to create real diagnostics for testing
        TODO("Implement in GREEN phase")
    }
}
