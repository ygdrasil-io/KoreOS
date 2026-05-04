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
    
    // Set library path for LLVM Clang specifically
    val os = System.getProperty("os.name").lowercase()
    if (os.contains("mac") || os.contains("darwin")) {
        // Use LLVM Clang from Homebrew, not Apple Clang
        val llvmPath = "/opt/homebrew/opt/llvm/lib"
        val currentPath = System.getenv("DYLD_LIBRARY_PATH") ?: ""
        environment("DYLD_LIBRARY_PATH", "$llvmPath:$currentPath")
    } else if (os.contains("linux")) {
        val currentPath = System.getenv("LD_LIBRARY_PATH") ?: ""
        environment("LD_LIBRARY_PATH", "/usr/lib/llvm-17/lib:$currentPath")
    }
}
