package info.spiralframework.core.plugins.events

import info.spiralframework.core.plugins.PluginEntry

class BeginPluginDiscoveryEvent: CancellableSpiralEvent {
    override var isCanceled: Boolean = false
}

class DiscoveredPluginEvent(val plugin: PluginEntry): CancellableSpiralEvent {
    override var isCanceled: Boolean = false
}

class EndPluginDiscoveryEvent: SpiralEvent