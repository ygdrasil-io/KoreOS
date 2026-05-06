// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

/**
 * Enum representing Clang diagnostic severities.
 * These values correspond to the CXDiagnosticSeverity enum in libclang.
 *
 * See: https://clang.llvm.org/doxygen/group__CINDEX__DIAG.html
 */
enum class Severity(val value: Int) {
    IGNORED(0),
    NOTE(1),
    WARNING(2),
    ERROR(3),
    FATAL(4),
    UNKNOWN(-1);

    /**
     * Check if this severity represents an error.
     */
    val isError: Boolean
        get() = this == ERROR || this == FATAL

    /**
     * Check if this severity is warning or worse (warning, error, fatal).
     */
    val isWarningOrWorse: Boolean
        get() = this == WARNING || this == ERROR || this == FATAL

    companion object {
        /**
         * Get Severity from its integer value.
         * Returns UNKNOWN if the value is not recognized.
         */
        fun fromValue(value: Int): Severity {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}
