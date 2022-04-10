package info.spiralframework.core.plugins

import kotlin.reflect.KClass

public data class EventHandler<T: Any>(val klass: KClass<T>, val priority: EventPriority, val handler: (T) -> Unit) {
    public operator fun invoke(t: T): Unit = handler(t)
}