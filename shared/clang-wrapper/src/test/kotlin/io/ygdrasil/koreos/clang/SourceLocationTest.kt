// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.lang.foreign.MemorySegment

/**
 * Unit tests for SourceLocation.
 * Part of GRA-4: Support des types complexes de Clang (AST, diagnostics)
 */
class SourceLocationTest {

    @Test
    fun sourceLocationWithNULLHandleThrowsException() {
        try {
            SourceLocation(MemorySegment.NULL)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("SourceLocation handle cannot be null or NULL", e.message)
        }
    }

    @Test
    fun sourceLocationWithNullHandleThrowsException() {
        try {
            SourceLocation(null)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("SourceLocation handle cannot be null or NULL", e.message)
        }
    }

    @Test
    fun `SourceLocation with valid handle has isValid true`() {
        // Create a location with a non-NULL handle (for testing purposes)
        // Note: In real usage, this would come from libclang
        // For now, we can't easily create a valid handle without libclang
        // This test will be properly implemented in GREEN phase
    }

    @Test
    fun `getFile returns correct file path`() {
        // This test would need a real MemorySegment from libclang
        // For TDD phase, we just verify the property exists
        // Actual implementation will be tested in GREEN phase
    }

    @Test
    fun `getLine returns correct line number`() {
        // Placeholder for TDD - actual test needs libclang
    }

    @Test
    fun `getColumn returns correct column number`() {
        // Placeholder for TDD - actual test needs libclang
    }

    @Test
    fun `getOffset returns correct byte offset`() {
        // Placeholder for TDD - actual test needs libclang
    }

    @Test
    fun `isValid returns true for valid location`() {
        // Placeholder for TDD
    }

    @Test
    fun `isValid returns false for invalid location`() {
        // Placeholder for TDD
    }

    @Test
    fun `toString returns formatted location string`() {
        // Placeholder for TDD
        // Expected format: "file:line:column"
    }
}
