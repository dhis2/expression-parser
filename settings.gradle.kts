pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
    id("com.mooltiverse.oss.nyx") version "2.5.2"
}

rootProject.name = "expression-parser"
