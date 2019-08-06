package info.spiralframework.core.plugins.events

interface SpiralEvent
interface CancellableSpiralEvent: SpiralEvent {
    var isCanceled: Boolean
}