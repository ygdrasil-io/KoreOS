plugins {
    kotlin("multiplatform") version "2.3.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    id("jacoco")
}

repositories {
    mavenCentral()
    google()
}
