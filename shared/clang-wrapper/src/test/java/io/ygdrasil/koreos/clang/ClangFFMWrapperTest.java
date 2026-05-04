// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import org.junit.jupiter.api.Test;
import java.lang.foreign.MemorySegment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test for ClangFFMWrapper.
 * Requires LLVM/Clang 17+ and Java 21+
 */
public class ClangFFMWrapperTest {

    @Test
    public void testInitialize() {
        assertDoesNotThrow(() -> {
            ClangFFMWrapper.initialize();
        });
    }

    @Test
    public void testCreateIndex() {
        ClangFFMWrapper.initialize();

        assertDoesNotThrow(() -> {
            MemorySegment index = ClangFFMWrapper.createIndex(false, false);
            assertNotNull(index);
        });
    }

    @Test
    public void testCleanup() {
        assertDoesNotThrow(() -> {
            ClangFFMWrapper.cleanup();
        });
    }
}