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

    sourceSets {
        val commonMain by getting {
            dependencies(libs) {
                implementation { kotlinx.coroutines.core }
                implementation { kotlinx.serialization.json }

                api(project(":spiral-base"))
                api(project(":spiral-formats"))
                api(project(":spiral-osl"))

                api { kornea.img }
            }
        }
        val jvmMain by getting {
            dependencies(libs) {
                api { ktor.client.cio }
                api { bundles.twelvemonkeys.imageio }

//                api "org.greenrobot:eventbus:3.1.1"
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
    }
}

//downloadLicenses {
//    includeProjectDependencies = true
//    dependencyConfiguration = 'jvmRuntimeClasspath'
//}