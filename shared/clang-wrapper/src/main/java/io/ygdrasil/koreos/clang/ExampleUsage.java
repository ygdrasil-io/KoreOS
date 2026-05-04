// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Example usage of the Clang FFM wrapper.
 * Demonstrates how to:
 * 1. Initialize the wrapper
 * 2. Create a Clang index
 * 3. Parse a C source file
 * 4. Get diagnostics information
 * 5. Properly dispose resources
 * 
 * Part of GRA-2: Basic implementation of libclang wrapper with FFM.
 */
public final class ExampleUsage {

    private ExampleUsage() {}

    /**
     * Main entry point for the example.
     * @param args Command line arguments (optional: path to C file)
     */
    public static void main(String[] args) {
        String cFilePath;
        
        if (args.length > 0) {
            cFilePath = args[0];
        } else {
            // Extract example.c from resources and save to temp file
            cFilePath = extractExampleCFile();
        }

        System.out.println("Parsing C file: " + cFilePath);
        
        try {
            // Step 1: Initialize the FFM wrapper
            System.out.println("\n1. Initializing Clang FFM wrapper...");
            ClangFFMWrapper.initialize();
            System.out.println("   ✓ Wrapper initialized");

            // Step 2: Create a Clang index
            System.out.println("\n2. Creating Clang index...");
            try (ClangIndex index = new ClangIndex(false, true)) {
                System.out.println("   ✓ Index created");

                // Step 3: Parse the C file
                System.out.println("\n3. Parsing translation unit...");
                String[] clangArgs = {"-std=c17", "-Wall"};
                try (ClangTranslationUnit tu = index.parseTranslationUnit(cFilePath, clangArgs)) {
                    System.out.println("   ✓ Translation unit parsed");

                    // Step 4: Get diagnostics
                    System.out.println("\n4. Checking diagnostics...");
                    int numDiagnostics = tu.getNumDiagnostics();
                    if (numDiagnostics >= 0) {
                        System.out.println("   Number of diagnostics: " + numDiagnostics);
                    } else {
                        System.out.println("   Diagnostics not supported in this libclang version");
                    }

                    System.out.println("\n✓ Example completed successfully!");
                }
            }

        } catch (ClangInitializationException e) {
            System.err.println("Initialization failed: " + e.getMessage());
            System.err.println("Ensure Java 21+ and LLVM/Clang 17+ are installed.");
            System.exit(1);
        } catch (ClangParsingException e) {
            System.err.println("Parsing failed: " + e.getMessage());
            System.exit(1);
        } catch (ClangMemoryException e) {
            System.err.println("Memory error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Clean up resources
            System.out.println("\n5. Cleaning up...");
            if (cFilePath != null && cFilePath.startsWith("/tmp/")) {
                try {
                    Files.deleteIfExists(Path.of(cFilePath));
                    System.out.println("   ✓ Temporary file deleted");
                } catch (IOException e) {
                    System.err.println("   Warning: Failed to delete temp file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Extracts the example.c file from resources to a temporary location.
     * @return Path to the extracted C file
     */
    private static String extractExampleCFile() {
        try {
            Path tempFile = Files.createTempFile("clang-wrapper-example-", ".c");
            try (InputStream is = ExampleUsage.class.getResourceAsStream("/examples/example.c")) {
                if (is == null) {
                    throw new IOException("example.c not found in resources");
                }
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile.toString();
        } catch (IOException e) {
            System.err.println("Failed to extract example C file: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }
}
