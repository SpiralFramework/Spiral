plugins {
    kotlinMultiplatform()
    kotlinSerialisation()
}

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
            }
        }
    }
    js()
//    js {
//        browser {
//        }
//        nodejs {
//        }
//    }
//    // For ARM, should be changed to iosArm32 or iosArm64
//    // For Linux, should be changed to e.g. linuxX64
//    // For MacOS, should be changed to e.g. macosX64
//    // For Windows, should be changed to e.g. mingwX64
//    mingwX64("mingw") {
//    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(KOTLINX_COROUTINES_CORE)
                implementation(KOTLINX_SERIALISATION_JSON)

                api("dev.brella:kornea-annotations:$KORNEA_ANNOTATIONS_VERSION")
                api("dev.brella:kornea-errors:$KORNEA_ERRORS_VERSION")
                api("dev.brella:kornea-io:$KORNEA_IO_VERSION")
                api("dev.brella:kornea-toolkit:$KORNEA_TOOLKIT_VERSION")
            }
        }
        val jvmMain by getting {
            dependencies {
                api("org.slf4j:slf4j-api:$SFL4J_VERSION")
                api("dev.dirs:directories:21")
            }
        }

        all {
            languageSettings.apply {
                enableLanguageFeature("InlineClasses")
                useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
                useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
                useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
            }
        }

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

//downloadLicenses {
//    includeProjectDependencies = true
//    dependencyConfiguration = 'jvmRuntimeClasspath'
//}