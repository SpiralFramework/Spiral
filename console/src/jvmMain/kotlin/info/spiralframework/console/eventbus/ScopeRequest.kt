package info.spiralframework.console.eventbus

import info.spiralframework.console.data.SpiralScope
import info.spiralframework.core.plugins.events.CancellableSpiralEvent
import info.spiralframework.core.plugins.events.SpiralEvent

data class ScopeRequest(override var isCanceled: Boolean = false): CancellableSpiralEvent
data class ScopeResponse(val scope: SpiralScope): SpiralEvent