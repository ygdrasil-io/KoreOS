// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for high-level wrapper classes (ClangIndex, ClangTranslationUnit, etc.).
 */
public class HighLevelWrapperTest {

    @BeforeEach
    public void setUp() {
        ClangFFMWrapper.reset();
    }

    @AfterEach
    public void tearDown() {
        ClangFFMWrapper.reset();
    }

    // ==================== ClangIndex Tests ====================

    @Test
    public void testClangIndexWithoutInit() {
        assertThrows(ClangInitializationException.class, () -> {
            new ClangIndex();
        });
    }

    // ==================== ClangTranslationUnit Tests ====================

    @Test
    public void testClangTranslationUnitNullHandle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ClangTranslationUnit(null);
        });
    }

    // ==================== ClangCursor Tests ====================

    @Test
    public void testClangCursorNullHandle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ClangCursor(null);
        });
    }

    // ==================== ClangDiagnostic Tests ====================

    @Test
    public void testClangDiagnosticNullHandle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ClangDiagnostic(null);
        });
    }

    // ==================== AutoCloseable Tests ====================

    @Test
    public void testClangIndexIsCloseable() {
        assertTrue(ClangIndex.class.getInterfaces().length > 0);
        // Check that AutoCloseable is implemented
        assertTrue(AutoCloseable.class.isAssignableFrom(ClangIndex.class));
    }

    @Test
    public void testClangTranslationUnitIsCloseable() {
        assertTrue(AutoCloseable.class.isAssignableFrom(ClangTranslationUnit.class));
    }
}
