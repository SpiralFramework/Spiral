package info.spiralframework.console.jvm.eventbus

import info.spiralframework.base.common.events.CancellableSpiralEvent
import info.spiralframework.base.common.events.SpiralEvent
import info.spiralframework.console.jvm.data.SpiralScope

data class ScopeRequest(override var cancelled: Boolean = false): CancellableSpiralEvent
data class ScopeResponse(val scope: SpiralScope): SpiralEvent