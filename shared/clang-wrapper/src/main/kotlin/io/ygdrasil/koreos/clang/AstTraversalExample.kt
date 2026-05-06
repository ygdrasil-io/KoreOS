// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import java.nio.file.Paths

/**
 * Example demonstrating AST traversal using the Clang wrapper.
 *
 * Part of GRA-4: Support des types complexes de Clang (AST, diagnostics)
 */
object AstTraversalExample {

    /**
     * Example: Parse a C file and print all function declarations.
     */
    fun printAllFunctions(filePath: String) {
        // Initialize the wrapper
        ClangFFMWrapper.initialize()

        // Create an index
        ClangIndex().use { index ->
            // Parse the translation unit
            val tu = index.parseTranslationUnit(filePath)

            // Get the root cursor
            val rootCursor = tu.cursor

            // Find all function declarations
            val functions = rootCursor.findAll(CursorKind.FUNCTION_DECL)

            println("Functions found in ${Paths.get(filePath).fileName}:")
            for (func in functions) {
                val name = func.getSpelling()
                val location = func.getLocation()
                println("  - $name at $location")
            }

            // Print some statistics
            println("\nAST Statistics:")
            println("  Total functions: ${functions.size}")

            // Count all cursor kinds
            val kindCounts = mutableMapOf<CursorKind, Int>()
            rootCursor.traverse { cursor ->
                val kind = cursor.getKind()
                kindCounts[kind] = kindCounts.getOrDefault(kind, 0) + 1
                true
            }

            println("\nCursor kinds found:")
            for ((kind, count) in kindCounts.entries.sortedByDescending { it.value }) {
                if (kind != CursorKind.UNKNOWN) {
                    println("  ${kind.name}: $count")
                }
            }
        }
    }

    /**
     * Example: Print all diagnostics (errors, warnings) in a file.
     */
    fun printAllDiagnostics(filePath: String) {
        ClangFFMWrapper.initialize()

        ClangIndex().use { index ->
            val tu = index.parseTranslationUnit(filePath)

            val diagnostics = tu.getDiagnostics()

            if (diagnostics.isEmpty()) {
                println("No diagnostics found in $filePath")
                return
            }

            println("Diagnostics for ${Paths.get(filePath).fileName}:")
            for (diag in diagnostics) {
                val severity = diag.getSeverity()
                val message = diag.getMessage()
                val location = diag.getLocation()
                println("  [$severity] $message at $location")
            }
        }
    }

    /**
     * Example: Extract the AST structure as a tree.
     */
    fun printAstTree(filePath: String, maxDepth: Int = 10) {
        ClangFFMWrapper.initialize()

        ClangIndex().use { index ->
            val tu = index.parseTranslationUnit(filePath)

            println("AST Tree for ${Paths.get(filePath).fileName}:")
            printCursorTree(tu.cursor, 0, maxDepth)
        }
    }

    /**
     * Recursively print the cursor tree.
     */
    private fun printCursorTree(cursor: ClangCursor, depth: Int, maxDepth: Int) {
        if (depth > maxDepth) return

        val indent = "  ".repeat(depth)
        val kind = cursor.getKind()
        val spelling = cursor.getSpelling()
        val name = if (spelling.isNotEmpty()) " ($spelling)" else ""

        println("$indent${kind.name}$name")

        for (child in cursor.getChildren()) {
            printCursorTree(child, depth + 1, maxDepth)
        }
    }
}
