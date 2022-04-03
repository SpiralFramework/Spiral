plugins {
    kotlin("multiplatform")
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
                implementation(kotlin("reflect"))
                implementation(KOTLINX_COROUTINES_CORE)

                api(project(":spiral-formats"))
            }
        }

        all {
            languageSettings.apply {
                enableLanguageFeature("InlineClasses")
                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlin.contracts.ExperimentalContracts")
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