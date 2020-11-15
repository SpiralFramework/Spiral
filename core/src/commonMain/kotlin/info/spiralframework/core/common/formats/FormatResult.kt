package info.spiralframework.core.common.formats

import dev.brella.kornea.errors.common.KorneaResult

public interface FormatResult<out T, out F> : KorneaResult.Success<T> {
    public companion object {
        public fun <T, F> of(value: T, format: ReadableSpiralFormat<F>, confidence: Double): FormatResult<T, F> =
            Base(value, format, confidence)
    }

    private data class Base<out T, out F>(private val value: T, private val format: ReadableSpiralFormat<F>, private val confidence: Double) : FormatResult<T, F> {
        override fun get(): T = value
        override fun format(): ReadableSpiralFormat<F> = format
        override fun confidence(): Double = confidence

        override fun <R> mapValue(newValue: R): KorneaResult.Success<R> = Base(newValue, format, confidence)
    }

    override fun get(): T
    fun format(): ReadableSpiralFormat<F>
    fun confidence(): Double
}

public inline fun <T> FormatResult<T, *>.value(): T = get()

public inline fun <T, F> KorneaResult.Companion.formatResult(value: T, format: ReadableSpiralFormat<F>, confidence: Double): KorneaResult<T> =
    FormatResult.of(value, format, confidence)