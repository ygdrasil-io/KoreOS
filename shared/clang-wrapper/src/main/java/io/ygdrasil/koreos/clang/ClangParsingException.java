// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.clang;

/**
 * Exception thrown when parsing fails.
 */
public class ClangParsingException extends ClangException {
    public ClangParsingException(String message) {
        super(message);
    }

    public ClangParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
