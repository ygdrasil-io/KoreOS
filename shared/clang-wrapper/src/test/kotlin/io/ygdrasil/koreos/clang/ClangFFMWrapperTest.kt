// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.foreign.MemorySegment

import org.junit.jupiter.api.Assertions.*

/**
 * Tests for ClangFFMWrapper.
 * Requires LLVM/Clang 17+ and Java 21+.
 */
class ClangFFMWrapperTest {

    @BeforeEach
    fun setUp() {
        // Reset state before each test
        ClangFFMWrapper.reset()
    }

    @AfterEach
    fun tearDown() {
        ClangFFMWrapper.reset()
    }

    // ==================== Initialization Tests ====================

    @Test
    fun testIsFFMSupported() {
        assertTrue(ClangFFMWrapper.isFFMSupported(), 
            "FFM should be supported on Java 21+")
    }

    @Test
    fun testIsNotInitializedInitially() {
        assertFalse(ClangFFMWrapper.isInitialized())
    }

    @Test
    fun testInitialize() {
        assertDoesNotThrow {
            ClangFFMWrapper.initialize()
            assertTrue(ClangFFMWrapper.isInitialized())
        }
    }

    @Test
    fun testInitializeIdempotent() {
        assertDoesNotThrow {
            ClangFFMWrapper.initialize()
            ClangFFMWrapper.initialize() // Should not throw
            assertTrue(ClangFFMWrapper.isInitialized())
        }
    }

    @Test
    fun testCreateIndexWithoutInit() {
        assertThrows<ClangInitializationException> {
            ClangFFMWrapper.createIndex(false, false)
        }
    }

    // ==================== Index Creation Tests ====================

    @Test
    fun testCreateIndex() {
        ClangFFMWrapper.initialize()

        val index = ClangFFMWrapper.createIndex(false, false)
        try {
            assertNotNull(index)
            assertFalse(index == MemorySegment.NULL)
        } finally {
            ClangFFMWrapper.disposeIndex(index)
        }
    }

    @Test
    fun testCreateIndexWithPCHExcluded() {
        ClangFFMWrapper.initialize()

        assertDoesNotThrow {
            val index = ClangFFMWrapper.createIndex(true, false)
            assertNotNull(index)
            ClangFFMWrapper.disposeIndex(index)
        }
    }

    @Test
    fun testCreateIndexWithDiagnosticsDisplayed() {
        ClangFFMWrapper.initialize()

        assertDoesNotThrow {
            val index = ClangFFMWrapper.createIndex(false, true)
            assertNotNull(index)
            ClangFFMWrapper.disposeIndex(index)
        }
    }

    // ==================== Disposal Tests ====================

    @Test
    fun testDisposeIndex() {
        ClangFFMWrapper.initialize()

        assertDoesNotThrow {
            val index = ClangFFMWrapper.createIndex(false, false)
            ClangFFMWrapper.disposeIndex(index)
            // Note: Double dispose will cause native crash - this is expected behavior
            // Use high-level ClangIndex wrapper for automatic lifecycle management
        }
    }

    @Test
    fun testDisposeNullIndex() {
        ClangFFMWrapper.initialize()

        assertDoesNotThrow {
            ClangFFMWrapper.disposeIndex(null)
            ClangFFMWrapper.disposeIndex(MemorySegment.NULL)
        }
    }

    // ==================== Translation Unit Tests ====================

    @Test
    fun testParseTranslationUnitWithNullIndex() {
        ClangFFMWrapper.initialize()

        assertThrows<ClangMemoryException> {
            ClangFFMWrapper.parseTranslationUnit(null, "dummy.c", emptyArray())
        }
    }

    @Test
    fun testParseTranslationUnitWithNullFilePath() {
        ClangFFMWrapper.initialize()
        val index = ClangFFMWrapper.createIndex(false, false)

        assertThrows<ClangParsingException> {
            ClangFFMWrapper.parseTranslationUnit(index, null, emptyArray())
        }

        ClangFFMWrapper.disposeIndex(index)
    }

    @Test
    fun testParseTranslationUnitWithEmptyFilePath() {
        ClangFFMWrapper.initialize()
        val index = ClangFFMWrapper.createIndex(false, false)

        assertThrows<ClangParsingException> {
            ClangFFMWrapper.parseTranslationUnit(index, "", emptyArray())
        }

        ClangFFMWrapper.disposeIndex(index)
    }

    @Test
    fun testDisposeTranslationUnit() {
        ClangFFMWrapper.initialize()

        assertDoesNotThrow {
            val index = ClangFFMWrapper.createIndex(false, false)
            // Note: This will fail if libclang is not properly configured,
            // but we test that dispose doesn't throw with null
            ClangFFMWrapper.disposeTranslationUnit(null)
            ClangFFMWrapper.disposeTranslationUnit(MemorySegment.NULL)
            ClangFFMWrapper.disposeIndex(index)
        }
    }

    // ==================== Memory Validation Tests ====================

    @Test
    fun testMemoryValidation() {
        ClangFFMWrapper.initialize()

        // Test that createIndex returns non-null
        assertDoesNotThrow {
            val index = ClangFFMWrapper.createIndex(false, false)
            assertNotNull(index)
            ClangFFMWrapper.disposeIndex(index)
        }
    }

    // ==================== GetNumDiagnostics Tests ====================

    @Test
    fun testGetNumDiagnosticsWithNull() {
        ClangFFMWrapper.initialize()

        assertEquals(-1, ClangFFMWrapper.getNumDiagnostics(null))
        assertEquals(-1, ClangFFMWrapper.getNumDiagnostics(MemorySegment.NULL))
    }

    // ==================== Reset Tests ====================

    @Test
    fun testReset() {
        ClangFFMWrapper.initialize()
        assertTrue(ClangFFMWrapper.isInitialized())

        ClangFFMWrapper.reset()
        assertFalse(ClangFFMWrapper.isInitialized())
    }

    @Test
    fun testOperationsAfterReset() {
        ClangFFMWrapper.initialize()
        ClangFFMWrapper.reset()

        assertThrows<ClangInitializationException> {
            ClangFFMWrapper.createIndex(false, false)
        }
    }
}
