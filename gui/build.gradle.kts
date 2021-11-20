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

    api(project(":spiral-antlr-pipeline"))
    api(project(":spiral-bst"))
    api(project(":spiral-core"))

    api("dev.brella:kornea-img:$KORNEA_IMG_VERSION")

    implementation("ch.qos.logback:logback-classic:$LOGBACK_VERSION")
}
//        all {
//            languageSettings {
//                enableLanguageFeature("InlineClasses")
//                useExperimentalAnnotation('kotlin.ExperimentalUnsignedTypes')
//                useExperimentalAnnotation('kotlin.ExperimentalStdlibApi')
//                useExperimentalAnnotation('kotlin.contracts.ExperimentalContracts')
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