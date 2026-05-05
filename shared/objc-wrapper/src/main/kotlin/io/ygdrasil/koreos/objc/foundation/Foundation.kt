// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc.foundation

/**
 * Package-level functions and constants for Foundation framework.
 * This module provides Kotlin wrappers for common Foundation classes.
 */

/**
 * Initialize all Foundation wrappers.
 * This should be called before using any Foundation classes.
 */
fun initializeFoundation() {
    // Initialize the Objective-C runtime
    io.ygdrasil.koreos.objc.ObjectiveCRuntime.initialize()
    
    // Pre-register common Foundation selectors
    val runtime = io.ygdrasil.koreos.objc.ObjectiveCRuntime
    runtime.registerSelector("alloc")
    runtime.registerSelector("init")
    runtime.registerSelector("new")
    runtime.registerSelector("release")
    runtime.registerSelector("retain")
    runtime.registerSelector("autorelease")
    runtime.registerSelector("description")
    runtime.registerSelector("UTF8String")
    runtime.registerSelector("count")
    runtime.registerSelector("objectAtIndex:")
    runtime.registerSelector("length")
    runtime.registerSelector("intValue")
    runtime.registerSelector("longValue")
    runtime.registerSelector("floatValue")
    runtime.registerSelector("doubleValue")
    runtime.registerSelector("boolValue")
    runtime.registerSelector("charValue")
    runtime.registerSelector("stringValue")
    runtime.registerSelector("isEqualToString:")
    runtime.registerSelector("containsString:")
    runtime.registerSelector("hasPrefix:")
    runtime.registerSelector("hasSuffix:")
    runtime.registerSelector("uppercaseString")
    runtime.registerSelector("lowercaseString")
    runtime.registerSelector("substringFromIndex:")
    runtime.registerSelector("stringByAppendingString:")
    runtime.registerSelector("characterAtIndex:")
    runtime.registerSelector("isEqual:")
    runtime.registerSelector("isEqualToNumber:")
    runtime.registerSelector("compare:")
    runtime.registerSelector("containsObject:")
    runtime.registerSelector("indexOfObject:")
    runtime.registerSelector("firstObject")
    runtime.registerSelector("lastObject")
    runtime.registerSelector("subarrayWithRange:")
    runtime.registerSelector("array")
    runtime.registerSelector("arrayWithObject:")
    runtime.registerSelector("arrayWithObjects:count:")
    runtime.registerSelector("numberWithInt:")
    runtime.registerSelector("numberWithLong:")
    runtime.registerSelector("numberWithFloat:")
    runtime.registerSelector("numberWithDouble:")
    runtime.registerSelector("numberWithBool:")
    runtime.registerSelector("numberWithChar:")
    runtime.registerSelector("stringWithUTF8String:")
    runtime.registerSelector("stringWithFormat:")
}

/**
 * Check if Foundation wrappers are available.
 */
fun isFoundationAvailable(): Boolean {
    return io.ygdrasil.koreos.objc.ObjectiveCRuntime.isInitialized()
}
