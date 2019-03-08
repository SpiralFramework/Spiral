package info.spiralframework.core.formats

import java.io.Closeable
import java.util.*
import kotlin.collections.ArrayList

sealed class FormatResult<T>: Closeable {
    companion object {
        operator fun <T> invoke(format: SpiralFormat? = null, obj: T?, isFormat: Boolean, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail(format, chance)
            if (!isFormat)
                return Fail(format, chance)
            return Success(format, obj, chance)
        }

        operator fun <T> invoke(format: SpiralFormat? = null, obj: T?, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail(format, chance)
            return Success(format, obj, chance)
        }
    }

    class Success<T>(override val format: SpiralFormat?, override val obj: T, override val chance: Double): FormatResult<T>() {
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
    class Fail<T>(override val format: SpiralFormat?, override val chance: Double, val reason: Throwable? = null): FormatResult<T>() {
        override val obj: T
            get() = throw NoSuchElementException("No value present")
        override val didSucceed: Boolean = false

        override fun <R> map(transform: (T) -> R): FormatResult<R> = Fail(format, chance)
        override fun filter(predicate: (T) -> Boolean): FormatResult<T> = this
        override fun weight(predicate: (T) -> Double): FormatResult<T> = this
    }

    abstract val format: SpiralFormat?
    abstract val obj: T
    abstract val chance: Double

    abstract val didSucceed: Boolean

    val safeObj: T?
        get() = if (didSucceed) obj else null

    val release: MutableCollection<Closeable> = ArrayList()

    override fun close() { release.forEach(Closeable::close) }

    abstract fun <R> map(transform: (T) -> R): FormatResult<R>
    abstract fun filter(predicate: (T) -> Boolean): FormatResult<T>
    abstract fun weight(predicate: (T) -> Double): FormatResult<T>

    override fun toString(): String = "FormatResult(didSucceed=$didSucceed, obj=$safeObj, chance=$chance)"
}