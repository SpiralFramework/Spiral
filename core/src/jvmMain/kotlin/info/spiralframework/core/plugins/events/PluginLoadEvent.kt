package info.spiralframework.core.plugins.events

import info.spiralframework.base.common.events.CancellableSpiralEvent
import info.spiralframework.base.common.events.SpiralEvent
import info.spiralframework.core.plugins.LoadPluginResult
import info.spiralframework.core.plugins.PluginEntry

public data class BeginLoadingPluginEvent(val plugin: PluginEntry, override var cancelled: Boolean = false): CancellableSpiralEvent
public data class SuccessfulPluginLoadEvent(val plugin: PluginEntry, val resultCode: LoadPluginResult): SpiralEvent
public data class FailedPluginLoadEvent(val plugin: PluginEntry, val result: LoadPluginResult): SpiralEvent