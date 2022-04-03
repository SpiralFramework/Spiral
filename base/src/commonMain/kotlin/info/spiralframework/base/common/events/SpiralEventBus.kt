package info.spiralframework.base.common.events

import info.spiralframework.base.common.SpiralContext
import kotlin.reflect.KClass

public interface SpiralEventBus {
    public object NoOp: SpiralEventBus {
        override fun <T : SpiralEvent> register(klass: KClass<T>, name: String, priority: SpiralEventPriority, block: suspend SpiralContext.(event: T) -> Unit) {}
        override fun <T : SpiralEvent> register(listener: SpiralEventListener<T>) {}
        override fun <T : SpiralEvent> deregister(klass: KClass<T>, name: String, priority: SpiralEventPriority) {}
        override fun <T : SpiralEvent> deregister(listener: SpiralEventListener<T>) {}
        override suspend fun <T : SpiralEvent> SpiralContext.post(event: T): T = event
    }

    public fun <T: SpiralEvent> register(klass: KClass<T>, name: String, priority: SpiralEventPriority, block: suspend SpiralContext.(event: T) -> Unit)
    public fun <T: SpiralEvent> register(listener: SpiralEventListener<T>)
    public fun <T: SpiralEvent> deregister(klass: KClass<T>, name: String, priority: SpiralEventPriority)
    public fun <T: SpiralEvent> deregister(listener: SpiralEventListener<T>)

    public suspend fun <T: SpiralEvent> SpiralContext.post(event: T): T
}

public suspend fun <T: SpiralEvent> SpiralEventBus.post(context: SpiralContext, event: T): T = context.post(event)

public inline fun <reified T: SpiralEvent> SpiralEventBus.register(name: String, priority: SpiralEventPriority, noinline block: suspend SpiralContext.(event: T) -> Unit): Unit = register(T::class, name, priority, block)
public inline fun <reified T: SpiralEvent> SpiralEventBus.deregister(name: String, priority: SpiralEventPriority): Unit = deregister(T::class, name, priority)