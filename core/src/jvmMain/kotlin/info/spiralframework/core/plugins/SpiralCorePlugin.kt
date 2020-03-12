package info.spiralframework.core.plugins

import com.fasterxml.jackson.module.kotlin.readValue
import info.spiralframework.core.SpiralCoreContext
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.useInputFlow

class SpiralCorePlugin private constructor(context: SpiralCoreContext) : BaseSpiralPlugin(context, SpiralCorePlugin::class.java, "spiralframework_core_plugin.yaml") {
    companion object {
        suspend operator fun invoke(context: SpiralCoreContext): SpiralCorePlugin {
            val plugin = SpiralCorePlugin(context)
            plugin.init()
            return plugin
        }
    }

    class Provider : SpiralPluginRegistry.PojoProvider {
        override suspend fun readPojo(context: SpiralCoreContext): SpiralPluginDefinitionPojo =
                context.yamlMapper.readValue(requireNotNull(context.loadResource("spiralframework_core_plugin.yaml")?.useInputFlow { it.readBytes() }))
    }

    override fun SpiralCoreContext.load() {}
    override fun SpiralCoreContext.unload() {}
}