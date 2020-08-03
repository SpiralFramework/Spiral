package info.spiralframework.core.formats

import info.spiralframework.base.common.concurrent.suspendForEach
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataCloseable
import java.io.Closeable
import java.util.*
import kotlin.collections.ArrayList

sealed class FormatResult<T>: DataCloseable {
    companion object {
        operator fun <T> invoke(format: SpiralFormat? = null, obj: T?, isFormat: Boolean, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail<T>(format, chance)
            if (!isFormat)
                return Fail<T>(format, chance)
            return Success(format, obj, chance)
        }

        operator fun <T> invoke(format: SpiralFormat? = null, obj: T?, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail<T>(format, chance)
            return Success(format, obj, chance)
        }

        operator fun <T> invoke(obj: T?, isFormat: Boolean, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail(chance)
            if (!isFormat)
                return Fail(chance)
            return Success(obj, chance)
        }

        operator fun <T> invoke(obj: T?, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail(chance)
            return Success(obj, chance)
        }

        val NO_FORMAT_DEFINED = object: SpiralFormat {
            override val name: String = "[NO FORMAT DEFINED]"
            override val extension: String? = null
        }
    }

    class Success<T>(override val obj: T, override val chance: Double): FormatResult<T>() {
        constructor(format: SpiralFormat?, obj: T, chance: Double): this(obj, chance) {
            this.nullableFormat = format
        }

        override val didSucceed: Boolean = true

        override fun <R> map(transform: (T) -> R): FormatResult<R> = Success(format, transform(obj), chance)
        override fun filter(predicate: (T) -> Boolean): FormatResult<T> {
            if (predicate(obj))
                return this
            else
                return Fail(format, 1.0)
        }
        override fun weight(predicate: (T) -> Double): FormatResult<T> = Success(format, obj, predicate(obj))
    }
    class Fail<T>(override val chance: Double, val reason: KorneaResult<*>? = null): FormatResult<T>() {
        constructor(format: SpiralFormat?, chance: Double, reason: KorneaResult<*>? = null): this(chance, reason) {
            this.nullableFormat = format
        }

        override val obj: T
            get() = throw NoSuchElementException("No value present")
        override val didSucceed: Boolean = false

        override fun <R> map(transform: (T) -> R): FormatResult<R> = Fail(format, chance)
        override fun filter(predicate: (T) -> Boolean): FormatResult<T> = this
        override fun weight(predicate: (T) -> Double): FormatResult<T> = this
    }

    abstract val obj: T
    abstract val chance: Double

    abstract val didSucceed: Boolean

    var format: SpiralFormat = NO_FORMAT_DEFINED
    var nullableFormat: SpiralFormat?
        get() = if (format === NO_FORMAT_DEFINED) null else format
        set(value) {
            if (value == null)
                format = NO_FORMAT_DEFINED
            else
                format = value
        }
    val safeObj: T?
        get() = if (didSucceed) obj else null

    val release: MutableCollection<DataCloseable> = ArrayList()

    override suspend fun close() { release.suspendForEach(DataCloseable::close) }

    abstract fun <R> map(transform: (T) -> R): FormatResult<R>
    abstract fun filter(predicate: (T) -> Boolean): FormatResult<T>
    abstract fun weight(predicate: (T) -> Double): FormatResult<T>

    override fun toString(): String = "FormatResult(didSucceed=$didSucceed, obj=$safeObj, chance=$chance)"
}