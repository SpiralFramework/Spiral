package info.spiralframework.base.common.io

//@ExperimentalUnsignedTypes
//TODO: Integrate into SpiralResourceLoader
//suspend fun loadResource(name: String, from: KClass<*>): DataSource<*>? {
//    val classLoader = from.java.classLoader
//
//    val file = File(name)
//    if (file.exists())
//        return FileDataSource(file)
//    val classLoaderResource = classLoader.getResource(name)
//    if (classLoaderResource != null)
//        return JVMDataSource(classLoaderResource::openStream)
//    for (module in spiralModules) {
//        for (platform in platformModules) {
//            val resourceFolderFile = File("$module/src/$platform/resources/$name")
//            if (resourceFolderFile.exists())
//                return FileDataSource(resourceFolderFile)
//        }
//    }
//
//    return null
//}