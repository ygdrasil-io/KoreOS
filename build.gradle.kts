plugins {
    kotlin("multiplatform") version "2.3.20" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.1" apply false
}

repositories {
    mavenCentral()
    google()
}
