# KoreOS - Kotlin Multiplatform Library

[![Kotlin Multiplatform CI](https://github.com/ygdrasil-io/KoreOS/actions/workflows/kotlin-multiplatform.yml/badge.svg)](https://github.com/ygdrasil-io/KoreOS/actions/workflows/kotlin-multiplatform.yml)

A Kotlin library for seamless, type-safe interaction with OS-specific APIs.

## Project Structure

```
KoreOS/
├── .github/
│   └── workflows/            # GitHub Actions CI/CD
│       └── kotlin-multiplatform.yml
├── build.gradle.kts          # Root build configuration
├── settings.gradle.kts        # Project settings
├── detekt.yml                # Code quality configuration
├── demo/                     # Demo application
│   ├── build.gradle.kts
│   └── src/main/kotlin/io/ygdrasil/koreos/Demo.kt
├── gradle/
│   └── wrapper/              # Gradle wrapper
├── shared/
│   ├── build.gradle.kts      # Shared module build config
│   └── src/
│       ├── commonMain/       # Common code for all platforms
│       │   └── kotlin/io/ygdrasil/koreos/
│       │       ├── Platform.kt # Common expect declarations & enums
│       │       └── PlatformType.kt
│       ├── jvmMain/          # JVM-specific implementations
│       │   └── kotlin/io/ygdrasil/koreos/
│       │       └── PlatformJvm.kt # JVM actual implementation
│       ├── iosMain/          # iOS-specific implementations
│       │   └── kotlin/io/ygdrasil/koreos/
│       │       └── Platform.kt # iOS actual implementation
│       ├── androidMain/      # Android-specific implementations
│       │   └── kotlin/io/ygdrasil/koreos/
│       │       └── Platform.kt # Android actual implementation
│       ├── jsMain/           # JavaScript-specific implementations
│       │   └── kotlin/io/ygdrasil/koreos/
│       │       └── Platform.kt # JS actual implementation
│       ├── linuxMain/        # Linux-specific implementations
│       │   └── kotlin/io/ygdrasil/koreos/
│       │       └── Platform.kt # Linux actual implementation
│       └── commonTest/       # Common tests
│           └── kotlin/io/ygdrasil/koreos/
│               └── PlatformTest.kt
└── gradlew*                  # Gradle wrapper scripts
```

## Platform Support

- **JVM** (Desktop/Java applications)
- **iOS** (Apple platforms)
- **Android** (to be added)
- **JavaScript** (to be added)

## Usage

The library uses Kotlin's `expect`/`actual` pattern for platform-specific implementations:

```kotlin
// Common code (commonMain)
package io.ygdrasil.koreos
expect fun platformName(): String

// JVM implementation (jvmMain)
package io.ygdrasil.koreos
actual fun platformName(): String = "JVM"

// iOS implementation (iosMain)
package io.ygdrasil.koreos
actual fun platformName(): String = "iOS"
```

## Building

```bash
./gradlew build
```

## Adding New Platforms

To add support for additional platforms:

1. Add the platform target in `shared/build.gradle.kts`
2. Create the corresponding source set directory (e.g., `jsMain`)
3. Implement the `actual` declarations for that platform

## Status

### ✅ Completed
- Android platform support
- JavaScript platform support  
- Linux platform support
- Core OS API wrappers with type-safe enums
- Comprehensive unit tests
- CI/CD pipeline with GitHub Actions
- Code quality tools (ktlint, detekt, Jacoco)

### 🚀 Current Focus
- Finalizing CI/CD configuration
- Platform-specific test implementation
- Enhanced CPU architecture detection
- Extended system information APIs

### 📋 Roadmap
- Memory and CPU usage monitoring
- Disk storage information
- Network status and interfaces
- Battery monitoring (mobile)
- Production-ready release (v1.0.0)