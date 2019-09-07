package info.spiralframework.core.plugins

import info.spiralframework.core.SpiralCoreContext

enum class LoadPluginResult(val success: Boolean) {
    SUCCESS(true),
    NO_OP(false),
    ALREADY_LOADED(false),
    LOADED_ON_CLASSPATH(false),
    PLUGIN_LOAD_CANCELLED(false),
    NO_PLUGIN_CLASS_CONSTRUCTOR(false),
    PLUGIN_CLASS_NOT_SPIRAL_PLUGIN(false),
    MODULE_NOT_SUPPORTED(false),
    REQUIRED_MODULE_NOT_LOADED(false);
}

interface SpiralPluginRegistry {
    interface PojoProvider {
        fun readPojo(context: SpiralCoreContext): SpiralPluginDefinitionPojo
    }

    object NoOp: SpiralPluginRegistry {
        override fun SpiralCoreContext.loadedPlugins(): List<ISpiralPlugin> = emptyList()
        override fun SpiralCoreContext.discover(): List<PluginEntry> = emptyList()
        override fun SpiralCoreContext.loadPlugin(plugin: PluginEntry): LoadPluginResult = LoadPluginResult.NO_OP
        override fun SpiralCoreContext.unloadPlugin(plugin: ISpiralPlugin) {}
        override fun SpiralCoreContext.queryEnablePlugin(plugin: PluginEntry): Boolean = false
    }

    fun SpiralCoreContext.loadedPlugins(): List<ISpiralPlugin>

    fun SpiralCoreContext.discover(): List<PluginEntry>
    fun SpiralCoreContext.loadPlugin(plugin: PluginEntry): LoadPluginResult
    fun SpiralCoreContext.queryEnablePlugin(plugin: PluginEntry): Boolean
    fun SpiralCoreContext.unloadPlugin(plugin: ISpiralPlugin)
}

fun SpiralPluginRegistry.loadedPlugins(context: SpiralCoreContext): List<ISpiralPlugin> = context.loadedPlugins()

fun SpiralPluginRegistry.discover(context: SpiralCoreContext): List<PluginEntry> = context.discover()
fun SpiralPluginRegistry.loadPlugin(context: SpiralCoreContext, plugin: PluginEntry) = context.loadPlugin(plugin)
fun SpiralPluginRegistry.unloadPlugin(context: SpiralCoreContext, plugin: ISpiralPlugin) = context.unloadPlugin(plugin)
fun SpiralPluginRegistry.queryEnablePlugin(context: SpiralCoreContext, plugin: PluginEntry): Boolean = context.queryEnablePlugin(plugin)