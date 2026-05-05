// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc.foundation

import io.ygdrasil.koreos.objc.ObjCClass
import io.ygdrasil.koreos.objc.ObjCObject
import io.ygdrasil.koreos.objc.ObjectiveCRuntime
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Wrapper for the Objective-C NSNumber class.
 * Provides methods for creating number objects from primitive types.
 */
class NSNumber(
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
            val selector = ObjectiveCRuntime.registerSelector("numberWithFloat:")
            val result = ObjectiveCRuntime.sendMessage(nsNumberClass.handle, selector, value)
            return NSNumber(result)
        }
        
        /**
         * Create an NSNumber from a Kotlin Double.
         */
        @JvmStatic
        fun fromDouble(value: Double): NSNumber {
            val selector = ObjectiveCRuntime.registerSelector("numberWithDouble:")
            val result = ObjectiveCRuntime.sendMessage(nsNumberClass.handle, selector, value)
            return NSNumber(result)
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
     * Convert this NSNumber to a Kotlin Int.
     */
    fun toInt(): Int {
        val result = invokeMethod("intValue")
        return result?.handle?.get(ValueLayout.JAVA_INT, 0) ?: 0
    }
    
    /**
     * Convert this NSNumber to a Kotlin Long.
     */
    fun toLong(): Long {
        val result = invokeMethod("longValue")
        return result?.handle?.get(ValueLayout.JAVA_LONG, 0) ?: 0L
    }
    
    /**
     * Convert this NSNumber to a Kotlin Float.
     */
    fun toFloat(): Float {
        val result = invokeMethod("floatValue")
        return result?.handle?.get(ValueLayout.JAVA_FLOAT, 0) ?: 0f
    }
    
    /**
     * Convert this NSNumber to a Kotlin Double.
     */
    fun toDouble(): Double {
        val result = invokeMethod("doubleValue")
        return result?.handle?.get(ValueLayout.JAVA_DOUBLE, 0) ?: 0.0
    }
    
    /**
     * Convert this NSNumber to a Kotlin Boolean.
     */
    fun toBoolean(): Boolean {
        val result = invokeMethod("boolValue")
        // boolValue returns a BOOL (signed char)
        return result?.handle?.get(ValueLayout.JAVA_BYTE, 0)?.toInt() != 0
    }
    
    /**
     * Convert this NSNumber to a Kotlin Char.
     */
    fun toChar(): Char {
        val result = invokeMethod("charValue")
        return result?.handle?.get(ValueLayout.JAVA_CHAR, 0)?.toInt()?.toChar() ?: '\u0000'
    }
    
    /**
     * Get the string representation of this number.
     */
    fun toStringValue(): NSString {
        val result = invokeMethod("stringValue")
        return NSString(result!!.handle)
    }
    
    /**
     * Compare this NSNumber with another NSNumber.
     * Returns -1, 0, or 1 based on the comparison.
     */
    fun compare(other: NSNumber): Int {
        val selector = ObjectiveCRuntime.registerSelector("compare:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, other.handle)
        // compare: returns an NSComparisonResult (typedef NSInteger)
        return result.get(ValueLayout.JAVA_LONG, 0).toInt()
    }
    
    /**
     * Check if this NSNumber is equal to another NSNumber.
     */
    fun isEqualToNumber(other: NSNumber): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("isEqualToNumber:")
        val result = ObjectiveCRuntime.sendMessage(handle, selector, other.handle)
        return result.get(ValueLayout.JAVA_BYTE, 0) != 0.toByte()
    }
    
    /**
     * Get the class of this object.
     */
    override fun getClass(): ObjCClass = nsNumberClass
    
    /**
     * Get a string representation.
     */
    override fun toString(): String {
        return "NSNumber(${toStringValue().toKotlinString()})"
    }
}
