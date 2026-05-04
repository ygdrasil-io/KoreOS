// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import java.lang.foreign.MemorySegment;

/**
 * High-level wrapper for a Clang CXIndex.
 * Represents a Clang index object used for parsing source code.
 */
public final class ClangIndex implements AutoCloseable {

    private final MemorySegment handle;
    private boolean disposed = false;

    /**
     * Creates a new Clang index.
     * @param excludeDeclsFromPCH Whether to exclude declarations from precompiled headers
     * @param displayDiagnostics Whether to display diagnostics
     * @throws ClangInitializationException if ClangFFMWrapper is not initialized
     * @throws ClangMemoryException if index creation fails
     */
    public ClangIndex(boolean excludeDeclsFromPCH, boolean displayDiagnostics) {
        ClangFFMWrapper.ensureInitialized();
        this.handle = ClangFFMWrapper.createIndex(excludeDeclsFromPCH, displayDiagnostics);
    }

    /**
     * Creates a new Clang index with default settings.
     */
    public ClangIndex() {
        this(false, false);
    }

    /**
     * Parses a translation unit (source file).
     * @param sourceFilePath Path to the source file
     * @param commandLineArgs Command line arguments for the parser
     * @return A new ClangTranslationUnit
     * @throws ClangParsingException if parsing fails
     * @throws ClangMemoryException if result is NULL
     */
    public ClangTranslationUnit parseTranslationUnit(String sourceFilePath, String[] commandLineArgs) {
        MemorySegment tu = ClangFFMWrapper.parseTranslationUnit(handle, sourceFilePath, commandLineArgs);
        return new ClangTranslationUnit(tu);
    }

    /**
     * Parses a translation unit with default command line arguments.
     * @param sourceFilePath Path to the source file
     * @return A new ClangTranslationUnit
     */
    public ClangTranslationUnit parseTranslationUnit(String sourceFilePath) {
        return parseTranslationUnit(sourceFilePath, new String[0]);
    }

    /**
     * Checks if this index has been disposed.
     * @return true if disposed
     */
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * Gets the underlying MemorySegment handle.
     * For advanced use only.
     * @return The MemorySegment handle
     */
    public MemorySegment getHandle() {
        return handle;
    }

    /**
     * Disposes this index and releases associated resources.
     */
    @Override
    public void close() {
        if (disposed) {
            return;
        }
        ClangFFMWrapper.disposeIndex(handle);
        disposed = true;
    }

    /**
     * Disposes this index.
     */
    public void dispose() {
        close();
    }


}
