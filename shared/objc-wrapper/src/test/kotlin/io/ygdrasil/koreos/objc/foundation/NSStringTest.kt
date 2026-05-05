// SPDX-License-Identifier: MIT
package io.ygdrasil.koreos.objc.foundation

import io.ygdrasil.koreos.objc.ObjCObject
import io.ygdrasil.koreos.objc.ObjectiveCRuntime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for NSString wrapper.
 */
class NSStringTest {
    
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
    fun `test fromString`() {
        val testString = "Hello, Kotlin!"
        val nsString: ObjCObject = NSString.fromString(testString)
        assertNotEquals(null, nsString)
        assertTrue(nsString.isValid())
    }
    
    @Test
    fun `test empty string`() {
        val nsString: ObjCObject = NSString.empty()
        assertNotEquals(null, nsString)
        assertTrue(nsString.isValid())
        assertTrue(nsString.isEmpty())
    }
    
    @Test
    fun `test toKotlinString`() {
        val testString = "Test String"
        val nsString: ObjCObject = NSString.fromString(testString)
        val kotlinString = nsString.toKotlinString()
        assertEquals(testString, kotlinString)
    }
    
    @Test
    fun `test length`() {
        val testString = "Hello"
        val nsString: ObjCObject = NSString.fromString(testString)
        assertEquals(testString.length, nsString.length())
    }
    
    @Test
    fun `test isEmpty for empty string`() {
        val nsString: ObjCObject = NSString.empty()
        assertTrue(nsString.isEmpty())
    }
    
    @Test
    fun `test isEmpty for non-empty string`() {
        val nsString: ObjCObject = NSString.fromString("Not empty")
        assertFalse(nsString.isEmpty())
    }
    
    @Test
    fun `test charAt`() {
        val testString = "Hello"
        val nsString: ObjCObject = NSString.fromString(testString)
        assertEquals('H', nsString.charAt(0))
        assertEquals('e', nsString.charAt(1))
        assertEquals('o', nsString.charAt(4))
    }
    
    @Test
    fun `test plus`() {
        val str1: ObjCObject = NSString.fromString("Hello")
        val str2: ObjCObject = NSString.fromString("World")
        val result: ObjCObject = str1.plus(str2)
        assertEquals("HelloWorld", result.toKotlinString())
    }
    
    @Test
    fun `test isEqualToString`() {
        val str1: ObjCObject = NSString.fromString("Test")
        val str2: ObjCObject = NSString.fromString("Test")
        val str3: ObjCObject = NSString.fromString("Different")
        
        assertTrue(str1.isEqualToString(str2))
        assertFalse(str1.isEqualToString(str3))
    }
    
    @Test
    fun `test contains`() {
        val str: ObjCObject = NSString.fromString("Hello World")
        val substring: ObjCObject = NSString.fromString("World")
        val notSubstring: ObjCObject = NSString.fromString("Kotlin")
        
        assertTrue(str.contains(substring))
        assertFalse(str.contains(notSubstring))
    }
    
    @Test
    fun `test hasPrefix`() {
        val str: ObjCObject = NSString.fromString("Hello World")
        val prefix: ObjCObject = NSString.fromString("Hello")
        val notPrefix: ObjCObject = NSString.fromString("World")
        
        assertTrue(str.hasPrefix(prefix))
        assertFalse(str.hasPrefix(notPrefix))
    }
    
    @Test
    fun `test hasSuffix`() {
        val str: ObjCObject = NSString.fromString("Hello World")
        val suffix: ObjCObject = NSString.fromString("World")
        val notSuffix: ObjCObject = NSString.fromString("Hello")
        
        assertTrue(str.hasSuffix(suffix))
        assertFalse(str.hasSuffix(notSuffix))
    }
    
    @Test
    fun `test uppercaseString`() {
        val str: ObjCObject = NSString.fromString("hello")
        val upper: ObjCObject = str.uppercaseString()
        assertEquals("HELLO", upper.toKotlinString())
    }
    
    @Test
    fun `test lowercaseString`() {
        val str: ObjCObject = NSString.fromString("HELLO")
        val lower: ObjCObject = str.lowercaseString()
        assertEquals("hello", lower.toKotlinString())
    }
    
    @Test
    fun `test substringFromIndex`() {
        val str: ObjCObject = NSString.fromString("Hello World")
        val substring: ObjCObject = str.substringFromIndex(6)
        assertEquals("World", substring.toKotlinString())
    }
    
    @Test
    fun `test toString`() {
        val str: ObjCObject = NSString.fromString("Test")
        val strRep = str.toString()
        assertTrue(strRep.contains("Test"))
    }
}
