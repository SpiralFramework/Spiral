plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    application
}

application {
    mainClass.set("info.spiralframework.console.jvm.Cockpit")
}

dependencies(libs) {
//    implementation project(":spiral-antlr-pipeline")
    implementation(project(":spiral-base"))
    implementation(project(":spiral-bst"))
    implementation(project(":spiral-formats"))
    implementation(project(":spiral-osl"))
    implementation(project(":spiral-core"))
    implementation(project(":spiral-updater"))

    implementation { kotlinx.coroutines.core }
    implementation { kotlinx.serialization.json }
    implementation { logback.classic }

    implementation("dev.brella:zshk:1.0.0")
//    implementation("dev.brella:knolus-core:2.7.0")

//    antlr("org.antlr:antlr4:4.9.3")
}

kotlin {
    explicitApi()
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
        val optIns = arrayOf("kotlin.RequiresOptIn")

        freeCompilerArgs = ArrayList(freeCompilerArgs).apply {
            optIns.forEach { opt -> add("-opt-in=$opt") }
        }
    }
}

tasks.jar {
    archiveBaseName.set("spiral-${project.name}")
    archiveVersion.set(project.version.toString())
}

tasks.shadowJar {
    mergeServiceFiles()
//    configurations = [project.configurations.runtimeClasspath]
//    manifest {
//        attributes 'Main-Class': mainClassName
//    }

    group = "shadow"

//    archiveBaseName = jar.archiveBaseName.get()
//    archiveAppendix = jar.archiveAppendix.getOrNull()
//    archiveVersion = jar.archiveVersion.get()
//    archiveClassifier = "shadow"
}


//publishing {
//    publications {
//        maven(MavenPublication) {
//            artifact shadowJar
//        }
//    }
//}

//downloadLicenses {
//    includeProjectDependencies = true
//    dependencyConfiguration = 'runtimeClasspath'
//}

//tasks.create<de.undercouch.gradle.tasks.download.Download>("downloadKnolus") {
//    group = "knolus"
//
//    src(
//        listOf(
//            "https://raw.githubusercontent.com/UnderMybrella/Knolus/master/core/src/main/antlr/KnolusLexer.g4",
//            "https://raw.githubusercontent.com/UnderMybrella/Knolus/master/core/src/main/antlr/KnolusModesLexer.g4",
//            "https://raw.githubusercontent.com/UnderMybrella/Knolus/master/core/src/main/antlr/KnolusParser.g4",
//            "https://raw.githubusercontent.com/UnderMybrella/Knolus/master/core/src/main/antlr/LibLexer.g4"
//        )
//    )
//    dest(file("$projectDir/src/main/antlr/"))
//}
//
//tasks.generateGrammarSource {
//    group = "knolus"
//    dependsOn("downloadKnolus")
//
//    arguments.addAll(listOf("-visitor", "-package", "info.spiralframework.antlr.pipeline"))
//    outputDirectory = file("$buildDir/generated-src/antlr/main/info/spiralframework/antlr/pipeline")
//}
//
//tasks.compileKotlin {
//    dependsOn("generateGrammarSource")
//}