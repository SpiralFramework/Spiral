package info.spiralframework.console.eventbus

import info.spiralframework.base.common.events.CancellableSpiralEvent

data class RegisterCommandRequest(val command: ParboiledCommand, override var cancelled: Boolean = false): CancellableSpiralEvent