package info.spiralframework.base.common.events

import info.spiralframework.base.common.SpiralContext
import kotlin.reflect.KClass

interface SpiralEventBus {
    object NoOp: SpiralEventBus {
        override fun <T : SpiralEvent> register(klass: KClass<T>, name: String, priority: SpiralEventPriority, block: suspend SpiralContext.(event: T) -> Unit) {}
        override fun <T : SpiralEvent> register(listener: SpiralEventListener<T>) {}
        override fun <T : SpiralEvent> deregister(klass: KClass<T>, name: String, priority: SpiralEventPriority) {}
        override fun <T : SpiralEvent> deregister(listener: SpiralEventListener<T>) {}
        override suspend fun <T : SpiralEvent> SpiralContext.post(event: T): T = event
    }

    fun <T: SpiralEvent> register(klass: KClass<T>, name: String, priority: SpiralEventPriority, block: suspend SpiralContext.(event: T) -> Unit)
    fun <T: SpiralEvent> register(listener: SpiralEventListener<T>)
    fun <T: SpiralEvent> deregister(klass: KClass<T>, name: String, priority: SpiralEventPriority)
    fun <T: SpiralEvent> deregister(listener: SpiralEventListener<T>)

    suspend fun <T: SpiralEvent> SpiralContext.post(event: T): T
}

suspend fun <T: SpiralEvent> SpiralEventBus.post(context: SpiralContext, event: T): T = context.post(event)

inline fun <reified T: SpiralEvent> SpiralEventBus.register(name: String, priority: SpiralEventPriority, noinline block: suspend SpiralContext.(event: T) -> Unit) = register(T::class, name, priority, block)
inline fun <reified T: SpiralEvent> SpiralEventBus.deregister(name: String, priority: SpiralEventPriority) = deregister(T::class, name, priority)