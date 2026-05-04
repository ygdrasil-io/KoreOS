// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang

/**
 * Exception thrown when libclang initialization fails.
 */
class ClangInitializationException @JvmOverloads constructor(
    message: String,
    cause: Throwable? = null
) : ClangException(message, cause)
