package info.spiralframework.core.plugins

import com.fasterxml.jackson.module.kotlin.readValue
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.core.SpiralCoreContext
import org.abimon.kornea.io.common.BinaryDataSource
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.jvm.files.FileDataSource
import java.io.File
import kotlin.reflect.KClass

abstract class BaseSpiralPlugin(context: SpiralCoreContext, callingClass: Class<*>, resourceName: String, yaml: Boolean = true) : ISpiralPlugin {
    constructor(context: SpiralCoreContext, callingKlass: KClass<*>, resourceName: String, yaml: Boolean = true) : this(context, callingKlass.java, resourceName, yaml)

    val pojo: SpiralPluginDefinitionPojo = (if (yaml) context.yamlMapper else context.jsonMapper).readValue(requireNotNull(callingClass.classLoader.getResourceAsStream(resourceName)))

    override val name: String = pojo.name
    override val uid: String = pojo.uid
    override val version: SemanticVersion = pojo.semanticVersion
    private val jarFile = callingClass::class.java.protectionDomain.codeSource?.location?.path?.let(::File)?.takeIf(File::isFile)
    override val dataSource: DataSource<*> = jarFile?.let { f -> FileDataSource(f) } ?: BinaryDataSource(byteArrayOf())
}