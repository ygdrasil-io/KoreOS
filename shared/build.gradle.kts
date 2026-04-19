plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        complements += listOf("junit")
    }
    ios {
        binaries {
            framework {
                baseName = "KoreOS"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Ajoutez ici les dépendances communes
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        // Connecter les source sets aux cibles
        jvm().compilations.getByName("main").defaultSourceSet.dependsOn(commonMain)
        jvm().compilations.getByName("test").defaultSourceSet.dependsOn(commonTest)
        ios().compilations.getByName("main").defaultSourceSet.dependsOn(commonMain)
        ios().compilations.getByName("test").defaultSourceSet.dependsOn(commonTest)
    }
}

dependencies {
    // Dépendances communes à toutes les plateformes
}