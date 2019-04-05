package info.spiralframework.core.plugins

import info.spiralframework.core.plugins.events.SpiralEvent
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

object EventBus {
    private val handlers: MutableList<EventHandler<SpiralEvent>> = ArrayList()

    inline fun <reified T: SpiralEvent> fire(event: T) = fire(event, T::class)
    fun <T: SpiralEvent> fire(event: T, eventKlass: KClass<T>) {
        val receivers = handlers.filter { handler -> handler.klass.isSuperclassOf(eventKlass) }.sortedBy(EventHandler<*>::priority)
        receivers.forEach { receiver -> receiver(event) }
    }

    inline fun <reified T: SpiralEvent> register(priority: EventPriority, noinline handle: (T) -> Unit) = register(T::class, priority, handle)
    @Suppress("UNCHECKED_CAST")
    fun <T: SpiralEvent> register(klass: KClass<T>, priority: EventPriority, handle: (T) -> Unit) {
        handlers.add(EventHandler(klass, priority, handle) as EventHandler<SpiralEvent>)
    }
}