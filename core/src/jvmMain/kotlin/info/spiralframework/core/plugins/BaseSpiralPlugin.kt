package info.spiralframework.core.plugins

import com.fasterxml.jackson.module.kotlin.readValue
import info.spiralframework.base.util.SemVer
import info.spiralframework.core.SpiralSerialisation
import info.spiralframework.formats.utils.DataSource
import java.io.File
import kotlin.reflect.KClass

abstract class BaseSpiralPlugin(callingClass: Class<*>, resourceName: String, yaml: Boolean = true): ISpiralPlugin {
    constructor(callingKlass: KClass<*>, resourceName: String, yaml: Boolean = true): this(callingKlass.java, resourceName, yaml)

    val pojo: SpiralPluginDefinitionPojo = (if (yaml) SpiralSerialisation.YAML_MAPPER else SpiralSerialisation.JSON_MAPPER).readValue(callingClass.classLoader.getResourceAsStream(resourceName))

    override val name: String = pojo.name
    override val uid: String = pojo.uid
    override val version: SemVer = pojo.semanticVersion
    private val jarFile = callingClass::class.java.protectionDomain.codeSource?.location?.path?.let(::File)?.takeIf(File::isFile)
    override val dataSource: DataSource = jarFile?.let { jar -> jar::inputStream } ?: ByteArray(0)::inputStream
}