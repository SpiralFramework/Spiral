package info.spiralframework.core.plugins.events

import info.spiralframework.core.plugins.PluginEntry
import info.spiralframework.core.plugins.PluginRegistry

data class BeginLoadingPluginEvent(val plugin: PluginEntry, override var isCanceled: Boolean = false): CancellableSpiralEvent
data class SuccessfulPluginLoadEvent(val plugin: PluginEntry, val resultCode: PluginRegistry.LoadPluginResult): SpiralEvent
data class FailedPluginLoadEvent(val plugin: PluginEntry, val result: PluginRegistry.LoadPluginResult): SpiralEvent