// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

/**
 * Exception thrown when parsing fails.
 */
class ClangParsingException @JvmOverloads constructor(
    message: String,
    cause: Throwable? = null
) : ClangException(message, cause)
