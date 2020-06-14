package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralModuleBase
import info.spiralframework.base.common.io.SpiralResourceLoader
import org.abimon.kornea.annotations.ExperimentalKorneaIO
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.korneaNotFound
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.jvm.JVMDataSource
import org.abimon.kornea.io.jvm.files.AsyncFileDataSource
import java.io.File
import kotlin.reflect.KClass

@ExperimentalUnsignedTypes
actual class DefaultSpiralResourceLoader actual constructor() : SpiralResourceLoader {
    companion object {
        val spiralModules = arrayOf(
                "antlr-json",
                "base",
                "base-extended",
                "core",
                "console",
                "gui",
                "formats",
                "osl",
                "osl-2",
                "updater"
        )

        val platformModules = arrayOf(
                "commonMain",
                "jvmMain"
        )
    }

    @ExperimentalKorneaIO
    override suspend fun loadResource(name: String, from: KClass<*>): KorneaResult<DataSource<*>> {
        val classLoader = from.java.classLoader

        val file = File(name)
        if (file.exists())
            return KorneaResult.success(AsyncFileDataSource(file))

        var classLoaderResource = classLoader.getResource(name)
        if (classLoaderResource != null)
            return KorneaResult.success(JVMDataSource(classLoaderResource::openStream, null))

        for (module in spiralModules) {
            for (platform in platformModules) {
                val resourceFolderFile = File("$module/src/$platform/resources/$name")
                if (resourceFolderFile.exists())
                    return KorneaResult.success(AsyncFileDataSource(resourceFolderFile))
            }
        }

        classLoaderResource = SpiralModuleBase::class.java.classLoader.getResource(name)
        if (classLoaderResource != null)
            return KorneaResult.success(JVMDataSource(classLoaderResource::openStream, null))

        return korneaNotFound("Could not find a resource for name $name")
    }
}