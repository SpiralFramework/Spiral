package info.spiralframework.base.common.properties

import dev.brella.kornea.errors.common.getOrElse
import info.spiralframework.base.common.SpiralContext

interface SpiralPropertyProvider {
    val availableProperties: List<ISpiralProperty<*>>

    suspend fun SpiralContext.populate(writeContext: SpiralProperties?, data: Any, keys: List<ISpiralProperty.PropertyKey<*>>): SpiralProperties? =
        keys.fold(writeContext) { formatContext, key ->
            val property = availableProperties.firstOrNull { it.key == key } ?: return@fold formatContext
            if (property.isPresent(this, formatContext, data)) return@fold formatContext

            property.fillIn(this, formatContext, data).getOrElse(formatContext)
        }

    interface Mutable: SpiralPropertyProvider {
        override val availableProperties: MutableList<ISpiralProperty<*>>
    }
}

suspend inline fun <C> C.populate(writeContext: SpiralProperties?, data: Any, keys: List<ISpiralProperty.PropertyKey<*>>) where C: SpiralPropertyProvider, C: SpiralContext =
    populate(writeContext, data, keys)