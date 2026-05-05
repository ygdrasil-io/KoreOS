// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc.foundation

import io.ygdrasil.koreos.objc.ObjCClass
import io.ygdrasil.koreos.objc.ObjCObject
import io.ygdrasil.koreos.objc.ObjectiveCRuntime
import java.lang.foreign.MemorySegment

/**
 * Wrapper for the Objective-C NSArray class.
 * Provides methods for creating and manipulating arrays.
 */
class NSArray(
    handle: MemorySegment
) : ObjCObject(handle) {
    
    companion object {
        private val nsArrayClass: ObjCClass by lazy {
            ObjCClass.fromName("NSArray")
        }
        
        /**
         * Create an empty NSArray.
         */
        @JvmStatic
        fun empty(): NSArray {
            val selector = ObjectiveCRuntime.registerSelector("array")
            val result = ObjectiveCRuntime.sendMessage(
                nsArrayClass.handle,
                selector
            )
            return NSArray(result)
        }
        
        /**
         * Create an NSArray with a single object.
         */
        @JvmStatic
        fun withObject(obj: ObjCObject): NSArray {
            val selector = ObjectiveCRuntime.registerSelector("arrayWithObject:")
            val result = ObjectiveCRuntime.sendMessage(
                nsArrayClass.handle,
                selector,
                obj.handle
            )
            return NSArray(result)
        }
        
        /**
         * Create an NSArray from a list of objects.
         */
        @JvmStatic
        fun fromList(objects: List<ObjCObject>): NSArray {
            if (objects.isEmpty()) {
                return empty()
            }
            
            // For multiple objects, we need to use arrayWithObjects:count:
            // This requires allocating an array of pointers
            val arena = Arena.ofConfined()
            
            return arena.use {
                // Allocate array of pointers
                val arrayPtr = arena.allocate(ValueLayout.ADDRESS, objects.size.toLong())
                
                // Copy object handles to the array
                for ((i, obj) in objects.withIndex()) {
                    arrayPtr.setAtIndex(ValueLayout.ADDRESS, i.toLong(), obj.handle)
                }
                
                // Get the selector
                val selector = ObjectiveCRuntime.registerSelector("arrayWithObjects:count:")
                
                // Send the message
                val result = ObjectiveCRuntime.sendMessage(
                    nsArrayClass.handle,
                    selector,
                    arrayPtr,
                    objects.size
                )
                
                NSArray(result)
            }
        }
    }
    
    /**
     * Get the number of objects in the array.
     */
    fun count(): Int {
        val result = invokeMethod("count")
        // count returns an NSUInteger (unsigned long)
        return result?.handle?.get(ValueLayout.JAVA_LONG, 0)?.toInt() ?: 0
    }
    
    /**
     * Check if the array is empty.
     */
    fun isEmpty(): Boolean = count() == 0
    
    /**
     * Get the object at a specific index.
     */
    fun objectAtIndex(index: Int): ObjCObject {
        val selector = ObjectiveCRuntime.registerSelector("objectAtIndex:")
        val result = ObjectiveCRuntime.sendMessage(
            handle,
            selector,
            index
        )
        return ObjCObject(result)
    }
    
    /**
     * Get the first object in the array.
     */
    fun firstObject(): ObjCObject? {
        if (count() == 0) return null
        return objectAtIndex(0)
    }
    
    /**
     * Get the last object in the array.
     */
    fun lastObject(): ObjCObject? {
        if (count() == 0) return null
        return objectAtIndex(count() - 1)
    }
    
    /**
     * Check if the array contains an object.
     */
    fun containsObject(obj: ObjCObject): Boolean {
        val selector = ObjectiveCRuntime.registerSelector("containsObject:")
        val result = ObjectiveCRuntime.sendMessage(
            handle,
            selector,
            obj.handle
        )
        // containsObject: returns a BOOL (signed char)
        return result.get(ValueLayout.JAVA_BYTE, 0) != 0.toByte()
    }
    
    /**
     * Get the index of an object in the array.
     */
    fun indexOfObject(obj: ObjCObject): Int {
        val selector = ObjectiveCRuntime.registerSelector("indexOfObject:")
        val result = ObjectiveCRuntime.sendMessage(
            handle,
            selector,
            obj.handle
        )
        // indexOfObject: returns an NSUInteger (unsigned long)
        // Returns NSNotFound if not found
        val index = result.get(ValueLayout.JAVA_LONG, 0).toInt()
        return if (index == Int.MAX_VALUE) -1 else index
    }
    
    /**
     * Convert the array to a Kotlin list.
     */
    fun toList(): List<ObjCObject> {
        val size = count()
        return List(size) { index -> objectAtIndex(index) }
    }
    
    /**
     * Get a subarray from a range.
     */
    fun subarrayWithRange(start: Int, length: Int): NSArray {
        val range = NSRange(start, length)
        val selector = ObjectiveCRuntime.registerSelector("subarrayWithRange:")
        val result = ObjectiveCRuntime.sendMessage(
            handle,
            selector,
            range.handle
        )
        return NSArray(result)
    }
    
    /**
     * Get the class of this object.
     */
    override fun getClass(): ObjCClass = nsArrayClass
    
    /**
     * Get a string representation.
     */
    override fun toString(): String {
        val objects = toList()
        val stringObjects = objects.map { 
            if (it is NSString) it.toKotlinString() 
            else it.getClass().name 
        }
        return "NSArray($stringObjects)"
    }
}

/**
 * Wrapper for NSRange structure.
 */
class NSRange(
    val location: Int,
    val length: Int
) {
    companion object {
        @JvmStatic
        fun fromLocationAndLength(location: Int, length: Int): NSRange {
            return NSRange(location, length)
        }
    }
    
    // Handle for passing to native code
    // Note: In a full implementation, we would need to allocate native memory
    // and copy the structure data
    val handle: MemorySegment by lazy {
        val arena = Arena.ofConfined()
        arena.use {
            val segment = arena.allocate(16) // 2 * sizeof(NSUInteger)
            segment.set(ValueLayout.JAVA_LONG, 0, location.toLong())
            segment.set(ValueLayout.JAVA_LONG, 8, length.toLong())
            segment
        }
    }
}
