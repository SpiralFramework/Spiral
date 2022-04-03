@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.properties

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.toolkit.common.KorneaTypeChecker
import info.spiralframework.base.common.SpiralContext

public interface ISpiralProperty<T> {
    public companion object {
        public val EMPTY_ARRAY: Array<String> = emptyArray()
    }

    public interface PropertyKey<out T>: KorneaTypeChecker<T> {
        public val name: String
        public val aliases: Array<String> get() = EMPTY_ARRAY

        public val isPersistent: Boolean get() = true

        override fun hashCode(): Int
        override fun equals(other: Any?): Boolean
    }

    public interface Object<T>: ISpiralProperty<T>, PropertyKey<T> {
        override val key: Object<T> get() = this
        override val aliases: Array<String> get() = EMPTY_ARRAY
    }

    public interface Empty<T>: ISpiralProperty<T> {
        override suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any?): KorneaResult<SpiralProperties> = KorneaResult.empty()
    }

    public val name: String
    public val aliases: Array<String> get() = EMPTY_ARRAY
    public val key: PropertyKey<T>

    public suspend fun isPresent(context: SpiralContext, writeContext: SpiralProperties?, data: Any?): Boolean = writeContext?.contains(key) == true
    public suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any?): KorneaResult<SpiralProperties>

    public object FileName: Object<String>, Empty<String>, KorneaTypeChecker<String> by KorneaTypeChecker.ClassBased() {
        override val name: String = "FileName"

        override val isPersistent: Boolean = false

        override fun hashCode(): Int = defaultHashCode()
        override fun equals(other: Any?): Boolean = defaultEquals(other)
    }
}

public inline fun <T> ISpiralProperty.PropertyKey<T>.defaultEquals(other: Any?): Boolean = when (other) {
    is CharSequence -> other == name
    is ISpiralProperty<*> -> other.name == name
    is ISpiralProperty.PropertyKey<*> -> other.name == name

    else -> false
}

public inline fun <T> ISpiralProperty.PropertyKey<T>.defaultHashCode(): Int = name.hashCode()