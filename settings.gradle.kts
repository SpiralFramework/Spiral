pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven ("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

enableFeaturePreview("GRADLE_METADATA")

rootProject.name = "spiral"

include(":antlr-osl-java")
include(":antlr-pipeline")
include(":base")
//include ':base-extended'
include(":bst")
include(":core")
include(":console")
include(":gui")
include(":harmony")
include(":formats")
include(":osl")
include(":updater")
include(":tpod")

rootProject.children.forEach { child -> child.name = "spiral-${child.name}"}