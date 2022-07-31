plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
    id("org.openjfx.javafxplugin")
}

application {
    mainClassName = "info.spiralframework.gui.jvm.CockpitKt"
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$KOTLINX_COROUTINES_VERSION")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$KOTLINX_COROUTINES_VERSION")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$KOTLINX_SERIALISATION_VERSION")

    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-bootstrapicons-pack:12.3.1")

    implementation("uk.co.caprica:vlcj:4.7.3")
    implementation("uk.co.caprica:vlcj-javafx:1.0.3")

    api(project(":spiral-bst"))
    api(project(":spiral-core"))

    api("dev.brella:kornea-img:$KORNEA_IMG_VERSION")

    implementation("ch.qos.logback:logback-classic:$LOGBACK_VERSION")
}
//        all {
//            languageSettings {
//                enableLanguageFeature("InlineClasses")
//                optIn('kotlin.ExperimentalUnsignedTypes')
//                optIn('kotlin.ExperimentalStdlibApi')
//                optIn('kotlin.contracts.ExperimentalContracts')
//            }
//        }

javafx {
    version = "11"
    modules = listOf("javafx.controls", "javafx.graphics")
    configuration = "implementation"
}

tasks.shadowJar {
//    baseName = jar.baseName
//    appendix = jar.appendix
//    version = jar.version

    classifier = "shadow"
    mergeServiceFiles()

    group = "shadow"
}

tasks.jar {
    archiveBaseName.set("spiral-${project.name}")
//    archiveVersion.set(version)
}

//publishing {
//    publications {
//        maven(MavenPublication) {
//            artifact shadowJar
//        }
//    }
//}