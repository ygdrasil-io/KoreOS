// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.foreign.MemorySegment

/**
 * Tests for high-level wrapper classes (ClangIndex, ClangTranslationUnit, etc.).
 */
class HighLevelWrapperTest {

    @BeforeEach
    fun setUp() {
        ClangFFMWrapper.reset()
    }

    @AfterEach
    fun tearDown() {
        ClangFFMWrapper.reset()
    }

    // ==================== ClangIndex Tests ====================

    @Test
    fun testClangIndexWithoutInit() {
        assertThrows<ClangInitializationException> {
            ClangIndex()
        }
    }

    // ==================== ClangTranslationUnit Tests ====================

    @Test
    fun testClangTranslationUnitNullHandle() {
        assertThrows<IllegalArgumentException> {
            ClangTranslationUnit(null as MemorySegment?)
        }
    }

    // ==================== ClangCursor Tests ====================

    @Test
    fun testClangCursorNullHandle() {
        assertThrows<IllegalArgumentException> {
            ClangCursor(null as MemorySegment?)
        }
    }

    // ==================== ClangDiagnostic Tests ====================

    @Test
    fun testClangDiagnosticNullHandle() {
        assertThrows<IllegalArgumentException> {
            ClangDiagnostic(null as MemorySegment?)
        }
    }

    // ==================== AutoCloseable Tests ====================

    @Test
    fun testClangIndexIsCloseable() {
        assertTrue(ClangIndex::class.java.interfaces.size > 0)
        // Check that AutoCloseable is implemented
        assertTrue(AutoCloseable::class.java.isAssignableFrom(ClangIndex::class.java))
    }

    @Test
    fun testClangTranslationUnitIsCloseable() {
        assertTrue(AutoCloseable::class.java.isAssignableFrom(ClangTranslationUnit::class.java))
    }
}
