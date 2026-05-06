// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

/**
 * Enum representing Clang cursor kinds.
 * These values correspond to the CXCursorKind enum in libclang.
 *
 * See: https://clang.llvm.org/doxygen/group__CINDEX__CURSOR__KIND.html
 */
enum class CursorKind(val value: Int) {
    // Cursor kinds for declarations
    UNEXPOSED_DECL(1),
    STRUCT_DECL(19),
    UNION_DECL(20),
    CLASS_DECL(21),
    ENUM_DECL(22),
    FIELD_DECL(23),
    ENUM_CONSTANT_DECL(24),
    FUNCTION_DECL(29),
    VAR_DECL(30),
    PARM_DECL(31),
    OBJ_CATEGORY_IMPL_DECL(32),
    CLASS_TEMPLATE(33),

    // Cursor kinds for references
    TYPE_REF(34),
    OBJECT_REF(35),
    BLOCK_REF(36),

    // Cursor kinds for expressions
    INTEGER_LITERAL(100),
    FLOATING_LITERAL(101),
    IMAGINARY_LITERAL(102),
    STRING_LITERAL(103),
    CHARACTER_LITERAL(104),
    PAREN_EXPR(105),
    UNARY_OPERATOR(106),
    ARRAY_SUBSCRIPT_EXPR(107),
    BINARY_OPERATOR(108),
    COMPOUND_ASSIGNMENT_OPERATOR(109),
    CONDITIONAL_OPERATOR(110),
    CSIZEOF_EXPR(111),
    CALL_EXPR(112),
    MEMBER_REF_EXPR(113),

    // Cursor kinds for statements
    COMPOUND_STMT(200),
    CASE_STMT(201),
    DEFAULT_STMT(202),
    IF_STMT(203),
    SWITCH_STMT(204),
    WHILE_STMT(205),
    DO_STMT(206),
    FOR_STMT(207),
    GOTO_STMT(208),
    INDIRECT_GOTO_STMT(209),
    CONTINUE_STMT(210),
    BREAK_STMT(211),
    RETURN_STMT(212),

    // Translation unit
    TRANSLATION_UNIT(0),

    // Preprocessing
    INCLUSION_DIRECTIVE(300),

    // Unknown/Invalid
    UNKNOWN(-1);

    /**
     * Check if this cursor kind represents a declaration.
     */
    val isDeclaration: Boolean
        get() = when (this) {
            UNEXPOSED_DECL,
            STRUCT_DECL,
            UNION_DECL,
            CLASS_DECL,
            ENUM_DECL,
            FIELD_DECL,
            ENUM_CONSTANT_DECL,
            FUNCTION_DECL,
            VAR_DECL,
            PARM_DECL,
            OBJ_CATEGORY_IMPL_DECL,
            CLASS_TEMPLATE -> true
            else -> false
        }

    /**
     * Check if this cursor kind represents an expression.
     */
    val isExpression: Boolean
        get() = when (this) {
            INTEGER_LITERAL,
            FLOATING_LITERAL,
            IMAGINARY_LITERAL,
            STRING_LITERAL,
            CHARACTER_LITERAL,
            PAREN_EXPR,
            UNARY_OPERATOR,
            ARRAY_SUBSCRIPT_EXPR,
            BINARY_OPERATOR,
            COMPOUND_ASSIGNMENT_OPERATOR,
            CONDITIONAL_OPERATOR,
            CSIZEOF_EXPR,
            CALL_EXPR,
            MEMBER_REF_EXPR -> true
            else -> false
        }

    /**
     * Check if this cursor kind represents a statement.
     */
    val isStatement: Boolean
        get() = when (this) {
            COMPOUND_STMT,
            CASE_STMT,
            DEFAULT_STMT,
            IF_STMT,
            SWITCH_STMT,
            WHILE_STMT,
            DO_STMT,
            FOR_STMT,
            GOTO_STMT,
            INDIRECT_GOTO_STMT,
            CONTINUE_STMT,
            BREAK_STMT,
            RETURN_STMT -> true
            else -> false
        }

    companion object {
        /**
         * Get CursorKind from its integer value.
         * Returns UNKNOWN if the value is not recognized.
         */
        fun fromValue(value: Int): CursorKind {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}
