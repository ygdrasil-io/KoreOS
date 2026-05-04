// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

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
object ExampleUsage {

    /**
     * Main entry point for the example.
     * @param args Command line arguments (optional: path to C file)
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val cFilePath: String = if (args.isNotEmpty()) {
            args[0]
        } else {
            // Extract example.c from resources and save to temp file
            extractExampleCFile()
        }

        println("Parsing C file: $cFilePath")
        
        try {
            // Step 1: Initialize the FFM wrapper
            println("\n1. Initializing Clang FFM wrapper...")
            ClangFFMWrapper.initialize()
            println("   ✓ Wrapper initialized")

            // Step 2: Create a Clang index
            println("\n2. Creating Clang index...")
            ClangIndex(false, true).use { index ->
                println("   ✓ Index created")

                // Step 3: Parse the C file
                println("\n3. Parsing translation unit...")
                val clangArgs = arrayOf("-std=c17", "-Wall")
                index.parseTranslationUnit(cFilePath, clangArgs).use { tu ->
                    println("   ✓ Translation unit parsed")

                    // Step 4: Get diagnostics
                    println("\n4. Checking diagnostics...")
                    val numDiagnostics = tu.getNumDiagnostics()
                    if (numDiagnostics >= 0) {
                        println("   Number of diagnostics: $numDiagnostics")
                    } else {
                        println("   Diagnostics not supported in this libclang version")
                    }

                    println("\n✓ Example completed successfully!")
                }
            }

        } catch (e: ClangInitializationException) {
            System.err.println("Initialization failed: " + e.message)
            System.err.println("Ensure Java 21+ and LLVM/Clang 17+ are installed.")
            System.exit(1)
        } catch (e: ClangParsingException) {
            System.err.println("Parsing failed: " + e.message)
            System.exit(1)
        } catch (e: ClangMemoryException) {
            System.err.println("Memory error: " + e.message)
            System.exit(1)
        } catch (e: Exception) {
            System.err.println("Unexpected error: " + e.message)
            e.printStackTrace()
            System.exit(1)
        } finally {
            // Clean up resources
            println("\n5. Cleaning up...")
            if (cFilePath.startsWith("/tmp/")) {
                try {
                    Files.deleteIfExists(Path.of(cFilePath))
                    println("   ✓ Temporary file deleted")
                } catch (e: Exception) {
                    System.err.println("   Warning: Failed to delete temp file: " + e.message)
                }
            }
        }
    }

    /**
     * Extracts the example.c file from resources to a temporary location.
     * @return Path to the extracted C file
     */
    private fun extractExampleCFile(): String {
        return try {
            val tempFile = Files.createTempFile("clang-wrapper-example-", ".c")
            ExampleUsage::class.java.getResourceAsStream("/examples/example.c").use { inputStream ->
                if (inputStream == null) {
                    throw IOException("example.c not found in resources")
                }
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING)
            }
            tempFile.toString()
        } catch (e: Exception) {
            System.err.println("Failed to extract example C file: " + e.message)
            System.exit(1)
            "" // Unreachable but required
        }
    }
}
