package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralModuleBase
import info.spiralframework.base.common.io.SpiralResourceLoader
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.jvm.JVMDataSource
import org.abimon.kornea.io.jvm.files.FileDataSource
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

    override suspend fun loadResource(name: String, from: KClass<*>): DataSource<*>? {
        val classLoader = from.java.classLoader

        val file = File(name)
        if (file.exists())
            return FileDataSource(file)
        var classLoaderResource = classLoader.getResource(name)
        if (classLoaderResource != null)
            return JVMDataSource(classLoaderResource::openStream)
        for (module in spiralModules) {
            for (platform in platformModules) {
                val resourceFolderFile = File("$module/src/$platform/resources/$name")
                if (resourceFolderFile.exists())
                    return FileDataSource(resourceFolderFile)
            }
        }

        classLoaderResource = SpiralModuleBase::class.java.classLoader.getResource(name)
        if (classLoaderResource != null)
            return JVMDataSource(classLoaderResource::openStream)

        return null
    }
}