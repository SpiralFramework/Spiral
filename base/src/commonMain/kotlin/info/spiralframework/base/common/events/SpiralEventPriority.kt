package info.spiralframework.base.common.events

/**
 * The priority of an event
 */
inline class SpiralEventPriority(val priority: Int): Comparable<SpiralEventPriority> {
    companion object {
        val HIGHEST   = SpiralEventPriority(0x80000)
        val HIGH      = SpiralEventPriority(0x60000)
        val NORMAL    = SpiralEventPriority(0x40000)
        val LOW       = SpiralEventPriority(0x20000)
        val LOWEST    = SpiralEventPriority(0x00000)
    }

    override fun compareTo(other: SpiralEventPriority): Int = other.priority - this.priority

    operator fun plus(other: Int): SpiralEventPriority = SpiralEventPriority(priority + other)
    operator fun minus(other: Int): SpiralEventPriority = SpiralEventPriority(priority - other)
}

operator fun Int.plus(other: SpiralEventPriority): SpiralEventPriority = SpiralEventPriority(this + other.priority)
operator fun Int.minus(other: SpiralEventPriority): SpiralEventPriority = SpiralEventPriority(this - other.priority)