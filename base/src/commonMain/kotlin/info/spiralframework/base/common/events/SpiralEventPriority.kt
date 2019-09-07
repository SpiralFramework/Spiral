package info.spiralframework.base.common.events

/**
 * The priority of an event
 */
inline class SpiralEventPriority(val priority: Int): Comparable<SpiralEventPriority> {
    companion object {
        const val HIGHEST   = 0x80000
        const val HIGH      = 0x60000
        const val NORMAL    = 0x40000
        const val LOW       = 0x20000
        const val LOWEST    = 0x00000
    }

    override fun compareTo(other: SpiralEventPriority): Int = other.priority - this.priority

    operator fun plus(other: Int): SpiralEventPriority = SpiralEventPriority(priority + other)
    operator fun minus(other: Int): SpiralEventPriority = SpiralEventPriority(priority - other)
}

operator fun Int.plus(other: SpiralEventPriority): SpiralEventPriority = SpiralEventPriority(this + other.priority)
operator fun Int.minus(other: SpiralEventPriority): SpiralEventPriority = SpiralEventPriority(this - other.priority)