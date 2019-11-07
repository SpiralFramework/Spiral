package info.spiralframework.console.eventbus

import info.spiralframework.base.common.events.CancellableSpiralEvent
import info.spiralframework.base.common.events.SpiralEvent
import info.spiralframework.console.data.SpiralScope

data class ScopeRequest(override var cancelled: Boolean = false): CancellableSpiralEvent
data class ScopeResponse(val scope: SpiralScope): SpiralEvent