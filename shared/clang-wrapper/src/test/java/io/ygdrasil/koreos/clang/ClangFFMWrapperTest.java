// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.foreign.MemorySegment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ClangFFMWrapper.
 * Requires LLVM/Clang 17+ and Java 21+.
 */
public class ClangFFMWrapperTest {

    @BeforeEach
    public void setUp() {
        // Reset state before each test
        ClangFFMWrapper.reset();
    }

    @AfterEach
    public void tearDown() {
        ClangFFMWrapper.reset();
    }

    // ==================== Initialization Tests ====================

    @Test
    public void testIsFFMSupported() {
        assertTrue(ClangFFMWrapper.isFFMSupported(), 
            "FFM should be supported on Java 21+");
    }

    @Test
    public void testIsNotInitializedInitially() {
        assertFalse(ClangFFMWrapper.isInitialized());
    }

    @Test
    public void testInitialize() {
        assertDoesNotThrow(() -> {
            ClangFFMWrapper.initialize();
            assertTrue(ClangFFMWrapper.isInitialized());
        });
    }

    @Test
    public void testInitializeIdempotent() {
        assertDoesNotThrow(() -> {
            ClangFFMWrapper.initialize();
            ClangFFMWrapper.initialize(); // Should not throw
            assertTrue(ClangFFMWrapper.isInitialized());
        });
    }

    @Test
    public void testCreateIndexWithoutInit() {
        assertThrows(ClangInitializationException.class, () -> {
            ClangFFMWrapper.createIndex(false, false);
        });
    }

    // ==================== Index Creation Tests ====================

    @Test
    public void testCreateIndex() {
        ClangFFMWrapper.initialize();

        MemorySegment index = ClangFFMWrapper.createIndex(false, false);
        try {
            assertNotNull(index);
            assertFalse(index.equals(MemorySegment.NULL));
        } finally {
            ClangFFMWrapper.disposeIndex(index);
        }
    }

    @Test
    public void testCreateIndexWithPCHExcluded() {
        ClangFFMWrapper.initialize();

        assertDoesNotThrow(() -> {
            MemorySegment index = ClangFFMWrapper.createIndex(true, false);
            assertNotNull(index);
            ClangFFMWrapper.disposeIndex(index);
        });
    }

    @Test
    public void testCreateIndexWithDiagnosticsDisplayed() {
        ClangFFMWrapper.initialize();

        assertDoesNotThrow(() -> {
            MemorySegment index = ClangFFMWrapper.createIndex(false, true);
            assertNotNull(index);
            ClangFFMWrapper.disposeIndex(index);
        });
    }

    // ==================== Disposal Tests ====================

    @Test
    public void testDisposeIndex() {
        ClangFFMWrapper.initialize();

        assertDoesNotThrow(() -> {
            MemorySegment index = ClangFFMWrapper.createIndex(false, false);
            ClangFFMWrapper.disposeIndex(index);
            // Note: Double dispose will cause native crash - this is expected behavior
            // Use high-level ClangIndex wrapper for automatic lifecycle management
        });
    }

    @Test
    public void testDisposeNullIndex() {
        ClangFFMWrapper.initialize();

        assertDoesNotThrow(() -> {
            ClangFFMWrapper.disposeIndex(null);
            ClangFFMWrapper.disposeIndex(MemorySegment.NULL);
        });
    }

    // ==================== Translation Unit Tests ====================

    @Test
    public void testParseTranslationUnitWithNullIndex() {
        ClangFFMWrapper.initialize();

        assertThrows(ClangMemoryException.class, () -> {
            ClangFFMWrapper.parseTranslationUnit(null, "dummy.c", new String[0]);
        });
    }

    @Test
    public void testParseTranslationUnitWithNullFilePath() {
        ClangFFMWrapper.initialize();
        MemorySegment index = ClangFFMWrapper.createIndex(false, false);

        assertThrows(ClangParsingException.class, () -> {
            ClangFFMWrapper.parseTranslationUnit(index, null, new String[0]);
        });

        ClangFFMWrapper.disposeIndex(index);
    }

    @Test
    public void testParseTranslationUnitWithEmptyFilePath() {
        ClangFFMWrapper.initialize();
        MemorySegment index = ClangFFMWrapper.createIndex(false, false);

        assertThrows(ClangParsingException.class, () -> {
            ClangFFMWrapper.parseTranslationUnit(index, "", new String[0]);
        });

        ClangFFMWrapper.disposeIndex(index);
    }

    @Test
    public void testDisposeTranslationUnit() {
        ClangFFMWrapper.initialize();

        assertDoesNotThrow(() -> {
            MemorySegment index = ClangFFMWrapper.createIndex(false, false);
            // Note: This will fail if libclang is not properly configured,
            // but we test that dispose doesn't throw with null
            ClangFFMWrapper.disposeTranslationUnit(null);
            ClangFFMWrapper.disposeTranslationUnit(MemorySegment.NULL);
            ClangFFMWrapper.disposeIndex(index);
        });
    }

    // ==================== Memory Validation Tests ====================

    @Test
    public void testMemoryValidation() {
        ClangFFMWrapper.initialize();

        // Test that createIndex returns non-null
        assertDoesNotThrow(() -> {
            MemorySegment index = ClangFFMWrapper.createIndex(false, false);
            assertNotNull(index);
            ClangFFMWrapper.disposeIndex(index);
        });
    }

    // ==================== GetNumDiagnostics Tests ====================

    @Test
    public void testGetNumDiagnosticsWithNull() {
        ClangFFMWrapper.initialize();

        assertEquals(-1, ClangFFMWrapper.getNumDiagnostics(null));
        assertEquals(-1, ClangFFMWrapper.getNumDiagnostics(MemorySegment.NULL));
    }

    // ==================== Reset Tests ====================

    @Test
    public void testReset() {
        ClangFFMWrapper.initialize();
        assertTrue(ClangFFMWrapper.isInitialized());

        ClangFFMWrapper.reset();
        assertFalse(ClangFFMWrapper.isInitialized());
    }

    @Test
    public void testOperationsAfterReset() {
        ClangFFMWrapper.initialize();
        ClangFFMWrapper.reset();

        assertThrows(ClangInitializationException.class, () -> {
            ClangFFMWrapper.createIndex(false, false);
        });
    }
}
