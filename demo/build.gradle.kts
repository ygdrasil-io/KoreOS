plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("io.ygdrasil.koreos.DemoKt")
}

dependencies {
    implementation(project(":shared"))
}