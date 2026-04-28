package io.ygdrasil.koreos

enum class PlatformType {
    JVM, IOS, ANDROID, JS, LINUX, WINDOWS, MACOS, UNKNOWN
}

enum class CPUArchitecture {
    X86, X86_64, ARM, ARM64, UNKNOWN
}

expect fun currentPlatform(): PlatformType

expect fun platformName(): String

expect fun osVersion(): String

expect fun osFullName(): String

expect fun cpuArchitecture(): CPUArchitecture

fun hello(): String = "Hello from ${platformName()}"

fun systemInfo(): String = """
    Platform: ${platformName()}
    OS: ${osFullName()} ${osVersion()}
    Architecture: ${cpuArchitecture()}
    Type: ${currentPlatform()}
""".trimIndent()