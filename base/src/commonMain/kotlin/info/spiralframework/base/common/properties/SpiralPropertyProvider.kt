package info.spiralframework.base.common.properties

import dev.brella.kornea.errors.common.getOrDefault
import info.spiralframework.base.common.SpiralContext

public interface SpiralPropertyProvider {
    public val availableProperties: List<ISpiralProperty<*>>

    public suspend fun SpiralContext.populate(writeContext: SpiralProperties?, data: Any?, keys: List<ISpiralProperty.PropertyKey<*>>): SpiralProperties? =
        keys.fold(writeContext) { formatContext, key ->
            val property = availableProperties.firstOrNull { it.key === key } ?: return@fold formatContext
            if (formatContext[property] != null) return@fold formatContext

            property.fillIn(this, formatContext, data).getOrDefault(formatContext)
        }

    public interface Mutable: SpiralPropertyProvider {
        override val availableProperties: MutableList<ISpiralProperty<*>>
    }
}

public suspend inline fun <C> C.populate(writeContext: SpiralProperties?, data: Any?, keys: List<ISpiralProperty.PropertyKey<*>>): SpiralProperties? where C: SpiralPropertyProvider, C: SpiralContext =
    populate(writeContext, data, keys)