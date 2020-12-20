package info.spiralframework.base.common.properties

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.toolkit.common.KorneaTypeChecker
import info.spiralframework.base.common.SpiralContext

interface ISpiralProperty<T> {
    companion object {
        val EMPTY_ARRAY = emptyArray<String>()
    }

    interface PropertyKey<out T>: KorneaTypeChecker<T> {
        val name: String
        val aliases: Array<String> get() = EMPTY_ARRAY

        val isPersistent: Boolean get() = true

        override fun hashCode(): Int
        override fun equals(other: Any?): Boolean
    }

    interface Object<T>: ISpiralProperty<T>, PropertyKey<T> {
        override val key get() = this
        override val aliases: Array<String> get() = EMPTY_ARRAY
    }

    interface Empty<T>: ISpiralProperty<T> {
        override suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any?): KorneaResult<SpiralProperties> = KorneaResult.empty()
    }

    val name: String
    val aliases: Array<String> get() = EMPTY_ARRAY
    val key: PropertyKey<T>

    suspend fun isPresent(context: SpiralContext, writeContext: SpiralProperties?, data: Any?): Boolean = writeContext?.contains(key) == true
    suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any?): KorneaResult<SpiralProperties>

    object FileName: Object<String>, Empty<String>, KorneaTypeChecker<String> by KorneaTypeChecker.ClassBased() {
        override val name: String = "FileName"

        override val isPersistent: Boolean = false

        override fun hashCode(): Int = defaultHashCode()
        override fun equals(other: Any?): Boolean = defaultEquals(other)
    }
}

inline fun <T> ISpiralProperty.PropertyKey<T>.defaultEquals(other: Any?): Boolean = when (other) {
    is CharSequence -> other == name
    is ISpiralProperty<*> -> other.name == name
    is ISpiralProperty.PropertyKey<*> -> other.name == name

    else -> false
}

inline fun <T> ISpiralProperty.PropertyKey<T>.defaultHashCode(): Int = name.hashCode()