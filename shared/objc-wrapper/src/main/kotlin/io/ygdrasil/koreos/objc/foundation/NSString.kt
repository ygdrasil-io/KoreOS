// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc.foundation

import io.ygdrasil.koreos.objc.ObjCClass
import io.ygdrasil.koreos.objc.ObjCObject
import io.ygdrasil.koreos.objc.ObjectiveCRuntime
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.charset.StandardCharsets

/**
 * Wrapper for the Objective-C NSString class.
 * Provides methods for creating and manipulating strings.
 */
class NSString(
    /** The native handle to the NSString object */
    val handle: MemorySegment
) {
    companion object {
        private val nsStringClass: ObjCClass by lazy {
            ObjCClass.fromName("NSString")
        }
        
        /**
         * Create an NSString from a Kotlin String.
         * Uses stringWithUTF8String: selector.
         */
        @JvmStatic
        fun fromString(value: String): NSString {
            ObjectiveCRuntime.ensureInitialized()
            
            // Allocate the UTF-8 string in native memory
            val utf8String = ObjectiveCRuntime.allocateUtf8String(value)
            
            // Get the stringWithUTF8String: selector
            val selector = ObjectiveCRuntime.registerSelector("stringWithUTF8String:")
            
            // Send the message to the NSString class with MemorySegment argument
            val result = ObjectiveCRuntime.sendMessage(
                nsStringClass.handle, 
                selector,
                utf8String
            )
            
            if (result == MemorySegment.NULL) {
                throw IllegalStateException("Failed to create NSString from: $value")
            }
            
            return NSString(result)
        }
        
        /**
         * Create an empty NSString.
         */
        @JvmStatic
        fun empty(): NSString {
            return fromString("")
        }
        
        /**
         * Create an NSString with a format string.
         * Note: This is a simplified version that doesn't handle format arguments.
         * For now, it just creates a string from the format without arguments.
         */
        @JvmStatic
        fun format(format: String): NSString {
            // Simplified: just use the format string as-is
            // A full implementation would need to handle varargs with objc_msgSend
            return fromString(format)
        }
    }
    
    /**
     * Get the ObjCObject representation of this string.
     */
    fun toObjCObject(): ObjCObject = ObjCObject(handle)
    
    /**
     * Convert this NSString to a Kotlin String.
     * Uses the UTF8String method.
     */
    fun toKotlinString(): String {
        val selector = ObjectiveCRuntime.registerSelector("UTF8String")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        
        if (result == MemorySegment.NULL) {
            return ""
        }
        
        // The result is a const char*, we need to read it as a UTF-8 string
        return result.getString(0, StandardCharsets.UTF_8)
    }
    
    /**
     * Get the length of the string.
     */
    fun length(): Int {
        val selector = ObjectiveCRuntime.registerSelector("length")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        // In Objective-C, length returns an NSUInteger (unsigned long)
        // We need to extract the integer value from the MemorySegment
        return result.get(ValueLayout.ofLong(), 0).toInt()
    }
    
    /**
     * Check if the string is empty.
     */
    fun isEmpty(): Boolean = length() == 0
    
    /**
     * Get a character at a specific index.
     */
    fun charAt(index: Int): Char {
        val selector = ObjectiveCRuntime.registerSelector("characterAtIndex:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, index)
        // characterAtIndex: returns a unichar (unsigned short)
        return result.get(ValueLayout.ofChar(), 0).toInt().toChar()
    }
    
    /**
     * Concatenate this string with another string.
     */
    fun plus(other: NSString): NSString {
        val selector = ObjectiveCRuntime.registerSelector("stringByAppendingString:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, other.handle)
        return NSString(result)
    }
    
    /**
     * Check if this string is equal to another string.
     */
    fun isEqualToString(other: NSString): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("isEqualToString:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, other.handle)
        // isEqualToString: returns a BOOL (signed char)
        return result.get(ValueLayout.ofByte(), 0) != 0.toByte()
    }
    
    /**
     * Check if this string contains a substring.
     */
    fun contains(substring: NSString): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("containsString:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, substring.handle)
        return result.get(ValueLayout.ofByte(), 0) != 0.toByte()
    }
    
    /**
     * Check if this string has a prefix.
     */
    fun hasPrefix(prefix: NSString): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("hasPrefix:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, prefix.handle)
        return result.get(ValueLayout.ofByte(), 0) != 0.toByte()
    }
    
    /**
     * Check if this string has a suffix.
     */
    fun hasSuffix(suffix: NSString): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("hasSuffix:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, suffix.handle)
        return result.get(ValueLayout.ofByte(), 0) != 0.toByte()
    }
    
    /**
     * Convert to uppercase.
     */
    fun uppercaseString(): NSString {
        val selector = ObjectiveCRuntime.registerSelector("uppercaseString")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        return NSString(result)
    }
    
    /**
     * Convert to lowercase.
     */
    fun lowercaseString(): NSString {
        val selector = ObjectiveCRuntime.registerSelector("lowercaseString")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        return NSString(result)
    }
    
    /**
     * Get a substring from a range.
     * Note: This is a simplified version.
     */
    fun substringFromIndex(index: Int): NSString {
        val selector = ObjectiveCRuntime.registerSelector("substringFromIndex:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, index)
        return NSString(result)
    }
    
    /**
     * Get a string representation.
     */
    override fun toString(): String = "NSString(\"${toKotlinString()}\")"
}
