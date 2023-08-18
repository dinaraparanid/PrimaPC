import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.compose") version "1.5.0-beta02"
}

group = "com.paranid5.prima"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

buildscript {
    dependencies {
        classpath("gradle.plugin.com.github.willir.rust:plugin:0.3.4")
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(group = "net.jthink", name = "jaudiotagger", version = "3.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.arkivanov.decompose:decompose:2.0.1")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:2.0.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "com.dinaraparanid.prima.Main"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "PrimaPC"
            packageVersion = "1.2.1"
        }
    }
}