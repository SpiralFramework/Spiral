package info.spiralframework.core.plugins

import com.fasterxml.jackson.module.kotlin.readValue
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.core.SpiralCoreContext
import org.abimon.kornea.io.common.BinaryDataSource
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.useInputFlow
import org.abimon.kornea.io.jvm.files.FileDataSource
import java.io.File

abstract class BaseSpiralPlugin protected constructor(val context: SpiralCoreContext, val callingClass: Class<*>, val resourceName: String, val yaml: Boolean = true) : ISpiralPlugin {
    lateinit var pojo: SpiralPluginDefinitionPojo

    override val name: String by lazy { pojo.name }
    override val uid: String by lazy { pojo.uid }
    override val version: SemanticVersion by lazy { pojo.semanticVersion }
    private val jarFile = callingClass::class.java.protectionDomain.codeSource?.location?.path?.let(::File)?.takeIf(File::isFile)
    override val dataSource: DataSource<*> = jarFile?.let { f -> FileDataSource(f) } ?: BinaryDataSource(byteArrayOf())

    protected suspend fun init() {
        pojo = (if (yaml) context.yamlMapper else context.jsonMapper).readValue(requireNotNull(context.loadResource(resourceName, callingClass.kotlin)?.useInputFlow { it.readBytes() }))
    }
}