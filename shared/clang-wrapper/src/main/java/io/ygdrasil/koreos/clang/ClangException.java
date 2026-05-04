// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

/**
 * Base exception for all Clang wrapper errors.
 */
public class ClangException extends RuntimeException {
    public ClangException(String message) {
        super(message);
    }

    public ClangException(String message, Throwable cause) {
        super(message, cause);
    }
}
