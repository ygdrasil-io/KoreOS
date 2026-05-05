// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc

/**
 * Exception thrown when Objective-C runtime initialization fails.
 */
class ObjCInitializationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when an Objective-C method invocation fails.
 */
class ObjCMethodInvocationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when an Objective-C class or method is not found.
 */
class ObjCNotFoundException(message: String) : RuntimeException(message)

/**
 * Exception thrown when there's a type mismatch in Objective-C method arguments or return values.
 */
class ObjCTypeMismatchException(message: String) : RuntimeException(message)
