// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for ObjectiveCRuntime.
 */
class ObjectiveCRuntimeTest {
    
    @BeforeEach
    fun setUp() {
        // Reset the runtime before each test
        ObjectiveCRuntime.reset()
    }
    
    @AfterEach
    fun tearDown() {
        // Reset the runtime after each test
        ObjectiveCRuntime.reset()
    }
    
    @Test
    fun `test isFFMSupported`() {
        assertTrue(ObjectiveCRuntime.isFFMSupported())
    }
    
    @Test
    fun `test isNotInitialized initially`() {
        assertFalse(ObjectiveCRuntime.isInitialized())
    }
    
    @Test
    fun `test initialize succeeds`() {
        ObjectiveCRuntime.initialize()
        assertTrue(ObjectiveCRuntime.isInitialized())
    }
    
    @Test
    fun `test initialize is idempotent`() {
        ObjectiveCRuntime.initialize()
        ObjectiveCRuntime.initialize()
        assertTrue(ObjectiveCRuntime.isInitialized())
    }
    
    @Test
    fun `test ensureInitialized throws when not initialized`() {
        assertThrows<ObjCInitializationException> {
            ObjectiveCRuntime.ensureInitialized()
        }
    }
    
    @Test
    fun `test ensureInitialized succeeds when initialized`() {
        ObjectiveCRuntime.initialize()
        // Should not throw
        ObjectiveCRuntime.ensureInitialized()
    }
    
    @Test
    fun `test getClass for NSString`() {
        ObjectiveCRuntime.initialize()
        
        val nsStringClass: MemorySegment = ObjectiveCRuntime.getClass("NSString")
        assertNotNull(nsStringClass)
        assertNotEquals(MemorySegment.NULL, nsStringClass)
    }
    
    @Test
    fun `test getClass for NSArray`() {
        ObjectiveCRuntime.initialize()
        
        val nsArrayClass: MemorySegment = ObjectiveCRuntime.getClass("NSArray")
        assertNotNull(nsArrayClass)
        assertNotEquals(MemorySegment.NULL, nsArrayClass)
    }
    
    @Test
    fun `test getClass for NSNumber`() {
        ObjectiveCRuntime.initialize()
        
        val nsNumberClass: MemorySegment = ObjectiveCRuntime.getClass("NSNumber")
        assertNotNull(nsNumberClass)
        assertNotEquals(MemorySegment.NULL, nsNumberClass)
    }
    
    @Test
    fun `test getClassName`() {
        ObjectiveCRuntime.initialize()
        
        val nsStringClass: MemorySegment = ObjectiveCRuntime.getClass("NSString")
        val className: String = ObjectiveCRuntime.getClassName(nsStringClass)
        assertEquals("NSString", className)
    }
    
    @Test
    fun `test registerSelector`() {
        ObjectiveCRuntime.initialize()
        
        val selector: MemorySegment = ObjectiveCRuntime.registerSelector("alloc")
        assertNotNull(selector)
        assertNotEquals(MemorySegment.NULL, selector)
    }
    
    @Test
    fun `test allocateUtf8String`() {
        ObjectiveCRuntime.initialize()
        
        val testString = "Hello, Objective-C!"
        val segment: MemorySegment = ObjectiveCRuntime.allocateUtf8String(testString)
        assertNotNull(segment)
        assertNotEquals(MemorySegment.NULL, segment)
        
        // Verify the string content
        val content: String = ObjectiveCRuntime.getUtf8String(segment)
        assertEquals(testString, content)
    }
    
    @Test
    fun `test getMetaClass`() {
        ObjectiveCRuntime.initialize()
        
        val metaClass: MemorySegment = ObjectiveCRuntime.getMetaClass("NSObject")
        assertNotNull(metaClass)
        assertNotEquals(MemorySegment.NULL, metaClass)
    }
    
    @Test
    fun `test isClass with class`() {
        ObjectiveCRuntime.initialize()
        
        val nsStringClass: MemorySegment = ObjectiveCRuntime.getClass("NSString")
        val isClass: Boolean = ObjectiveCRuntime.isClass(nsStringClass)
        assertTrue(isClass)
    }
}
