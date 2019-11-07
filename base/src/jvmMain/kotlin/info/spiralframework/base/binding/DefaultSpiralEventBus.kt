package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.events.*
import kotlin.reflect.KClass

actual class DefaultSpiralEventBus actual constructor() : SpiralEventBus {
    private val listeners: MutableSet<SpiralEventListener<*>> = HashSet()

    override fun <T : SpiralEvent> register(klass: KClass<T>, name: String, priority: SpiralEventPriority, block: suspend SpiralContext.(event: T) -> Unit) = register(BlockSpiralEventListener(klass, name, priority, block))
    override fun <T : SpiralEvent> register(listener: SpiralEventListener<T>) { listeners.add(listener) }

    override fun <T : SpiralEvent> deregister(klass: KClass<T>, name: String, priority: SpiralEventPriority) {
        listeners.removeAll { listener -> listener is BlockSpiralEventListener && listener.eventClass == klass && listener.name == name && listener.eventPriority == priority }
    }

    override fun <T : SpiralEvent> deregister(listener: SpiralEventListener<T>) {
        listeners.remove(listener)
    }

    override suspend fun <T : SpiralEvent> SpiralContext.post(event: T): T {
        val listeners = listeners.filter { listener ->
            listener.eventClass.isInstance(event)
        }.sortedBy(SpiralEventListener<*>::eventPriority)

        listeners.forEach { listener ->
            @Suppress("UNCHECKED_CAST")
            (listener as SpiralEventListener<T>).handle(this, event)
        }

        return event
    }

}