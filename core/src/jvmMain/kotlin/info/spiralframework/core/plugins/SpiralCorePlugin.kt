package info.spiralframework.core.plugins

import com.fasterxml.jackson.module.kotlin.readValue
import info.spiralframework.core.SpiralCoreContext

class SpiralCorePlugin(context: SpiralCoreContext) : BaseSpiralPlugin(context, SpiralCorePlugin::class.java, "spiralframework_core_plugin.yaml") {
    class Provider : SpiralPluginRegistry.PojoProvider {
        override fun readPojo(context: SpiralCoreContext): SpiralPluginDefinitionPojo =
                context.yamlMapper.readValue(requireNotNull(SpiralCorePlugin::class.java.classLoader.getResourceAsStream("spiralframework_core_plugin.yaml")))
    }

    override fun load() {}
    override fun unload() {}
}