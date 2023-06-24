import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.HasKotlinDependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream

abstract class DependencyHandlerWithCatalog<F : ExternalModuleDependencyFactory>(val factory: F) {
    abstract fun api(dependencyNotation: Any): Dependency?
    abstract fun implementation(dependencyNotation: Any): Dependency?
    abstract fun compileOnly(dependencyNotation: Any): Dependency?
    abstract fun runtimeOnly(dependencyNotation: Any): Dependency?

    inline fun api(dependencyNotation: F.() -> Any): Dependency? =
        api(factory.dependencyNotation())

    inline fun implementation(dependencyNotation: F.() -> Any): Dependency? =
        implementation(factory.dependencyNotation())

    inline fun compileOnly(dependencyNotation: F.() -> Any): Dependency? =
        compileOnly(factory.dependencyNotation())

    inline fun runtimeOnly(dependencyNotation: F.() -> Any): Dependency? =
        runtimeOnly(factory.dependencyNotation())
}

class KotlinDependencyHandlerWithCatalog<F : ExternalModuleDependencyFactory>(
    factory: F,
    val handler: KotlinDependencyHandler,
) : DependencyHandlerWithCatalog<F>(factory), KotlinDependencyHandler by handler {
    inline fun <SF : ExternalModuleDependencyFactory> of(
        make: F.() -> SF,
        configure: KotlinDependencyHandlerWithCatalog<SF>.() -> Unit,
    ) =
        KotlinDependencyHandlerWithCatalog(factory.make(), handler).configure()
}

class GradleDependencyHandlerWithCatalog<F : ExternalModuleDependencyFactory>(
    factory: F,
    val handler: DependencyHandler,
) : DependencyHandlerWithCatalog<F>(factory), DependencyHandler by handler {
    override fun api(dependencyNotation: Any): Dependency? =
        handler.add("api", dependencyNotation)

    override fun implementation(dependencyNotation: Any): Dependency? =
        handler.add("implementation", dependencyNotation)

    override fun compileOnly(dependencyNotation: Any): Dependency? =
        handler.add("compileOnly", dependencyNotation)

    override fun runtimeOnly(dependencyNotation: Any): Dependency? =
        handler.add("runtimeOnly", dependencyNotation)

    inline fun <SF : ExternalModuleDependencyFactory> of(
        make: F.() -> SF,
        configure: GradleDependencyHandlerWithCatalog<SF>.() -> Unit,
    ) =
        GradleDependencyHandlerWithCatalog(factory.make(), handler).configure()
}

public inline fun <F : ExternalModuleDependencyFactory> HasKotlinDependencies.dependencies(
    factory: F,
    crossinline configure: KotlinDependencyHandlerWithCatalog<F>.() -> Unit,
) = dependencies {
    KotlinDependencyHandlerWithCatalog(factory, this).configure()
}

public inline fun <F : ExternalModuleDependencyFactory> Project.dependencies(
    factory: F,
    crossinline configure: GradleDependencyHandlerWithCatalog<F>.() -> Unit,
) = GradleDependencyHandlerWithCatalog(factory, dependencies).configure()

public inline fun parseProcess(crossinline factory: () -> ProcessBuilder): () -> ByteArray =
    {
        val process = factory().start()

        val processOutput = BufferedInputStream(process.inputStream)
        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(8192)

        while (process.isAlive) {
            val read = processOutput.read(buffer)
            if (read != -1) baos.write(buffer, 0, read)
        }

        while (true) {
            val read = processOutput.read(buffer)
            if (read == -1) break

            baos.write(buffer, 0, read)
        }

        baos.toByteArray()
    }