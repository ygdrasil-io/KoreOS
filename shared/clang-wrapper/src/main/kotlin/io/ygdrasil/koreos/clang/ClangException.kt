// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

/**
 * Base exception for all Clang wrapper errors.
 */
open class ClangException @JvmOverloads constructor(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
