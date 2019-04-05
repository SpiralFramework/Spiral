package info.spiralframework.core.plugins

import kotlin.reflect.KClass

data class EventHandler<T: Any>(val klass: KClass<T>, val priority: EventPriority, val handler: (T) -> Unit) {
    operator fun invoke(t: T) = handler(t)
}