package io.ygdrasil.koreos

actual fun currentPlatform(): PlatformType = PlatformType.LINUX

actual fun platformName(): String = "Linux"

actual fun osVersion(): String = System.getProperty("os.version", "unknown")

actual fun osFullName(): String = "Linux"

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