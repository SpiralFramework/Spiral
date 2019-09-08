package info.spiralframework.base.common.events

interface SpiralEvent
interface CancellableSpiralEvent: SpiralEvent {
    var cancelled: Boolean
}