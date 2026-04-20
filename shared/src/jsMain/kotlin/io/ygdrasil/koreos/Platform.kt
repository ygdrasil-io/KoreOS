package io.ygdrasil.koreos

actual fun currentPlatform(): PlatformType = PlatformType.JS

actual fun platformName(): String = "JavaScript"

actual fun osVersion(): String = "unknown"

actual fun osFullName(): String = "JavaScript"

actual fun cpuArchitecture(): CPUArchitecture {
    // JavaScript runs on various architectures
    return CPUArchitecture.UNKNOWN
}