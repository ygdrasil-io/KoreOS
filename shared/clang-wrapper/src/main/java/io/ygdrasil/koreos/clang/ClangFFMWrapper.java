// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/**
 * Low-level wrapper for libclang using Java 21+ FFM API.
 * Part of GRA-1: Study and initial configuration for Clang wrapper with FFM.
 */
public final class ClangFFMWrapper {

    private static MemorySegment clangIndex;
    private static MemorySegment clangLib;

    // Method handles for libclang functions
    private static MethodHandle clang_createIndex;
    private static MethodHandle clang_parseTranslationUnit;
    private static MethodHandle clang_disposeIndex;
    private static MethodHandle clang_disposeTranslationUnit;

    /**
     * Initialize the FFM bindings for libclang.
     * Must be called before any other method.
     */
    public static void initialize() {
        try {
            SymbolFinder loader = SymbolFinder.loaderLookup();
            
            // Try multiple library names for cross-platform support
            String[] libNames = {"libclang.so.17", "libclang.so", "libclang.dylib", "clang.dll"};
            for (String libName : libNames) {
                try {
                    clangLib = loader.find(libName).orElse(null);
                    if (clangLib != null) break;
                } catch (Exception e) {
                    // Try next library name
                }
            }
            
            if (clangLib == null) {
                throw new RuntimeException("Failed to load libclang library. Ensure LLVM/Clang is installed.");
            }

            // Resolve function symbols
            clang_createIndex = loader.find("clang_createIndex").orElseThrow();
            clang_parseTranslationUnit = loader.find("clang_parseTranslationUnit").orElseThrow();
            clang_disposeIndex = loader.find("clang_disposeIndex").orElseThrow();
            clang_disposeTranslationUnit = loader.find("clang_disposeTranslationUnit").orElseThrow();

            System.out.println("ClangFFMWrapper initialized successfully with libclang");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Clang FFM bindings", e);
        }
    }

    /**
     * Create a Clang index.
     *
     * @param excludeDeclsFromPCH Whether to exclude declarations from precompiled headers
     * @param displayDiagnostics Whether to display diagnostics
     * @return MemorySegment pointing to the CXIndex
     */
    public static MemorySegment createIndex(boolean excludeDeclsFromPCH, boolean displayDiagnostics) {
        if (clangLib == null) {
            initialize();
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pExclude = arena.allocate(ValueLayout.JAVA_INT, excludeDeclsFromPCH ? 1 : 0);
            MemorySegment pDisplay = arena.allocate(ValueLayout.JAVA_INT, displayDiagnostics ? 1 : 0);

            return (MemorySegment) clang_createIndex.invoke(pExclude, pDisplay);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to create Clang index", t);
        }
    }

    /**
     * Parse a translation unit (source file).
     *
     * @param index The Clang index
     * @param sourceFilePath Path to the source file
     * @param commandLineArgs Command line arguments for the parser
     * @return MemorySegment pointing to the CXTranslationUnit
     */
    public static MemorySegment parseTranslationUnit(
            MemorySegment index, String sourceFilePath, String[] commandLineArgs) {
        try (Arena arena = Arena.ofConfined()) {
            // Convert command line args to C string array
            MemorySegment argsArray = arena.allocateArray(ValueLayout.ADDRESS, commandLineArgs.length);
            for (int i = 0; i < commandLineArgs.length; i++) {
                MemorySegment arg = arena.allocateUtf8String(commandLineArgs[i]);
                argsArray.setAtIndex(ValueLayout.ADDRESS, i, arg);
            }

            MemorySegment filePath = arena.allocateUtf8String(sourceFilePath);

            return (MemorySegment) clang_parseTranslationUnit.invoke(
                index, filePath, argsArray, commandLineArgs.length, null, 0);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to parse translation unit", t);
        }
    }

    /**
     * Clean up resources.
     */
    public static void cleanup() {
        if (clangIndex != null) {
            try {
                clang_disposeIndex.invoke(clangIndex);
            } catch (Throwable t) {
                System.err.println("Failed to dispose Clang index");
            }
            clangIndex = null;
        }
    }

    private ClangFFMWrapper() {} // Prevent instantiation
}