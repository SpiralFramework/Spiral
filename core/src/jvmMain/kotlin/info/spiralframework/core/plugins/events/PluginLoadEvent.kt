package info.spiralframework.core.plugins.events

import info.spiralframework.core.plugins.LoadPluginResult
import info.spiralframework.core.plugins.PluginEntry

data class BeginLoadingPluginEvent(val plugin: PluginEntry, override var isCanceled: Boolean = false): CancellableSpiralEvent
data class SuccessfulPluginLoadEvent(val plugin: PluginEntry, val resultCode: LoadPluginResult): SpiralEvent
data class FailedPluginLoadEvent(val plugin: PluginEntry, val result: LoadPluginResult): SpiralEvent