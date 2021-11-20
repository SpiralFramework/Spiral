plugins {
//    id 'java' apply false
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") version "1.6.0" apply false
    id("com.github.johnrengelman.shadow") version "6.0.0" apply false
    id("org.openjfx.javafxplugin") version "0.0.8" apply false
    id("org.beryx.jlink") version "2.12.0" apply false
    id("de.undercouch.download") apply false
    id("com.github.hierynomus.license") version "0.15.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.brella.dev")
    }
}