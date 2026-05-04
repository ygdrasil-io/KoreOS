// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

/**
 * Exception thrown for memory-related errors.
 */
public class ClangMemoryException extends ClangException {
    public ClangMemoryException(String message) {
        super(message);
    }

    public ClangMemoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
