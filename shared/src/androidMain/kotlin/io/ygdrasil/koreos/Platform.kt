package io.ygdrasil.koreos

import android.os.Build

actual fun currentPlatform(): PlatformType = PlatformType.ANDROID

actual fun platformName(): String = "Android"

actual fun osVersion(): String = Build.VERSION.RELEASE

actual fun osFullName(): String = "Android"

actual fun cpuArchitecture(): CPUArchitecture {
    val arch = Build.CPU_ABI.lowercase()
    return when {
        arch.contains("x86_64") -> CPUArchitecture.X86_64
        arch.contains("x86") -> CPUArchitecture.X86
        arch.contains("arm64") -> CPUArchitecture.ARM64
        arch.contains("arm") -> CPUArchitecture.ARM
        else -> CPUArchitecture.UNKNOWN
    }
}