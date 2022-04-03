package info.spiralframework.base.common.events

import info.spiralframework.base.common.SpiralContext
import kotlin.reflect.KClass

public interface SpiralEventListener<T : SpiralEvent> {
    public val eventClass: KClass<T>
    public val eventPriority: SpiralEventPriority

    public suspend fun SpiralContext.handle(event: T)
}

public data class BlockSpiralEventListener<T : SpiralEvent>(override val eventClass: KClass<T>, val name: String, override val eventPriority: SpiralEventPriority, val block: suspend SpiralContext.(T) -> Unit) : SpiralEventListener<T> {
    override suspend fun SpiralContext.handle(event: T): Unit = block(event)
}

public suspend fun <T : SpiralEvent> SpiralEventListener<T>.handle(context: SpiralContext, event: T): Unit = context.handle(event)