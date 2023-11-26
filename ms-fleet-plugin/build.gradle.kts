plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.fleetPlugin)
}

repositories {
    mavenCentral()
    // needed to retrieve `rhizomedb-compiler-plugin` and `noria-compiler-plugin`
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
}

version = "0.1.0"

fleetPlugin {
    id = "titouan.bion.minesweeper"

    metadata {
        readableName = "Minesweeper"
        description = "Play Minesweeper in Fleet"
    }

    fleetRuntime {
        version = "1.27.192"
    }

    layers {
        frontendImpl {
            dependencies {
                api(project(":ms-engine"))
            }
        }
    }
}