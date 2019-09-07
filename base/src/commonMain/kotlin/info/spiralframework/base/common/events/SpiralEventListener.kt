package info.spiralframework.base.common.events

import info.spiralframework.base.common.SpiralContext
import kotlin.reflect.KClass

interface SpiralEventListener<T : SpiralEvent> {
    val eventClass: KClass<T>
    val eventPriority: SpiralEventPriority

    suspend fun SpiralContext.handle(event: T)
}

data class BlockSpiralEventListener<T : SpiralEvent>(override val eventClass: KClass<T>, val name: String, override val eventPriority: SpiralEventPriority, val block: suspend SpiralContext.(T) -> Unit) : SpiralEventListener<T> {
    override suspend fun SpiralContext.handle(event: T) = block(event)
}

suspend fun <T : SpiralEvent> SpiralEventListener<T>.handle(context: SpiralContext, event: T) = context.handle(event)