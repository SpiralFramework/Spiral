package info.spiralframework.core.formats

import info.spiralframework.core.FormatChance
import java.io.Closeable
import java.util.*
import kotlin.collections.ArrayList

sealed class FormatResult<T>: Closeable {
    companion object {
        operator fun <T> invoke(obj: T?, isFormat: Boolean, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail(isFormat, chance)
            if (!isFormat)
                return Fail(chance)
            return Success(obj, chance)
        }

        operator fun <T> invoke(obj: T?, chance: Double): FormatResult<T> {
            if (obj == null)
                return Fail(chance)
            return Success(obj, true, chance)
        }
    }

    class Success<T>(override val obj: T, override val chance: FormatChance): FormatResult<T>() {
        constructor(obj: T, isFormat: Boolean, chance: Double): this(obj, FormatChance(isFormat, chance))
        constructor(obj: T, chance: Double): this(obj, true, chance)

        override val didSucceed: Boolean = true
    }
    class Fail<T>(override val chance: FormatChance): FormatResult<T>() {
        constructor(isFormat: Boolean, chance: Double): this(FormatChance(isFormat, chance))
        constructor(chance: Double): this(FormatChance(false, chance))

        override val obj: T
            get() = throw NoSuchElementException("No value present")
        override val didSucceed: Boolean = false
    }

    abstract val obj: T
    abstract val chance: FormatChance

    abstract val didSucceed: Boolean

    val safeObj: T?
        get() = if (didSucceed) obj else null

    val release: MutableCollection<Closeable> = ArrayList()

    override fun close() { release.forEach(Closeable::close) }
}