
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.gradle.plugin.use.PluginDependenciesSpec
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

const val KOTLIN_VERSION = "1.6.0"
const val KOTLINX_COROUTINES_VERSION = "1.5.2"
const val KOTLINX_SERIALISATION_VERSION = "1.3.1"
const val KTOR_VERSION = "1.6.3"
const val LOGBACK_VERSION = "1.2.3"
const val SFL4J_VERSION = "1.7.32"

const val KORNEA_ANNOTATIONS_VERSION = "1.1.0-alpha"
const val KORNEA_APOLLO_VERSION = "1.0.6-alpha"
const val KORNEA_ERRORS_VERSION = "2.2.4-alpha"
const val KORNEA_IMG_VERSION = "1.3.0-alpha"
const val KORNEA_IO_VERSION = "5.4.3-alpha"
const val KORNEA_MODELLING_VERSION = "1.1.0-alpha"
const val KORNEA_TOOLKIT_VERSION = "3.4.2-alpha"

const val KOTLINX_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$KOTLINX_COROUTINES_VERSION"
const val KOTLINX_SERIALISATION_CORE = "org.jetbrains.kotlinx:kotlinx-serialization-core:$KOTLINX_SERIALISATION_VERSION"
const val KOTLINX_SERIALISATION_JSON = "org.jetbrains.kotlinx:kotlinx-serialization-json:$KOTLINX_SERIALISATION_VERSION"

inline fun PluginDependenciesSpec.kotlinMultiplatform() =
    kotlin("multiplatform")

inline fun PluginDependenciesSpec.kotlinJVM() =
    kotlin("jvm")

inline fun PluginDependenciesSpec.kotlinSerialisation() =
    kotlin("plugin.serialization")

inline fun Project.defineSourceSet(newName: String, dependsOn: List<String>, noinline includedIn: (String) -> Boolean) =
    project.extensions.getByType<KotlinMultiplatformExtension>()
        .defineSourceSet(newName, dependsOn, includedIn)

fun KotlinMultiplatformExtension.defineSourceSet(
    newName: String,
    dependsOn: String,
    includedIn: List<String>,
    config: (KotlinSourceSet.() -> Unit)? = null
) =
    defineSourceSet(newName, listOf(dependsOn), { it in includedIn }, config)

fun KotlinMultiplatformExtension.defineSourceSet(
    newName: String,
    dependsOn: List<String>,
    includedIn: List<String>,
    config: (KotlinSourceSet.() -> Unit)? = null
) =
    defineSourceSet(newName, dependsOn, { it in includedIn }, config)

fun KotlinMultiplatformExtension.defineSourceSet(
    newName: String,
    dependsOn: String,
    vararg includedIn: String,
    config: (KotlinSourceSet.() -> Unit)? = null
) =
    defineSourceSet(newName, listOf(dependsOn), { it in includedIn }, config)

fun KotlinMultiplatformExtension.defineSourceSet(
    newName: String,
    dependsOn: List<String>,
    vararg includedIn: String,
    config: (KotlinSourceSet.() -> Unit)? = null
) =
    defineSourceSet(newName, dependsOn, { it in includedIn }, config)

fun KotlinMultiplatformExtension.defineSourceSet(
    newName: String,
    dependsOn: String,
    includedIn: String,
    config: (KotlinSourceSet.() -> Unit)? = null
) =
    defineSourceSet(newName, listOf(dependsOn), { includedIn == it }, config)

fun KotlinMultiplatformExtension.defineSourceSet(
    newName: String,
    dependsOn: List<String>,
    includedIn: String,
    config: (KotlinSourceSet.() -> Unit)? = null
) =
    defineSourceSet(newName, dependsOn, { includedIn == it }, config)

fun KotlinMultiplatformExtension.defineSourceSet(
    newName: String,
    dependsOn: String,
    config: (KotlinSourceSet.() -> Unit)? = null
) =
    defineSourceSet(newName, listOf(dependsOn), null, config)

fun KotlinMultiplatformExtension.defineSourceSet(
    newName: String,
    dependsOn: List<String>,
    config: (KotlinSourceSet.() -> Unit)? = null
) =
    defineSourceSet(newName, dependsOn, null, config)

fun KotlinMultiplatformExtension.defineSourceSet(
    newName: String,
    dependsOn: List<String>,
    includedIn: ((String) -> Boolean)? = null,
    config: (KotlinSourceSet.() -> Unit)? = null
) {
    for (suffix in listOf("Main", "Test")) {
        val newSourceSet = sourceSets.maybeCreate("$newName$suffix")
        dependsOn.forEach { dep -> newSourceSet.dependsOn(sourceSets["$dep$suffix"]) }
        sourceSets.forEach { currentSourceSet ->
            val currentName = currentSourceSet.name
            if (currentName.endsWith(suffix)) {
                val prefix = currentName.removeSuffix(suffix)
                if (includedIn?.invoke(prefix) == true) currentSourceSet.dependsOn(newSourceSet)
            }
        }

        config?.invoke(newSourceSet)
    }
}

//fun KotlinMultiplatformExtension.addCompilerArgs(vararg compilerArgs: String) =
//    targets.all {
//        compilations.all {
//            kotlinOptions {
//                freeCompilerArgs = freeCompilerArgs + compilerArgs
//            }
//        }
//    }

inline fun Project.multiplatform(noinline configuration: KotlinMultiplatformExtension.() -> Unit): Unit =
    configure(configuration)

//inline fun