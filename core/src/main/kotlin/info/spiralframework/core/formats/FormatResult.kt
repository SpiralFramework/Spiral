package info.spiralframework.core.formats

import java.io.Closeable
import java.util.*
import kotlin.collections.ArrayList

sealed class FormatResult<T>: Closeable {
    companion object {
        operator fun <T> invoke(format: SpiralFormat? = null, obj: T?, isFormat: Boolean, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail<T>(chance).withFormat(format)
            if (!isFormat)
                return Fail<T>(chance).withFormat(format)
            return Success(obj, chance).withFormat(format)
        }

        operator fun <T> invoke(format: SpiralFormat? = null, obj: T?, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail<T>(chance).withFormat(format)
            return Success(obj, chance).withFormat(format)
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
        }
    }

    class Success<T>(override val obj: T, override val chance: Double): FormatResult<T>() {
        override val didSucceed: Boolean = true

        override fun <R> map(transform: (T) -> R): FormatResult<R> = Success(transform(obj), chance).withFormat(format)
        override fun filter(predicate: (T) -> Boolean): FormatResult<T> {
            if (predicate(obj))
                return this
            else
                return Fail<T>(1.0).withFormat(format)
        }
        override fun weight(predicate: (T) -> Double): FormatResult<T> = Success(obj, predicate(obj)).withFormat(format)
    }
    class Fail<T>(override val chance: Double, val reason: Throwable? = null): FormatResult<T>() {
        override val obj: T
            get() = throw NoSuchElementException("No value present")
        override val didSucceed: Boolean = false

        override fun <R> map(transform: (T) -> R): FormatResult<R> = Fail<R>(chance).withFormat(format)
        override fun filter(predicate: (T) -> Boolean): FormatResult<T> = this
        override fun weight(predicate: (T) -> Double): FormatResult<T> = this
    }

    abstract val obj: T
    abstract val chance: Double

    abstract val didSucceed: Boolean

    var format: SpiralFormat = NO_FORMAT_DEFINED
    var safeFormat: SpiralFormat?
        get() = if (format === NO_FORMAT_DEFINED) null else format
        set(value) {
            if (value == null)
                format = NO_FORMAT_DEFINED
            else
                format = value
        }
    val safeObj: T?
        get() = if (didSucceed) obj else null

    val release: MutableCollection<Closeable> = ArrayList()

    override fun close() { release.forEach(Closeable::close) }

    abstract fun <R> map(transform: (T) -> R): FormatResult<R>
    abstract fun filter(predicate: (T) -> Boolean): FormatResult<T>
    abstract fun weight(predicate: (T) -> Double): FormatResult<T>

    override fun toString(): String = "FormatResult(didSucceed=$didSucceed, obj=$safeObj, chance=$chance)"
}