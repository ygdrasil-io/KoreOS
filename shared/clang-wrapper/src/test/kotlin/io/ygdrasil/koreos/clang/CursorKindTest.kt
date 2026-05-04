// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for CursorKind enum.
 * Part of GRA-4: Support des types complexes de Clang (AST, diagnostics)
 */
class CursorKindTest {

    @Test
    fun `CursorKind_UNEXPOSED_DECL has correct value`() {
        assertEquals(1, CursorKind.UNEXPOSED_DECL.value)
    }

    @Test
    fun `CursorKind_STRUCT_DECL has correct value`() {
        assertEquals(19, CursorKind.STRUCT_DECL.value)
    }

    @Test
    fun `CursorKind_UNION_DECL has correct value`() {
        assertEquals(20, CursorKind.UNION_DECL.value)
    }

    @Test
    fun `CursorKind_CLASS_DECL has correct value`() {
        assertEquals(21, CursorKind.CLASS_DECL.value)
    }

    @Test
    fun `CursorKind_ENUM_DECL has correct value`() {
        assertEquals(22, CursorKind.ENUM_DECL.value)
    }

    @Test
    fun `CursorKind_FUNCTION_DECL has correct value`() {
        assertEquals(29, CursorKind.FUNCTION_DECL.value)
    }

    @Test
    fun `CursorKind_VAR_DECL has correct value`() {
        assertEquals(30, CursorKind.VAR_DECL.value)
    }

    @Test
    fun `CursorKind_PARM_DECL has correct value`() {
        assertEquals(31, CursorKind.PARM_DECL.value)
    }

    @Test
    fun `CursorKind_INTEGER_LITERAL has correct value`() {
        assertEquals(100, CursorKind.INTEGER_LITERAL.value)
    }

    @Test
    fun `CursorKind_STRING_LITERAL has correct value`() {
        assertEquals(103, CursorKind.STRING_LITERAL.value)
    }

    @Test
    fun `CursorKind_TRANSLATION_UNIT has correct value`() {
        assertEquals(0, CursorKind.TRANSLATION_UNIT.value)
    }

    @Test
    fun `CursorKind_INCLUSION_DIRECTIVE has correct value`() {
        assertEquals(300, CursorKind.INCLUSION_DIRECTIVE.value)
    }

    @Test
    fun `fromValue returns correct CursorKind for known values`() {
        assertEquals(CursorKind.TRANSLATION_UNIT, CursorKind.fromValue(0))
        assertEquals(CursorKind.STRUCT_DECL, CursorKind.fromValue(19))
        assertEquals(CursorKind.FUNCTION_DECL, CursorKind.fromValue(29))
        assertEquals(CursorKind.VAR_DECL, CursorKind.fromValue(30))
    }

    @Test
    fun `fromValue returns UNKNOWN for unknown values`() {
        assertEquals(CursorKind.UNKNOWN, CursorKind.fromValue(9999))
        assertEquals(CursorKind.UNKNOWN, CursorKind.fromValue(-1))
    }

    @Test
    fun `isDeclaration returns true for declaration kinds`() {
        assertEquals(true, CursorKind.STRUCT_DECL.isDeclaration)
        assertEquals(true, CursorKind.FUNCTION_DECL.isDeclaration)
        assertEquals(true, CursorKind.VAR_DECL.isDeclaration)
        assertEquals(true, CursorKind.PARM_DECL.isDeclaration)
    }

    @Test
    fun `isDeclaration returns false for non-declaration kinds`() {
        assertEquals(false, CursorKind.INTEGER_LITERAL.isDeclaration)
        assertEquals(false, CursorKind.STRING_LITERAL.isDeclaration)
        assertEquals(false, CursorKind.BINARY_OPERATOR.isDeclaration)
    }

    @Test
    fun `isExpression returns true for expression kinds`() {
        assertEquals(true, CursorKind.INTEGER_LITERAL.isExpression)
        assertEquals(true, CursorKind.STRING_LITERAL.isExpression)
        assertEquals(true, CursorKind.BINARY_OPERATOR.isExpression)
    }

    @Test
    fun `isExpression returns false for non-expression kinds`() {
        assertEquals(false, CursorKind.STRUCT_DECL.isExpression)
        assertEquals(false, CursorKind.FUNCTION_DECL.isExpression)
    }

    @Test
    fun `isStatement returns true for statement kinds`() {
        assertEquals(true, CursorKind.IF_STMT.isStatement)
        assertEquals(true, CursorKind.FOR_STMT.isStatement)
        assertEquals(true, CursorKind.RETURN_STMT.isStatement)
    }

    @Test
    fun `isStatement returns false for non-statement kinds`() {
        assertEquals(false, CursorKind.STRUCT_DECL.isStatement)
        assertEquals(false, CursorKind.INTEGER_LITERAL.isStatement)
    }
}
