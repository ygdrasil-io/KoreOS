// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Represents an Objective-C object instance.
 * Provides methods for invoking instance methods, accessing properties, and managing memory.
 * 
 * This class is open to allow subclassing by specific Objective-C types like NSString, NSArray, etc.
 */
open class ObjCObject(
    /** The native handle to the object */
    val handle: MemorySegment
) {
    
    /**
     * Create an ObjCObject from a native handle.
     */
    companion object {
        @JvmStatic
        fun fromHandle(handle: MemorySegment): ObjCObject = ObjCObject(handle)
    }
    
    /**
     * Check if this object is valid (not NULL).
     */
    fun isValid(): Boolean = handle != MemorySegment.NULL
    
    /**
     * Get the class of this object.
     */
    open fun getClass(): ObjCClass {
        return ObjCClass.fromObject(this)
    }
    
    /**
     * Invoke an instance method on this object with no arguments.
     * @param methodName The name of the method to invoke
     * @return The return value as an ObjCObject, or null if the return type is void
     */
    open fun invokeMethod(methodName: String): ObjCObject? {
        ObjectiveCRuntime.ensureInitialized()
        
        val selector = ObjectiveCRuntime.registerSelector(methodName)
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        
        if (result == MemorySegment.NULL) {
            return null
        }
        
        return ObjCObject(result)
    }
    
    /**
     * Invoke an instance method on this object with a MemorySegment argument.
     * @param methodName The name of the method to invoke
     * @param arg The MemorySegment argument
     * @return The return value as an ObjCObject, or null if the return type is void
     */
    open fun invokeMethod(methodName: String, arg: MemorySegment): ObjCObject? {
        ObjectiveCRuntime.ensureInitialized()
        
        val selector = ObjectiveCRuntime.registerSelector(methodName)
        val result = ObjectiveCRuntime.sendMessage(handle, selector, arg)
        
        if (result == MemorySegment.NULL) {
            return null
        }
        
        return ObjCObject(result)
    }
    
    /**
     * Invoke an instance method on this object with an integer argument.
     * @param methodName The name of the method to invoke
     * @param arg The integer argument
     * @return The return value as an ObjCObject, or null if the return type is void
     */
    open fun invokeMethod(methodName: String, arg: Int): ObjCObject? {
        ObjectiveCRuntime.ensureInitialized()
        
        val selector = ObjectiveCRuntime.registerSelector(methodName)
        val result = ObjectiveCRuntime.sendMessage(handle, selector, arg)
        
        if (result == MemorySegment.NULL) {
            return null
        }
        
        return ObjCObject(result)
    }
    
    /**
     * Invoke an instance method on this object with a long argument.
     * @param methodName The name of the method to invoke
     * @param arg The long argument
     * @return The return value as an ObjCObject, or null if the return type is void
     */
    open fun invokeMethod(methodName: String, arg: Long): ObjCObject? {
        ObjectiveCRuntime.ensureInitialized()
        
        val selector = ObjectiveCRuntime.registerSelector(methodName)
        val result = ObjectiveCRuntime.sendMessage(handle, selector, arg)
        
        if (result == MemorySegment.NULL) {
            return null
        }
        
        return ObjCObject(result)
    }
    
    /**
     * Get the value of a property.
     * This calls the getter method for the property.
     * @param propertyName The name of the property
     * @return The property value as an ObjCObject
     */
    open fun getProperty(propertyName: String): ObjCObject? {
        // For simple properties, the getter has the same name as the property
        return invokeMethod(propertyName)
    }
    
    /**
     * Set the value of a property.
     * This calls the setter method for the property.
     * @param propertyName The name of the property
     * @param value The value to set (must be an ObjCObject)
     */
    open fun setProperty(propertyName: String, value: ObjCObject) {
        // Setter name format: setPropertyName:
        val setterName = "set${propertyName.capitalize()}:"
        invokeMethod(setterName, value.handle)
    }
    
    /**
     * Set the value of a property with a Kotlin String.
     */
    open fun setProperty(propertyName: String, value: String) {
        // Import here to avoid circular dependency
        val nsStringClass = ObjCClass.fromName("NSString")
        val selector = ObjectiveCRuntime.registerSelector("stringWithUTF8String:")
        val utf8String = ObjectiveCRuntime.allocateUtf8String(value)
        val stringHandle = ObjectiveCRuntime.sendMessage(nsStringClass.handle, selector, utf8String)
        setProperty(propertyName, ObjCObject(stringHandle))
    }
    
    /**
     * Set the value of a property with a Kotlin Int.
     */
    open fun setProperty(propertyName: String, value: Int) {
        val nsNumberClass = ObjCClass.fromName("NSNumber")
        val selector = ObjectiveCRuntime.registerSelector("numberWithInt:")
        val numberHandle = ObjectiveCRuntime.sendMessage(nsNumberClass.handle, selector, value)
        setProperty(propertyName, ObjCObject(numberHandle))
    }
    
    /**
     * Set the value of a property with a Kotlin Boolean.
     */
    open fun setProperty(propertyName: String, value: Boolean) {
        val nsNumberClass = ObjCClass.fromName("NSNumber")
        val selector = ObjectiveCRuntime.registerSelector("numberWithBool:")
        val boolValue = if (value) 1 else 0
        val numberHandle = ObjectiveCRuntime.sendMessage(nsNumberClass.handle, selector, boolValue)
        setProperty(propertyName, ObjCObject(numberHandle))
    }
    
    /**
     * Get the description of this object (calls the description method).
     */
    open fun description(): String {
        val descObj = invokeMethod("description")
        return descObj?.toString() ?: "null"
    }
    
    /**
     * Retain this object (increment reference count).
     */
    open fun retain(): ObjCObject {
        ObjectiveCRuntime.ensureInitialized()
        val retainSelector = ObjectiveCRuntime.registerSelector("retain")
        val retainedHandle = ObjectiveCRuntime.sendMessage(handle, retainSelector)
        return ObjCObject(retainedHandle)
    }
    
    /**
     * Release this object (decrement reference count).
     */
    open fun release() {
        ObjectiveCRuntime.ensureInitialized()
        val releaseSelector = ObjectiveCRuntime.registerSelector("release")
        ObjectiveCRuntime.sendMessage(handle, releaseSelector)
    }
    
    /**
     * Autorelease this object.
     */
    open fun autorelease(): ObjCObject {
        ObjectiveCRuntime.ensureInitialized()
        val autoreleaseSelector = ObjectiveCRuntime.registerSelector("autorelease")
        val autoreleasedHandle = ObjectiveCRuntime.sendMessage(handle, autoreleaseSelector)
        return ObjCObject(autoreleasedHandle)
    }
    
    /**
     * Check if this object is equal to another object.
     */
    open fun isEqual(other: ObjCObject): Boolean {
        val isEqualSelector = ObjectiveCRuntime.registerSelector("isEqual:")
        val result = ObjectiveCRuntime.sendMessage(handle, isEqualSelector, other.handle)
        // In Objective-C, isEqual: returns a BOOL which is a signed char
        return result.get(ValueLayout.JAVA_BYTE, 0) != 0.toByte()
    }
    
    /**
     * Get the hash code of this object.
     */
    override fun hashCode(): Int = handle.hashCode()
    
    /**
     * Check equality with another object.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjCObject) return false
        return handle == other.handle
    }
    
    /**
     * Get a string representation of this object.
     */
    override fun toString(): String {
        return "ObjCObject(handle=$handle, class=${getClass().name})"
    }
}
