// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc

import java.lang.foreign.MemorySegment

/**
 * Represents an Objective-C class.
 * Provides methods for creating instances, invoking class methods, and accessing class metadata.
 */
class ObjCClass(
    /** The class name */
    val name: String,
    /** The native handle to the class */
    val handle: MemorySegment
) {
    
    /**
     * Get an ObjCClass by its name.
     * @param className The name of the class (e.g., "NSString", "NSArray")
     * @return The ObjCClass instance
     * @throws ObjCNotFoundException if the class is not found
     */
    companion object {
        @JvmStatic
        fun fromName(className: String): ObjCClass {
            ObjectiveCRuntime.ensureInitialized()
            val handle = ObjectiveCRuntime.getClass(className)
            if (handle == MemorySegment.NULL) {
                throw ObjCNotFoundException("Class not found: $className")
            }
            return ObjCClass(className, handle)
        }
        
        /**
         * Get the class of an object.
         */
        @JvmStatic
        fun fromObject(obj: ObjCObject): ObjCClass {
            ObjectiveCRuntime.ensureInitialized()
            val classHandle = ObjectiveCRuntime.getObjectClass(obj.handle)
            val className = ObjectiveCRuntime.getClassName(classHandle)
            return ObjCClass(className, classHandle)
        }
    }
    
    /**
     * Check if this class is valid (not NULL).
     */
    fun isValid(): Boolean = handle != MemorySegment.NULL
    
    /**
     * Get the metaclass for this class.
     */
    fun getMetaClass(): ObjCClass {
        ObjectiveCRuntime.ensureInitialized()
        val metaClassHandle = ObjectiveCRuntime.getMetaClass(name)
        return ObjCClass("$name (Meta)", metaClassHandle)
    }
    
    /**
     * Create a new instance of this class using alloc and init.
     * This is equivalent to [[Class alloc] init] in Objective-C.
     * @return A new instance of the class
     */
    fun createInstance(): ObjCObject {
        ObjectiveCRuntime.ensureInitialized()
        
        // Get the alloc selector
        val allocSelector = ObjectiveCRuntime.registerSelector("alloc")
        
        // Send alloc message to the class (no arguments)
        val instanceHandle = ObjectiveCRuntime.sendMessage(handle, allocSelector)
        
        if (instanceHandle == MemorySegment.NULL) {
            throw ObjCMethodInvocationException("Failed to allocate instance of class: $name")
        }
        
        // Get the init selector
        val initSelector = ObjectiveCRuntime.registerSelector("init")
        
        // Send init message to the instance (no arguments)
        val initializedHandle = ObjectiveCRuntime.sendMessage(instanceHandle, initSelector)
        
        if (initializedHandle == MemorySegment.NULL) {
            throw ObjCMethodInvocationException("Failed to initialize instance of class: $name")
        }
        
        return ObjCObject(initializedHandle)
    }
    
    /**
     * Create a new instance using the 'new' method.
     * This is equivalent to [Class new] in Objective-C.
     * @return A new instance of the class
     */
    fun newInstance(): ObjCObject {
        ObjectiveCRuntime.ensureInitialized()
        
        val newSelector = ObjectiveCRuntime.registerSelector("new")
        val instanceHandle = ObjectiveCRuntime.sendMessage(handle, newSelector)
        
        if (instanceHandle == MemorySegment.NULL) {
            throw ObjCMethodInvocationException("Failed to create instance of class: $name")
        }
        
        return ObjCObject(instanceHandle)
    }
    
    /**
     * Invoke a class method (static method) with no arguments.
     * @param methodName The name of the class method
     * @return The return value as an ObjCObject, or null if the return type is void
     */
    fun invokeClassMethod(methodName: String): ObjCObject? {
        ObjectiveCRuntime.ensureInitialized()
        
        val selector = ObjectiveCRuntime.registerSelector(methodName)
        val result = ObjectiveCRuntime.sendMessage(handle, selector)
        
        if (result == MemorySegment.NULL) {
            return null
        }
        
        return ObjCObject(result)
    }
    
    /**
     * Check if this object is a class (not an instance).
     */
    fun isMetaClass(): Boolean {
        ObjectiveCRuntime.ensureInitialized()
        return ObjectiveCRuntime.isMetaClass(handle)
    }
    
    /**
     * Get a string representation of this class.
     */
    override fun toString(): String = "ObjCClass(name='$name', handle=$handle)"
    
    /**
     * Check equality with another object.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjCClass) return false
        return handle == other.handle
    }
    
    /**
     * Hash code based on the handle.
     */
    override fun hashCode(): Int = handle.hashCode()
}
