plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    alias(libs.plugins.ktlint)
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
    val os = System.getProperty("os.name").lowercase()
    val nativeArgs = mutableListOf("--enable-native-access=ALL-UNNAMED")

    // Set library path - must be passed via environment for native linker to work
    if (os.contains("mac") || os.contains("darwin")) {
        val dyldPath = System.getenv("DYLD_LIBRARY_PATH") ?: "/opt/homebrew/opt/llvm/lib"
        environment("DYLD_LIBRARY_PATH", dyldPath)
        nativeArgs.add("-Djava.library.path=$dyldPath")
    } else if (os.contains("linux")) {
        val ldPath = System.getenv("LD_LIBRARY_PATH") ?: "/usr/lib/llvm-17/lib"
        environment("LD_LIBRARY_PATH", ldPath)
        nativeArgs.add("-Djava.library.path=$ldPath")
    } else if (os.contains("win")) {
        // On Windows, use PATH and java.library.path
        // Check JAVA_LIBRARY_PATH first (set by GitHub Actions), then LLVM_PATH
        val javaLibPath = System.getenv("JAVA_LIBRARY_PATH")
            ?: (System.getenv("LLVM_PATH")?.let { "$it\\lib;$it\\bin" } ?: "C:\\Program Files\\LLVM\\lib;C:\\Program Files\\LLVM\\bin")
        nativeArgs.add("-Djava.library.path=$javaLibPath")
    }

    jvmArgs = nativeArgs
}
