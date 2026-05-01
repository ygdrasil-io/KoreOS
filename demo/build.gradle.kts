plugins {
    alias(libs.plugins.kotlin.jvm)
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