package io.ygdrasil.koreos

fun main() {
    println("=== KoreOS Platform Information ===")
    println()
    
    println("Basic hello:")
    println(hello())
    println()
    
    println("Detailed system information:")
    println(systemInfo())
    println()
    
    println("Platform type enum: ${currentPlatform()}")
    println("CPU Architecture enum: ${cpuArchitecture()}")
    println()
    
    println("Available platform types:")
    PlatformType.values().forEach { platform ->
        println("  - $platform")
    }
    println()
    
    println("Available CPU architectures:")
    CPUArchitecture.values().forEach { arch ->
        println("  - $arch")
    }
}