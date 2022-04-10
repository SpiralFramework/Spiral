package info.spiralframework.core.plugins

import dev.brella.kornea.errors.common.getOrThrow
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useAndMapInputFlow
import info.spiralframework.core.SpiralCoreContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

public class SpiralCorePlugin private constructor(context: SpiralCoreContext) :
    BaseSpiralPlugin(context, SpiralCorePlugin::class.java, "spiralframework_core_plugin.json") {
    public companion object {
        public suspend operator fun invoke(context: SpiralCoreContext): SpiralCorePlugin {
            val plugin = SpiralCorePlugin(context)
            plugin.init()
            return plugin
        }
    }

    public class Provider : SpiralPluginRegistry.PojoProvider {
        override suspend fun readPojo(context: SpiralCoreContext): SpiralPluginDefinitionPojo =
            context.loadResource("spiralframework_core_plugin.yaml")
                .useAndMapInputFlow { flow -> flow.readBytes() }
                .map { data -> Json.decodeFromString<SpiralPluginDefinitionPojo>(data.decodeToString()) }
                .getOrThrow()
    }

    override fun SpiralCoreContext.load() {}
    override fun SpiralCoreContext.unload() {}
}