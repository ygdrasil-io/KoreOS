// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc.foundation

import io.ygdrasil.koreos.objc.ObjCClass
import io.ygdrasil.koreos.objc.ObjCObject
import io.ygdrasil.koreos.objc.ObjectiveCRuntime
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.charset.StandardCharsets

/**
 * Wrapper for the Objective-C NSNumber class.
 * Provides methods for creating number objects from primitive types.
 */
class NSNumber(
    /** The native handle to the NSNumber object */
    handle: MemorySegment
) : ObjCObject(handle) {
    companion object {
        private val nsNumberClass: ObjCClass by lazy {
            ObjCClass.fromName("NSNumber")
        }
        
        /**
         * Create an NSNumber from a Kotlin Int.
         */
        @JvmStatic
        fun fromInt(value: Int): NSNumber {
            val selector = ObjectiveCRuntime.registerSelector("numberWithInt:")
            val result = ObjectiveCRuntime.sendMessage(nsNumberClass.handle, selector, value)
            return NSNumber(result)
        }
        
        /**
         * Create an NSNumber from a Kotlin Long.
         */
        @JvmStatic
        fun fromLong(value: Long): NSNumber {
            val selector = ObjectiveCRuntime.registerSelector("numberWithLong:")
            val result = ObjectiveCRuntime.sendMessage(nsNumberClass.handle, selector, value)
            return NSNumber(result)
        }
        
        /**
         * Create an NSNumber from a Kotlin Float.
         */
        @JvmStatic
        fun fromFloat(value: Float): NSNumber {
            // Simplified implementation - use fromInt for now
            return fromInt(value.toInt())
        }
        
        /**
         * Create an NSNumber from a Kotlin Double.
         */
        @JvmStatic
        fun fromDouble(value: Double): NSNumber {
            // Simplified implementation - use fromLong for now
            return fromLong(value.toLong())
        }
        
        /**
         * Create an NSNumber from a Kotlin Boolean.
         */
        @JvmStatic
        fun fromBoolean(value: Boolean): NSNumber {
            val selector = ObjectiveCRuntime.registerSelector("numberWithBool:")
            // BOOL in Objective-C is a signed char (0 or 1)
            val boolValue = if (value) 1 else 0
            val result = ObjectiveCRuntime.sendMessage(nsNumberClass.handle, selector, boolValue)
            return NSNumber(result)
        }
        
        /**
         * Create an NSNumber from a Kotlin Char.
         */
        @JvmStatic
        fun fromChar(value: Char): NSNumber {
            val selector = ObjectiveCRuntime.registerSelector("numberWithChar:")
            val result = ObjectiveCRuntime.sendMessage(nsNumberClass.handle, selector, value.code)
            return NSNumber(result)
        }
    }
    
    /**
     * Get the ObjCObject representation of this number.
     */
    fun toObjCObject(): ObjCObject = ObjCObject(handle)
    
    /**
     * Convert this NSNumber to a Kotlin Int.
     */
    fun toInt(): Int {
        val selector = ObjectiveCRuntime.registerSelector("intValue")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        return result.address().toInt()
    }
    
    /**
     * Convert this NSNumber to a Kotlin Long.
     */
    fun toLong(): Long {
        val selector = ObjectiveCRuntime.registerSelector("longValue")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        return result.address()
    }
    
    /**
     * Convert this NSNumber to a Kotlin Float.
     */
    fun toFloat(): Float {
        // Simplified implementation - use toInt for now
        return toInt().toFloat()
    }
    
    /**
     * Convert this NSNumber to a Kotlin Double.
     */
    fun toDouble(): Double {
        // Simplified implementation - use toLong for now
        return toLong().toDouble()
    }
    
    /**
     * Convert this NSNumber to a Kotlin Boolean.
     */
    fun toBoolean(): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("boolValue")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        // boolValue returns a BOOL (signed char)
        return result.address().toInt() != 0
    }
    
    /**
     * Convert this NSNumber to a Kotlin Char.
     */
    fun toChar(): Char {
        val selector = ObjectiveCRuntime.registerSelector("charValue")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        return result.address().toInt().toChar()
    }
    
    /**
     * Get the string representation of this number.
     */
    fun toStringValue(): NSString {
        val selector = ObjectiveCRuntime.registerSelector("stringValue")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        return NSString(result)
    }
    
    /**
     * Compare this NSNumber with another NSNumber.
     * Returns -1, 0, or 1 based on the comparison.
     */
    fun compare(other: NSNumber): Int {
        val selector = ObjectiveCRuntime.registerSelector("compare:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, other.handle)
        // compare: returns an NSComparisonResult (typedef NSInteger)
        return result.address().toInt()
    }
    
    /**
     * Check if this NSNumber is equal to another NSNumber.
     */
    fun isEqualToNumber(other: NSNumber): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("isEqualToNumber:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, other.handle)
        return result.address().toByte() != 0.toByte()
    }
    
    /**
     * Get a string representation.
     */
    override fun toString(): String {
        return "NSNumber(${toStringValue().toKotlinString()})"
    }
}
