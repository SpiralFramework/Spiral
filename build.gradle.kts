plugins {
//    id 'java' apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.mpp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.openjfx) apply false
    alias(libs.plugins.jlink) apply false
    alias(libs.plugins.licenseCheck) apply false
    alias(libs.plugins.kornea) apply false

    `maven-publish`
}

val GIT_VERSION = parseProcess { ProcessBuilder("git", "rev-parse", "--short", "HEAD") }().decodeToString().trim()

allprojects {
    apply<MavenPublishPlugin>()
    group = "info.spiralframework"
    version = GIT_VERSION
    
    repositories {
        mavenCentral()
        maven("https://maven.brella.dev")
    }

    configure<PublishingExtension> {
        repositories {
            maven(url = "${rootProject.buildDir}/repo")
        }
    }
}