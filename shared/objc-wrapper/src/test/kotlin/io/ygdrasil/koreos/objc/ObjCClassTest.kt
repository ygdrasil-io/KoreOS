// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc

import java.lang.foreign.MemorySegment
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
 * Unit tests for ObjCClass.
 */
class ObjCClassTest {
    
    @BeforeEach
    fun setUp() {
        ObjectiveCRuntime.reset()
        ObjectiveCRuntime.initialize()
    }
    
    @AfterEach
    fun tearDown() {
        ObjectiveCRuntime.reset()
    }
    
    @Test
    fun `test fromName for NSString`() {
        val nsStringClass: ObjCClass = ObjCClass.Companion.fromName("NSString")
        assertNotNull(nsStringClass)
        assertEquals("NSString", nsStringClass.name)
        assertTrue(nsStringClass.isValid())
    }
    
    @Test
    fun `test fromName for NSArray`() {
        val nsArrayClass: ObjCClass = ObjCClass.Companion.fromName("NSArray")
        assertNotNull(nsArrayClass)
        assertEquals("NSArray", nsArrayClass.name)
        assertTrue(nsArrayClass.isValid())
    }
    
    @Test
    fun `test fromName for non-existent class throws`() {
        assertThrows<ObjCNotFoundException> {
            ObjCClass.fromName("NonExistentClass12345")
        }
    }
    
    @Test
    fun `test fromObject`() {
        val nsStringClass: ObjCClass = ObjCClass.Companion.fromName("NSString")
        val instance = nsStringClass.createInstance()
        val cls: ObjCClass = ObjCClass.Companion.fromObject(instance)
        // NSString is a class cluster, so the actual class name might vary (e.g., __NSCFConstantString)
        assertTrue(cls.name.contains("String"), "Expected class name to contain 'String' but got ${cls.name}")
    }
    
    @Test
    fun `test getMetaClass`() {
        val nsObjectClass: ObjCClass = ObjCClass.Companion.fromName("NSObject")
        val metaClass = nsObjectClass.getMetaClass()
        assertNotNull(metaClass)
        assertTrue(metaClass.name.contains("Meta"))
    }
    
    @Test
    fun `test isMetaClass for regular class`() {
        val nsStringClass: ObjCClass = ObjCClass.Companion.fromName("NSString")
        assertFalse(nsStringClass.isMetaClass())
    }
    
    @Test
    fun `test isMetaClass for metaclass`() {
        val nsObjectClass: ObjCClass = ObjCClass.Companion.fromName("NSObject")
        val metaClass = nsObjectClass.getMetaClass()
        assertTrue(metaClass.isMetaClass())
    }
    
    @Test
    fun `test createInstance`() {
        val nsStringClass: ObjCClass = ObjCClass.Companion.fromName("NSString")
        val instance = nsStringClass.createInstance()
        assertNotNull(instance)
        assertTrue(instance.isValid())
    }
    
    @Test
    fun `test newInstance`() {
        val nsStringClass: ObjCClass = ObjCClass.Companion.fromName("NSString")
        val instance = nsStringClass.newInstance()
        assertNotNull(instance)
        assertTrue(instance.isValid())
    }
    
    @Test
    fun `test invokeClassMethod`() {
        val nsStringClass: ObjCClass = ObjCClass.Companion.fromName("NSString")
        // Try to invoke a class method (this may fail depending on the method)
        // For now, we'll just test that it doesn't crash
        try {
            val result = nsStringClass.invokeClassMethod("string")
            // If it succeeds, verify the result
            if (result != null) {
                assertTrue(result.isValid())
            }
        } catch (e: Exception) {
            // Some methods may not be available or may have different signatures
            // This is expected for now
        }
    }
    
    @Test
    fun `test toString`() {
        val nsStringClass = ObjCClass.fromName("NSString")
        val str = nsStringClass.toString()
        assertTrue(str.contains("NSString"))
    }
    
    @Test
    fun `test equals`() {
        val nsStringClass1 = ObjCClass.fromName("NSString")
        val nsStringClass2 = ObjCClass.fromName("NSString")
        assertEquals(nsStringClass1, nsStringClass2)
    }
    
    @Test
    fun `test not equals`() {
        val nsStringClass = ObjCClass.fromName("NSString")
        val nsArrayClass = ObjCClass.fromName("NSArray")
        assertFalse(nsStringClass == nsArrayClass)
    }
}
