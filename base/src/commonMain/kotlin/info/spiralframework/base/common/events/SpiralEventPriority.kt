package info.spiralframework.base.common.events

import kotlin.jvm.JvmInline

/**
 * The priority of an event
 */
@JvmInline
public value class SpiralEventPriority(public val priority: Int) : Comparable<SpiralEventPriority> {
    public companion object {
        public val HIGHEST: SpiralEventPriority     = SpiralEventPriority(0x80000)
        public val HIGH: SpiralEventPriority        = SpiralEventPriority(0x60000)
        public val NORMAL: SpiralEventPriority      = SpiralEventPriority(0x40000)
        public val LOW: SpiralEventPriority         = SpiralEventPriority(0x20000)
        public val LOWEST: SpiralEventPriority      = SpiralEventPriority(0x00000)
    }

    override operator fun compareTo(other: SpiralEventPriority): Int = other.priority - this.priority
    public operator fun compareTo(other: Int): Int = other - this.priority

    public operator fun plus(other: Int): SpiralEventPriority = SpiralEventPriority(priority + other)
    public operator fun minus(other: Int): SpiralEventPriority = SpiralEventPriority(priority - other)
}

public operator fun Int.compareTo(other: SpiralEventPriority): Int = other.priority - this
public operator fun Int.plus(other: SpiralEventPriority): SpiralEventPriority = SpiralEventPriority(this + other.priority)
public operator fun Int.minus(other: SpiralEventPriority): SpiralEventPriority = SpiralEventPriority(this - other.priority)