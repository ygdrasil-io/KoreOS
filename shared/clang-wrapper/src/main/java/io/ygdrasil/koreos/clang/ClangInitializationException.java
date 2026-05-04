// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

/**
 * Exception thrown when libclang initialization fails.
 */
public class ClangInitializationException extends ClangException {
    public ClangInitializationException(String message) {
        super(message);
    }

    public ClangInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
