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
    
    // Enable native access for FFM (required for Java 25+)
    jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    
    // Inherit library path from environment
    // GitHub Actions workflow sets LD_LIBRARY_PATH/DYLD_LIBRARY_PATH
    val os = System.getProperty("os.name").lowercase()
    if (os.contains("mac") || os.contains("darwin")) {
        val currentPath = System.getenv("DYLD_LIBRARY_PATH") ?: ""
        if (currentPath.isNotEmpty()) {
            environment("DYLD_LIBRARY_PATH", currentPath)
        }
    } else if (os.contains("linux")) {
        val currentPath = System.getenv("LD_LIBRARY_PATH") ?: ""
        if (currentPath.isNotEmpty()) {
            environment("LD_LIBRARY_PATH", currentPath)
        }
    }
}
