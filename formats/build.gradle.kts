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
    js()
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$KOTLINX_COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$KOTLINX_SERIALISATION_VERSION")

                api("com.soywiz.korlibs.krypto:krypto:2.2.0")

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