package info.spiralframework.base.common.events

public interface SpiralEvent
public interface CancellableSpiralEvent: SpiralEvent {
    public var cancelled: Boolean
}