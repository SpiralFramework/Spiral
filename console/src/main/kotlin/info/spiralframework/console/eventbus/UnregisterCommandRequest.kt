package info.spiralframework.console.eventbus

import info.spiralframework.core.plugins.events.CancellableSpiralEvent

data class UnregisterCommandRequest(val command: ParboiledCommand): CancellableSpiralEvent {
    override var isCanceled: Boolean = false
}