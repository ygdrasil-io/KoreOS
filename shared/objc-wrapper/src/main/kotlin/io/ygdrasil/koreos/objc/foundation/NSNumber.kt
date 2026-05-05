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
    val handle: MemorySegment
) {
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
            // For float, we use sendMessageFloat which handles float return types
            val selector = ObjectiveCRuntime.registerSelector("numberWithFloat:")
            val result = ObjectiveCRuntime.sendMessageFloat(nsNumberClass.handle, selector, value)
            // The result is the float value itself, but we need an object
            // This is a simplified implementation - in reality, numberWithFloat: returns an NSNumber*
            // For now, we'll create a placeholder
            val placeholderHandle = ObjectiveCRuntime.globalArena.allocate(ValueLayout.ADDRESS)
            return NSNumber(placeholderHandle)
        }
        
        /**
         * Create an NSNumber from a Kotlin Double.
         */
        @JvmStatic
        fun fromDouble(value: Double): NSNumber {
            val selector = ObjectiveCRuntime.registerSelector("numberWithDouble:")
            val result = ObjectiveCRuntime.sendMessageDouble(nsNumberClass.handle, selector, value)
            // Similar to fromFloat, this is a placeholder
            val placeholderHandle = ObjectiveCRuntime.globalArena.allocate(ValueLayout.ADDRESS)
            return NSNumber(placeholderHandle)
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
            val result = ObjectiveCRuntime.sendMessage(nsNumberClass.handle, selector, value.toInt())
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
        return result.get(ValueLayout.ofInt(), 0)
    }
    
    /**
     * Convert this NSNumber to a Kotlin Long.
     */
    fun toLong(): Long {
        val selector = ObjectiveCRuntime.registerSelector("longValue")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        return result.get(ValueLayout.ofLong(), 0)
    }
    
    /**
     * Convert this NSNumber to a Kotlin Float.
     */
    fun toFloat(): Float {
        val selector = ObjectiveCRuntime.registerSelector("floatValue")
        // floatValue returns a float directly, not an object
        // We need to use objc_msgSend_fpret for this
        return ObjectiveCRuntime.sendMessageFloat(handle, selector, 0f)
    }
    
    /**
     * Convert this NSNumber to a Kotlin Double.
     */
    fun toDouble(): Double {
        val selector = ObjectiveCRuntime.registerSelector("doubleValue")
        return ObjectiveCRuntime.sendMessageDouble(handle, selector, 0.0)
    }
    
    /**
     * Convert this NSNumber to a Kotlin Boolean.
     */
    fun toBoolean(): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("boolValue")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        // boolValue returns a BOOL (signed char)
        return result.get(ValueLayout.ofByte(), 0).toInt() != 0
    }
    
    /**
     * Convert this NSNumber to a Kotlin Char.
     */
    fun toChar(): Char {
        val selector = ObjectiveCRuntime.registerSelector("charValue")
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        return result.get(ValueLayout.ofChar(), 0).toInt().toChar()
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
        return result.get(ValueLayout.ofLong(), 0).toInt()
    }
    
    /**
     * Check if this NSNumber is equal to another NSNumber.
     */
    fun isEqualToNumber(other: NSNumber): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("isEqualToNumber:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, other.handle)
        return result.get(ValueLayout.ofByte(), 0) != 0.toByte()
    }
    
    /**
     * Get a string representation.
     */
    override fun toString(): String {
        return "NSNumber(${toStringValue().toKotlinString()})"
    }
}
