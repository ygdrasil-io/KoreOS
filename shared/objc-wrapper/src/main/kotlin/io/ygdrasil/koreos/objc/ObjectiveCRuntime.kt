// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc

import java.lang.foreign.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.nio.file.Path

/**
 * Low-level FFM wrapper for the Objective-C runtime (libobjc.dylib).
 * Provides access to core Objective-C runtime functions for class and method manipulation.
 * 
 * This class uses Java 21+ Foreign Function & Memory API to interact with native Objective-C libraries.
 * It requires Java 21+ and macOS with libobjc.dylib available.
 */
object ObjectiveCRuntime {
    
    // Global arena for memory management
    private val globalArena: Arena = Arena.global()
    
    // Native linker
    private val linker: Linker = Linker.nativeLinker()
    
    // Symbol lookup for libobjc
    private lateinit var objcLookup: SymbolLookup
    
    // Method handles for Objective-C runtime functions
    private lateinit var objc_getClass: MethodHandle
    private lateinit var objc_getMetaClass: MethodHandle
    private lateinit var objc_getProtocol: MethodHandle
    private lateinit var sel_registerName: MethodHandle
    private lateinit var sel_getUid: MethodHandle
    private lateinit var class_getName: MethodHandle
    private lateinit var object_getClass: MethodHandle
    private lateinit var object_isClass: MethodHandle
    private lateinit var object_getIvar: MethodHandle
    private lateinit var object_setIvar: MethodHandle
    private lateinit var ivar_getName: MethodHandle
    private lateinit var ivar_getTypeEncoding: MethodHandle
    
    // objc_msgSend handles - these have variable signatures
    private val msgSendHandles: MutableMap<String, MethodHandle> = mutableMapOf()
    
    // Function descriptors
    private val OBJC_GET_CLASS_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS,  // Class (pointer)
        ValueLayout.ADDRESS   // const char* (class name)
    )
    
    private val SEL_REGISTER_NAME_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS,  // SEL
        ValueLayout.ADDRESS   // const char* (selector name)
    )
    
    private val CLASS_GET_NAME_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS,  // const char*
        ValueLayout.ADDRESS   // Class
    )
    
    private val OBJECT_GET_CLASS_DESC = FunctionDescriptor.of(
        ValueLayout.ADDRESS,  // Class
        ValueLayout.ADDRESS   // id (object)
    )
    
    @Volatile
    private var initialized: Boolean = false
    
    /**
     * Check if FFM is supported (Java 21+).
     */
    @JvmStatic
    fun isFFMSupported(): Boolean {
        return Runtime.version().feature() >= 21
    }
    
    /**
     * Check if the runtime is initialized.
     */
    @JvmStatic
    fun isInitialized(): Boolean = initialized
    
    /**
     * Initialize the Objective-C runtime bindings.
     * This loads libobjc.dylib and resolves the necessary symbols.
     * Thread-safe and idempotent.
     * 
     * @throws ObjCInitializationException if initialization fails
     */
    @Synchronized
    @JvmStatic
    fun initialize() {
        if (initialized) {
            return
        }
        
        if (!isFFMSupported()) {
            throw ObjCInitializationException(
                "FFM requires Java 21+. Current version: " + Runtime.version()
            )
        }
        
        try {
            println("[ObjectiveCRuntime] Initializing...")
            
            // Try loading libobjc.dylib
            var loaded = false
            
            // First, try System.loadLibrary
            try {
                System.loadLibrary("objc")
                objcLookup = linker.defaultLookup()
                loaded = true
                println("[ObjectiveCRuntime] Loaded libobjc.dylib via System.loadLibrary")
            } catch (e: UnsatisfiedLinkError) {
                println("[ObjectiveCRuntime] System.loadLibrary failed: ${e.message}")
            } catch (e: SecurityException) {
                println("[ObjectiveCRuntime] System.loadLibrary failed: ${e.message}")
            }
            
            // If not loaded, try known paths
            if (!loaded) {
                val knownPaths = listOf(
                    "/usr/lib/libobjc.dylib",
                    "/usr/lib/system/libobjc.dylib",
                    "/Library/Developer/CommandLineTools/usr/lib/libobjc.dylib"
                )
                
                for (path in knownPaths) {
                    try {
                        if (Path.of(path).toFile().exists()) {
                            System.load(path)
                            objcLookup = SymbolLookup.libraryLookup(Path.of(path), globalArena)
                            loaded = true
                            println("[ObjectiveCRuntime] Loaded libobjc.dylib from: $path")
                            break
                        }
                    } catch (e: Exception) {
                        // Try next path
                    }
                }
            }
            
            if (!loaded) {
                throw ObjCInitializationException(
                    "Failed to load libobjc.dylib. Ensure you are running on macOS."
                )
            }
            
            // Resolve required symbols
            objc_getClass = resolveSymbol("objc_getClass", OBJC_GET_CLASS_DESC)
            objc_getMetaClass = resolveSymbol("objc_getMetaClass", OBJC_GET_CLASS_DESC)
            objc_getProtocol = resolveSymbol("objc_getProtocol", OBJC_GET_CLASS_DESC)
            sel_registerName = resolveSymbol("sel_registerName", SEL_REGISTER_NAME_DESC)
            sel_getUid = resolveSymbol("sel_getUid", SEL_REGISTER_NAME_DESC)
            class_getName = resolveSymbol("class_getName", CLASS_GET_NAME_DESC)
            object_getClass = resolveSymbol("object_getClass", OBJECT_GET_CLASS_DESC)
            object_isClass = resolveSymbol("object_isClass", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS))
            object_getIvar = resolveSymbol("object_getIvar", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
            object_setIvar = resolveSymbol("object_setIvar", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
            ivar_getName = resolveSymbol("ivar_getName", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS))
            ivar_getTypeEncoding = resolveSymbol("ivar_getTypeEncoding", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS))
            
            println("[ObjectiveCRuntime] All required symbols resolved")
            
            // Pre-register common selectors
            registerSelector("alloc")
            registerSelector("init")
            registerSelector("new")
            registerSelector("release")
            registerSelector("retain")
            registerSelector("autorelease")
            registerSelector("description")
            registerSelector("UTF8String")
            registerSelector("count")
            registerSelector("objectAtIndex:")
            
            initialized = true
            println("[ObjectiveCRuntime] Initialized successfully")
            
        } catch (e: Exception) {
            throw ObjCInitializationException("Failed to initialize ObjectiveCRuntime", e)
        }
    }
    
    /**
     * Ensure the runtime is initialized.
     * @throws ObjCInitializationException if not initialized
     */
    @JvmStatic
    fun ensureInitialized() {
        if (!initialized) {
            throw ObjCInitializationException(
                "ObjectiveCRuntime not initialized. Call initialize() first."
            )
        }
    }
    
    /**
     * Resolve a symbol from libobjc.
     */
    private fun resolveSymbol(name: String, desc: FunctionDescriptor): MethodHandle {
        val symbol = objcLookup.find(name).orElseThrow {
            ObjCInitializationException("Symbol not found: $name")
        }
        return linker.downcallHandle(symbol, desc)
    }
    
    /**
     * Allocate a UTF-8 string in native memory.
     */
    fun allocateUtf8String(value: String): MemorySegment {
        ensureInitialized()
        val bytes = value.encodeToByteArray()
        val segment = globalArena.allocate(bytes.size.toLong() + 1)
        segment.copyFrom(MemorySegment.ofArray(bytes))
        segment.set(ValueLayout.JAVA_BYTE, bytes.size.toLong(), 0) // Null terminator
        return segment
    }
    
    /**
     * Register an Objective-C selector and return its handle.
     */
    fun registerSelector(selectorName: String): MemorySegment {
        ensureInitialized()
        val nameSegment = allocateUtf8String(selectorName)
        return sel_registerName.invoke(nameSegment) as MemorySegment
    }
    
    /**
     * Get an Objective-C class by name.
     */
    fun getClass(className: String): MemorySegment {
        ensureInitialized()
        val nameSegment = allocateUtf8String(className)
        return objc_getClass.invoke(nameSegment) as MemorySegment
    }
    
    /**
     * Get the metaclass for a class.
     */
    fun getMetaClass(className: String): MemorySegment {
        ensureInitialized()
        val nameSegment = allocateUtf8String(className)
        return objc_getMetaClass.invoke(nameSegment) as MemorySegment
    }
    
    /**
     * Get the name of a class.
     */
    fun getClassName(cls: MemorySegment): String {
        ensureInitialized()
        val namePtr = class_getName.invoke(cls) as MemorySegment
        return namePtr.getUtf8String(0)
    }
    
    /**
     * Get the class of an object.
     */
    fun getObjectClass(obj: MemorySegment): MemorySegment {
        ensureInitialized()
        return object_getClass.invoke(obj) as MemorySegment
    }
    
    /**
     * Check if an object is a class.
     */
    fun isClass(obj: MemorySegment): Boolean {
        ensureInitialized()
        return object_isClass.invoke(obj) as Boolean
    }
    
    /**
     * Get the value of an instance variable.
     */
    fun getIvar(obj: MemorySegment, ivar: MemorySegment): MemorySegment {
        ensureInitialized()
        return object_getIvar.invoke(obj, ivar) as MemorySegment
    }
    
    /**
     * Set the value of an instance variable.
     */
    fun setIvar(obj: MemorySegment, ivar: MemorySegment, value: MemorySegment) {
        ensureInitialized()
        object_setIvar.invoke(obj, ivar, value)
    }
    
    /**
     * Get the name of an instance variable.
     */
    fun getIvarName(ivar: MemorySegment): String {
        ensureInitialized()
        val namePtr = ivar_getName.invoke(ivar) as MemorySegment
        return namePtr.getUtf8String(0)
    }
    
    /**
     * Get the type encoding of an instance variable.
     */
    fun getIvarTypeEncoding(ivar: MemorySegment): String {
        ensureInitialized()
        val encodingPtr = ivar_getTypeEncoding.invoke(ivar) as MemorySegment
        return encodingPtr.getUtf8String(0)
    }
    
    /**
     * Send a message to an Objective-C object or class.
     * This is the core method for invoking Objective-C methods.
     * 
     * @param receiver The object or class to send the message to
     * @param selector The selector (method name)
     * @param args Variable arguments for the method
     * @return The return value as a MemorySegment
     */
    fun sendMessage(receiver: MemorySegment, selector: MemorySegment, vararg args: Any?): MemorySegment {
        ensureInitialized()
        
        // For objc_msgSend, we need to handle the variable arguments
        // This is complex with FFM, so we use a simplified approach for now
        // In a full implementation, we would need to:
        // 1. Determine the method signature
        // 2. Allocate memory for arguments
        // 3. Copy arguments to native memory
        // 4. Call the appropriate objc_msgSend variant
        
        // For now, we'll use a basic approach that works for methods with no arguments
        // or simple return types
        if (args.isEmpty()) {
            // Simple case: no arguments
            val msgSend = getMsgSendHandle("@0:0") // @ is id, 0 is no args
            return msgSend.invoke(receiver, selector) as MemorySegment
        }
        
        // For methods with arguments, we need a more complex approach
        // This is a placeholder - a full implementation would need to handle
        // type encodings and proper argument marshaling
        throw ObjCMethodInvocationException(
            "Message sending with arguments not yet fully implemented. " +
            "Use specialized methods for common cases."
        )
    }
    
    /**
     * Get or create a msgSend handle for a specific signature.
     */
    private fun getMsgSendHandle(signature: String): MethodHandle {
        return msgSendHandles.getOrPut(signature) {
            // For simplicity, we use the basic objc_msgSend
            // In a full implementation, we would look up the appropriate variant
            // based on the signature (objc_msgSend, objc_msgSend_stret, etc.)
            resolveSymbol("objc_msgSend", OBJC_MSG_SEND_DESC)
        }
    }
    
    /**
     * Reset the runtime state (for testing purposes).
     */
    @Synchronized
    @JvmStatic
    fun reset() {
        initialized = false
        // Note: We don't reset the arena or method handles to avoid use-after-free issues
    }
}
