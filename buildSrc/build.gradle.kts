import org.jetbrains.kotlin.config.LanguageFeature

repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:${libs.versions.kotlin.get()}")
}

//kotlin.sourceSets.all {
//    languageSettings {
//        enableLanguageFeature(LanguageFeature.ContextReceivers.name)
//    }
//}