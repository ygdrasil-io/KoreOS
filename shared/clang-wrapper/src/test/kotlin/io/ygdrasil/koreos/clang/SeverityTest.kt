// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for Severity enum.
 * Part of GRA-4: Support des types complexes de Clang (AST, diagnostics)
 */
class SeverityTest {

    @Test
    fun `Severity_IGNORED has correct value`() {
        assertEquals(0, Severity.IGNORED.value)
    }

    @Test
    fun `Severity_NOTE has correct value`() {
        assertEquals(1, Severity.NOTE.value)
    }

    @Test
    fun `Severity_WARNING has correct value`() {
        assertEquals(2, Severity.WARNING.value)
    }

    @Test
    fun `Severity_ERROR has correct value`() {
        assertEquals(3, Severity.ERROR.value)
    }

    @Test
    fun `Severity_FATAL has correct value`() {
        assertEquals(4, Severity.FATAL.value)
    }

    @Test
    fun `fromValue returns correct Severity for all values`() {
        assertEquals(Severity.IGNORED, Severity.fromValue(0))
        assertEquals(Severity.NOTE, Severity.fromValue(1))
        assertEquals(Severity.WARNING, Severity.fromValue(2))
        assertEquals(Severity.ERROR, Severity.fromValue(3))
        assertEquals(Severity.FATAL, Severity.fromValue(4))
    }

    @Test
    fun `fromValue returns UNKNOWN for unknown values`() {
        assertEquals(Severity.UNKNOWN, Severity.fromValue(5))
        assertEquals(Severity.UNKNOWN, Severity.fromValue(-1))
        assertEquals(Severity.UNKNOWN, Severity.fromValue(100))
    }

    @Test
    fun `isError returns true for error and fatal`() {
        assertEquals(true, Severity.ERROR.isError)
        assertEquals(true, Severity.FATAL.isError)
    }

    @Test
    fun `isError returns false for non-errors`() {
        assertEquals(false, Severity.IGNORED.isError)
        assertEquals(false, Severity.NOTE.isError)
        assertEquals(false, Severity.WARNING.isError)
    }

    @Test
    fun `isWarningOrWorse returns true for warning and above`() {
        assertEquals(true, Severity.WARNING.isWarningOrWorse)
        assertEquals(true, Severity.ERROR.isWarningOrWorse)
        assertEquals(true, Severity.FATAL.isWarningOrWorse)
    }

    @Test
    fun `isWarningOrWorse returns false for below warning`() {
        assertEquals(false, Severity.IGNORED.isWarningOrWorse)
        assertEquals(false, Severity.NOTE.isWarningOrWorse)
    }

    @Test
    fun `severity order is correct`() {
        assertEquals(true, Severity.IGNORED < Severity.NOTE)
        assertEquals(true, Severity.NOTE < Severity.WARNING)
        assertEquals(true, Severity.WARNING < Severity.ERROR)
        assertEquals(true, Severity.ERROR < Severity.FATAL)
    }
}
