package info.spiralframework.base.common.properties

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.toolkit.common.KorneaTypeChecker
import info.spiralframework.base.common.SpiralContext

interface ISpiralProperty<T> {
    interface PropertyKey<T>: KorneaTypeChecker<T> {
        val name: String
    }

    interface Object<T>: ISpiralProperty<T>, PropertyKey<T> {
        override val key get() = this
    }

    interface Empty<T>: ISpiralProperty<T> {
        override suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any): KorneaResult<SpiralProperties> = KorneaResult.empty()
    }

    val name: String
    val key: PropertyKey<T>

    suspend fun isPresent(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean = writeContext?.contains(key) == true
    suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any): KorneaResult<SpiralProperties>

    object FileName: Object<String>, Empty<String>, KorneaTypeChecker<String> by KorneaTypeChecker.ClassBased() {
        override val name: String = "FileName"
    }
}