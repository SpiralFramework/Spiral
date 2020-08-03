package info.spiralframework.core.plugins

import com.fasterxml.jackson.module.kotlin.readValue
import info.spiralframework.core.SpiralCoreContext
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useAndMapInputFlow

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
            context.loadResource("spiralframework_core_plugin.yaml")
                .useAndMapInputFlow { flow -> flow.readBytes() }
                .map { data -> context.yamlMapper.readValue<SpiralPluginDefinitionPojo>(data) }
                .get()
    }

    override fun SpiralCoreContext.load() {}
    override fun SpiralCoreContext.unload() {}
}