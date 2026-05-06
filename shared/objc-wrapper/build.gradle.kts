plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
}

group = "io.ygdrasil.koreos"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.3.20")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Only run these tests on macOS
    onlyIf {
        val os = System.getProperty("os.name").lowercase()
        os.contains("mac") || os.contains("darwin")
    }

    // Enable native access for FFM (required for Java 25+)
    val os = System.getProperty("os.name").lowercase()
    val nativeArgs = mutableListOf("--enable-native-access=ALL-UNNAMED")

    // Set library path for libobjc.dylib on macOS
    if (os.contains("mac") || os.contains("darwin")) {
        val dyldPath = System.getenv("DYLD_LIBRARY_PATH")
        if (dyldPath != null) {
            // Include existing DYLD_LIBRARY_PATH and ensure /usr/lib is present
            environment("DYLD_LIBRARY_PATH", "$dyldPath:/usr/lib")
            nativeArgs.add("-Djava.library.path=$dyldPath:/usr/lib")
        } else {
            // If not set, use /usr/lib
            environment("DYLD_LIBRARY_PATH", "/usr/lib")
            nativeArgs.add("-Djava.library.path=/usr/lib")
        }
    }

    jvmArgs = nativeArgs
}
