package info.spiralframework.core.plugins

import info.spiralframework.core.SpiralCoreContext

//TODO: Replace with KorneaResult
public enum class LoadPluginResult(public val success: Boolean) {
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

public interface SpiralPluginRegistry {
    public interface PojoProvider {
        public suspend fun readPojo(context: SpiralCoreContext): SpiralPluginDefinitionPojo
    }

    public object NoOp: SpiralPluginRegistry {
        override fun SpiralCoreContext.loadedPlugins(): List<ISpiralPlugin> = emptyList()
        override suspend fun SpiralCoreContext.discover(): List<PluginEntry> = emptyList()
        override suspend fun SpiralCoreContext.loadPlugin(plugin: PluginEntry): LoadPluginResult = LoadPluginResult.NO_OP
        override suspend fun SpiralCoreContext.unloadPlugin(plugin: ISpiralPlugin) {}
        override suspend fun SpiralCoreContext.queryEnablePlugin(plugin: PluginEntry): Boolean = false
    }

    public fun SpiralCoreContext.loadedPlugins(): List<ISpiralPlugin>

    public suspend fun SpiralCoreContext.discover(): List<PluginEntry>
    public suspend fun SpiralCoreContext.loadPlugin(pluginEntry: PluginEntry): LoadPluginResult
    public suspend fun SpiralCoreContext.queryEnablePlugin(plugin: PluginEntry): Boolean
    public suspend fun SpiralCoreContext.unloadPlugin(plugin: ISpiralPlugin)
}

public fun SpiralPluginRegistry.loadedPlugins(context: SpiralCoreContext): List<ISpiralPlugin> = context.loadedPlugins()

public suspend fun SpiralPluginRegistry.discover(context: SpiralCoreContext): List<PluginEntry> = context.discover()
public suspend fun SpiralPluginRegistry.loadPlugin(context: SpiralCoreContext, plugin: PluginEntry): LoadPluginResult = context.loadPlugin(plugin)
public suspend fun SpiralPluginRegistry.unloadPlugin(context: SpiralCoreContext, plugin: ISpiralPlugin): Unit = context.unloadPlugin(plugin)
public suspend fun SpiralPluginRegistry.queryEnablePlugin(context: SpiralCoreContext, plugin: PluginEntry): Boolean = context.queryEnablePlugin(plugin)