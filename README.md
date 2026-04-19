# KoreOS - Kotlin Multiplatform Library

A Kotlin library for seamless, type-safe interaction with OS-specific APIs.

## Project Structure

```
KoreOS/
├── build.gradle.kts          # Root build configuration
├── settings.gradle.kts        # Project settings
├── gradle/
│   └── wrapper/              # Gradle wrapper
├── shared/
│   ├── build.gradle.kts      # Shared module build config
│   └── src/
│       ├── commonMain/       # Common code for all platforms
│       │   └── kotlin/io/ygdrasil/koreos/
│       │       └── Platform.kt # Common expect declarations
│       ├── jvmMain/          # JVM-specific implementations
│       │   └── kotlin/io/ygdrasil/koreos/
│       │       └── Platform.kt # JVM actual implementation
│       ├── iosMain/          # iOS-specific implementations
│       │   └── kotlin/io/ygdrasil/koreos/
│       │       └── Platform.kt # iOS actual implementation
│       └── commonTest/       # Common tests
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

## Next Steps

- Add Android platform support
- Add JavaScript platform support
- Implement core OS API wrappers
- Add comprehensive testing
- Set up CI/CD pipeline