package info.spiralframework.base.common.io

import info.spiralframework.base.jvm.io.JVMDataSource
import info.spiralframework.base.jvm.io.files.FileDataSource
import info.spiralframework.base.jvm.platformModules
import info.spiralframework.base.jvm.spiralModules
import java.io.File
import kotlin.reflect.KClass

@ExperimentalUnsignedTypes
actual suspend fun loadResource(name: String, from: KClass<*>): DataSource<*>? {
    val classLoader = from.java.classLoader

    val file = File(name)
    if (file.exists())
        return FileDataSource(file)
    val classLoaderResource = classLoader.getResource(name)
    if (classLoaderResource != null)
        return JVMDataSource(classLoaderResource::openStream)
    for (module in spiralModules) {
        for (platform in platformModules) {
            val resourceFolderFile = File("$module/src/$platform/resources/$name")
            if (resourceFolderFile.exists())
                return FileDataSource(resourceFolderFile)
        }
    }

    return null
}