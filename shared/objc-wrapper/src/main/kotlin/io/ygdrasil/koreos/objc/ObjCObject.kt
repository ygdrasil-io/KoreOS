// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc

import java.lang.foreign.MemorySegment

/**
 * Represents an Objective-C object instance.
 * Provides methods for invoking instance methods, accessing properties, and managing memory.
 */
class ObjCObject(
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
    fun getClass(): ObjCClass {
        return ObjCClass.fromObject(this)
    }
    
    /**
     * Invoke an instance method on this object.
     * @param methodName The name of the method to invoke
     * @param args Arguments for the method
     * @return The return value as an ObjCObject, or null if the return type is void
     */
    fun invokeMethod(methodName: String, vararg args: Any?): ObjCObject? {
        ObjectiveCRuntime.ensureInitialized()
        
        val selector = ObjectiveCRuntime.registerSelector(methodName)
        val result = ObjectiveCRuntime.sendMessage(handle, selector, *args)
        
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
    fun getProperty(propertyName: String): ObjCObject? {
        // For simple properties, the getter has the same name as the property
        return invokeMethod(propertyName)
    }
    
    /**
     * Set the value of a property.
     * This calls the setter method for the property.
     * @param propertyName The name of the property
     * @param value The value to set (must be an ObjCObject or convertible)
     */
    fun setProperty(propertyName: String, value: ObjCObject) {
        // Setter name format: setPropertyName:
        val setterName = "set${propertyName.capitalize()}:"
        invokeMethod(setterName, value)
    }
    
    /**
     * Set the value of a property with a Kotlin String.
     */
    fun setProperty(propertyName: String, value: String) {
        val nsString = NSString.fromString(value)
        setProperty(propertyName, nsString)
    }
    
    /**
     * Set the value of a property with a Kotlin Int.
     */
    fun setProperty(propertyName: String, value: Int) {
        // For primitive types, we need to box them in NSNumber
        // This is a simplified approach
        val nsNumber = NSNumber.fromInt(value)
        setProperty(propertyName, nsNumber)
    }
    
    /**
     * Set the value of a property with a Kotlin Boolean.
     */
    fun setProperty(propertyName: String, value: Boolean) {
        val nsNumber = NSNumber.fromBoolean(value)
        setProperty(propertyName, nsNumber)
    }
    
    /**
     * Get the description of this object (calls the description method).
     */
    fun description(): String {
        val descObj = invokeMethod("description")
        return descObj?.toString() ?: "null"
    }
    
    /**
     * Retain this object (increment reference count).
     */
    fun retain(): ObjCObject {
        ObjectiveCRuntime.ensureInitialized()
        val retainSelector = ObjectiveCRuntime.registerSelector("retain")
        val retainedHandle = ObjectiveCRuntime.sendMessage(handle, retainSelector)
        return ObjCObject(retainedHandle)
    }
    
    /**
     * Release this object (decrement reference count).
     */
    fun release() {
        ObjectiveCRuntime.ensureInitialized()
        val releaseSelector = ObjectiveCRuntime.registerSelector("release")
        ObjectiveCRuntime.sendMessage(handle, releaseSelector)
    }
    
    /**
     * Autorelease this object.
     */
    fun autorelease(): ObjCObject {
        ObjectiveCRuntime.ensureInitialized()
        val autoreleaseSelector = ObjectiveCRuntime.registerSelector("autorelease")
        val autoreleasedHandle = ObjectiveCRuntime.sendMessage(handle, autoreleaseSelector)
        return ObjCObject(autoreleasedHandle)
    }
    
    /**
     * Check if this object is equal to another object.
     */
    fun isEqual(other: ObjCObject): Boolean {
        val isEqualSelector = ObjectiveCRuntime.registerSelector("isEqual:")
        val result = ObjectiveCRuntime.sendMessage(handle, isEqualSelector, other.handle)
        // In Objective-C, isEqual: returns a BOOL which is a signed char
        // We need to extract the boolean value from the MemorySegment
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
