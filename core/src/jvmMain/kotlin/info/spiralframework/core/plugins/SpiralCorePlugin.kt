package info.spiralframework.core.plugins

object SpiralCorePlugin: BaseSpiralPlugin(SpiralCorePlugin::class.java, "spiralframework_core_plugin.yaml") {
    class Provider: PluginRegistry.PojoProvider {
        override val pojo: SpiralPluginDefinitionPojo = SpiralCorePlugin.pojo
    }

    override fun load() {}
    override fun unload() {}
}