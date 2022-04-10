package info.spiralframework.core.plugins.events

import info.spiralframework.base.common.events.CancellableSpiralEvent
import info.spiralframework.base.common.events.SpiralEvent
import info.spiralframework.core.plugins.PluginEntry

public class BeginPluginDiscoveryEvent : CancellableSpiralEvent {
    override var cancelled: Boolean = false

    override fun toString(): String = "BeginPluginDiscoveryEvent()"
}

public data class DiscoveredPluginEvent(val plugin: PluginEntry, override var cancelled: Boolean = false): CancellableSpiralEvent

public class EndPluginDiscoveryEvent : SpiralEvent {
    override fun toString(): String = "EndPluginDiscoveryEvent()"
}