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
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$KOTLINX_COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$KOTLINX_SERIALISATION_VERSION")

                api(project(":spiral-base"))
                api(project(":spiral-formats"))
                api(project(":spiral-osl"))

                api("dev.brella:kornea-img:$KORNEA_IMG_VERSION")
            }
        }
        val jvmMain by getting {
            dependencies {
//                api "com.fasterxml.jackson.core:jackson-core:$jackson_version"
//                api "com.fasterxml.jackson.core:jackson-annotations:$jackson_version"
//                api "com.fasterxml.jackson.core:jackson-databind:$jackson_version"
//                api "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jackson_version"
//                api "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version"
//                api "com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version"
//                api "com.fasterxml.jackson.module:jackson-module-parameter-names:$jackson_version"
//                api "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jackson_version"
//                api "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version"

//                api "com.github.kittinunf.fuel:fuel:$fuel_version"
//                api "com.github.kittinunf.fuel:fuel-coroutines:$fuel_version"

                api("io.ktor:ktor-client-apache:$KTOR_VERSION")

                api("com.twelvemonkeys.imageio:imageio-bmp:3.4.1")
                api("com.twelvemonkeys.imageio:imageio-jpeg:3.4.1")
                api("com.twelvemonkeys.imageio:imageio-tga:3.4.1")

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