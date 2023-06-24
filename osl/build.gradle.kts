plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies(libs) {
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$KOTLINX_COROUTINES_VERSION")
//                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$KOTLINX_SERIALISATION_VERSION")

                api { kotlinx.coroutines.core }
                api { kotlinx.serialization.json }

                implementation(project(":spiral-base"))
                implementation(project(":spiral-formats"))
//                api project(":osl-json")
            }
        }
        val jvmMain by getting {
            dependencies(libs) {
//                implementation kotlin('stdlib-jdk8')

                api(project(":spiral-antlr-osl-java"))
                implementation { logback.classic }
//                implementation("ch.qos.logback:logback-classic:$LOGBACK_VERSION")
            }
        }

        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
                explicitApi()

                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlin.contracts.ExperimentalContracts")
            }
        }
//        jsMain {
//            dependencies {
//                implementation kotlin('stdlib-js')
//                implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.0-RC'
//            }
//        }
//        jsTest {
//            dependencies {
//                implementation kotlin('test-js')
//            }
//        }
//        mingwMain {
//        }
//        mingwTest {
//        }
    }
}

//tasks.jvmJar {
//    archiveBaseName.set("spiral-$project.name")
//    archiveVersion.set(project_version)
//}

//task jvmShadowJar(type: ShadowJar, dependsOn: [jvmJar]) {
//    mergeServiceFiles()
//    from jvmJar.archiveFile
//    configurations = [project.configurations.jvmRuntimeClasspath]
//    group = "shadow"
//
//    manifest {
//        attributes 'Main-Class': mainClassName
//    }
//
//    archiveBaseName.set(jvmJar.archiveBaseName.get())
//    archiveAppendix.set(jvmJar.archiveAppendix.get())
//    archiveVersion.set(jvmJar.archiveVersion.get())
//    archiveClassifier.set("shadow")
//}

//generateGrammarSource {
//    arguments += ['-visitor', '-package', 'info.spiralframework.antlr.osl']
//    outputDirectory = file("$buildDir/generated-src-mpp/antlr/jvmMain/info/spiralframework/antlr/osl")
//}

//downloadLicenses {
//    includeProjectDependencies = true
//    dependencyConfiguration = 'jvmRuntimeClasspath'
//}