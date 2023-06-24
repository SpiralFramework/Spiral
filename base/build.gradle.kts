plugins {
    alias(libs.plugins.kotlin.mpp)
    alias(libs.plugins.kotlin.serialization)
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
                implementation { kotlinx.coroutines.core }
                implementation { kotlinx.serialization.json }

                api { kornea.annotations }
                api { kornea.errors }
                api { kornea.io }
                api { kornea.toolkit }
            }
        }
        val jvmMain by getting {
            dependencies(libs) {
                api { slf4j.api }
                api { directories }
            }
        }

        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
                explicitApi()
            }
        }
    }
}

//downloadLicenses {
//    includeProjectDependencies = true
//    dependencyConfiguration = 'jvmRuntimeClasspath'
//}