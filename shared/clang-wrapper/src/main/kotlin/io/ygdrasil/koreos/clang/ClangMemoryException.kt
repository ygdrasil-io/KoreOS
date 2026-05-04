// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

/**
 * Exception thrown for memory-related errors.
 */
class ClangMemoryException @JvmOverloads constructor(
    message: String,
    cause: Throwable? = null
) : ClangException(message, cause)
