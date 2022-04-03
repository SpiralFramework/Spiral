package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.events.SpiralEvent
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.events.SpiralEventListener
import info.spiralframework.base.common.events.SpiralEventPriority
import kotlin.reflect.KClass

public actual class DefaultSpiralEventBus actual constructor() : SpiralEventBus {
    override fun <T : SpiralEvent> register(klass: KClass<T>, name: String, priority: SpiralEventPriority, block: suspend SpiralContext.(event: T) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : SpiralEvent> register(listener: SpiralEventListener<T>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : SpiralEvent> deregister(klass: KClass<T>, name: String, priority: SpiralEventPriority) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : SpiralEvent> deregister(listener: SpiralEventListener<T>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun <T : SpiralEvent> SpiralContext.post(event: T): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}