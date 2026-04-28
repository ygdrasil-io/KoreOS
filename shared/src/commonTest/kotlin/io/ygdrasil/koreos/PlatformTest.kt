package io.ygdrasil.koreos

import kotlin.test.Test
import kotlin.test.assertNotNull

class PlatformTest {
    
    @Test
    fun testPlatformFunctionsExist() {
        // Test that all platform functions are accessible
        assertNotNull(platformName())
        assertNotNull(osVersion())
        assertNotNull(osFullName())
        assertNotNull(cpuArchitecture())
        assertNotNull(currentPlatform())
    }
    
    @Test
    fun testHelloFunction() {
        val helloMessage = hello()
        assert(helloMessage.contains("Hello from"))
    }
    
    @Test
    fun testSystemInfoFunction() {
        val systemInfo = systemInfo()
        assert(systemInfo.isNotEmpty())
        assert(systemInfo.contains("Platform:"))
        assert(systemInfo.contains("OS:"))
        assert(systemInfo.contains("Architecture:"))
        assert(systemInfo.contains("Type:"))
    }
    
    @Test
    fun testEnumValues() {
        // Test that our enums have the expected values
        val platformTypes = PlatformType.values()
        assert(platformTypes.contains(PlatformType.JVM))
        assert(platformTypes.contains(PlatformType.IOS))
        assert(platformTypes.contains(PlatformType.ANDROID))
        assert(platformTypes.contains(PlatformType.JS))
        assert(platformTypes.contains(PlatformType.LINUX))
        
        val cpuArchitectures = CPUArchitecture.values()
        assert(cpuArchitectures.contains(CPUArchitecture.X86))
        assert(cpuArchitectures.contains(CPUArchitecture.X86_64))
        assert(cpuArchitectures.contains(CPUArchitecture.ARM))
        assert(cpuArchitectures.contains(CPUArchitecture.ARM64))
    }
}