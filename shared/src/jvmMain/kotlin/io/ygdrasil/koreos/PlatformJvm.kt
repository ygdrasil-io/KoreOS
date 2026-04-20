package io.ygdrasil.koreos

import io.ygdrasil.koreos.PlatformType
import io.ygdrasil.koreos.CPUArchitecture

actual fun currentPlatform(): PlatformType = PlatformType.JVM

actual fun platformName(): String = "JVM"

actual fun osVersion(): String = System.getProperty("os.version", "unknown")

actual fun osFullName(): String = System.getProperty("os.name", "unknown")

actual fun cpuArchitecture(): CPUArchitecture {
    val arch = System.getProperty("os.arch", "unknown").lowercase()
    return when {
        arch.contains("amd64") || arch.contains("x86_64") -> CPUArchitecture.X86_64
        arch.contains("x86") -> CPUArchitecture.X86
        arch.contains("aarch64") || arch.contains("arm64") -> CPUArchitecture.ARM64
        arch.contains("arm") -> CPUArchitecture.ARM
        else -> CPUArchitecture.UNKNOWN
    }
}