package io.ygdrasil.koreos

import platform.UIKit.UIDevice

actual fun currentPlatform(): PlatformType = PlatformType.IOS

actual fun platformName(): String = "iOS"

actual fun osVersion(): String = UIDevice.currentDevice.systemVersion

actual fun osFullName(): String = "iOS"

actual fun cpuArchitecture(): CPUArchitecture {
    // iOS devices are typically ARM-based
    return CPUArchitecture.ARM64
}