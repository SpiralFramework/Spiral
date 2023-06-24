plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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
        commonMain {
            dependencies(libs) {
                implementation { kotlinx.coroutines.core }
                implementation { kotlinx.serialization.json }

                api { korlibs.krypto }

                api(project(":spiral-base"))
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