@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.core.common.formats

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.errors.common.*
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.properties.ISpiralProperty
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public typealias SpiralFormatResult<T, F> = KorneaResult<FormatSuccess<T, F>>
public typealias SpiralFormatReturnResult<T> = KorneaResult<FormatSuccess<T, T>>
public typealias SpiralFormatOptionalResult<T> = KorneaResult<FormatSuccess<Optional<T>, T>>
public typealias SpiralGenericFormatResult<T> = KorneaResult<FormatSuccess<T, *>>
public typealias GenericFormatSuccess<T> = FormatSuccess<T, *>

public data class FormatSuccess<out T, out F>(val value: T, val format: ReadableSpiralFormat<F>, val confidence: Double)

public interface SpiralFormatError : KorneaResult.Failure {
    public val errorMessage: String

    override fun asException(): Throwable =
        IllegalArgumentException(WrongFormat.errorMessage)

    public interface WrongFormat : SpiralFormatError {
        public companion object : WrongFormat {
            override val errorMessage: String
                get() = "Wrong format provided"
            override val cause: KorneaResult.Failure?
                get() = null

            override fun withCause(newCause: KorneaResult.Failure?): WrongFormat =
                Base(errorMessage, newCause)

            override fun <R> withPayload(newPayload: R): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, cause, newPayload)

            override fun <R> with(newPayload: R, newCause: KorneaResult.Failure?): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, newCause, newPayload)

            public fun of(errorMessage: String, cause: KorneaResult.Failure? = null): WrongFormat =
                Base(errorMessage, cause)

            public fun <T> of(errorMessage: String, payload: T): KorneaResult.WithPayload<T> =
                BaseWithPayload(errorMessage, null, payload)

            public fun <T> of(
                errorMessage: String,
                cause: KorneaResult.Failure? = null,
                payload: T,
            ): KorneaResult.WithPayload<T> =
                BaseWithPayload(errorMessage, cause, payload)
        }

        private data class Base(override val errorMessage: String, override val cause: KorneaResult.Failure?) :
            WrongFormat {

            override fun withCause(newCause: KorneaResult.Failure?): WrongFormat =
                Base(errorMessage, newCause)

            override fun <R> withPayload(newPayload: R): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, cause, newPayload)

            override fun <R> with(newPayload: R, newCause: KorneaResult.Failure?): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, newCause, newPayload)
        }

        private data class BaseWithPayload<out T>(
            override val errorMessage: String,
            override val cause: KorneaResult.Failure?,
            override val payload: T,
        ) : WrongFormat, KorneaResult.WithPayload<T> {
            override fun withCause(newCause: KorneaResult.Failure?): KorneaResult.WithPayload<T> =
                BaseWithPayload(errorMessage, newCause, payload)

            override fun <R> withPayload(newPayload: R): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, cause, newPayload)

            override fun <R> with(newPayload: R, newCause: KorneaResult.Failure?): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, newCause, newPayload)
        }
    }

    public interface MissingProperty<out T> : SpiralFormatError {
        public val property: ISpiralProperty.PropertyKey<T>

        public companion object {
            public fun <T> of(
                errorMessage: String,
                property: ISpiralProperty.PropertyKey<T>,
                cause: KorneaResult.Failure? = null,
            ): MissingProperty<T> =
                Base(errorMessage, cause, property)

            public fun <T, R> of(
                errorMessage: String,
                property: ISpiralProperty.PropertyKey<T>,
                payload: R,
            ): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, null, payload, property)

            public fun <T, R> of(
                errorMessage: String,
                property: ISpiralProperty.PropertyKey<T>,
                cause: KorneaResult.Failure? = null,
                payload: R,
            ): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, cause, payload, property)
        }

        private data class Base<out T>(
            override val errorMessage: String,
            override val cause: KorneaResult.Failure?,
            override val property: ISpiralProperty.PropertyKey<T>,
        ) : MissingProperty<T> {
            override fun withCause(newCause: KorneaResult.Failure?): KorneaResult.Failure =
                Base(errorMessage, newCause, property)

            override fun <R> withPayload(newPayload: R): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, cause, newPayload, property)

            override fun <R> with(newPayload: R, newCause: KorneaResult.Failure?): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, newCause, newPayload, property)
        }

        private data class BaseWithPayload<out T, out R>(
            override val errorMessage: String,
            override val cause: KorneaResult.Failure?,
            override val payload: R,
            override val property: ISpiralProperty.PropertyKey<T>,
        ) : MissingProperty<T>, KorneaResult.WithPayload<R> {
            override fun withCause(newCause: KorneaResult.Failure?): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, newCause, payload, property)

            override fun <R> withPayload(newPayload: R): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, cause, newPayload, property)

            override fun <R> with(newPayload: R, newCause: KorneaResult.Failure?): KorneaResult.WithPayload<R> =
                BaseWithPayload(errorMessage, newCause, newPayload, property)
        }
    }
//    public object WRONG_FORMAT : FormatWriteResponse()

//    public data class MISSING_PROPERTY(val property: ISpiralProperty.PropertyKey<*>) : FormatWriteResponse()
}

/** ----- Constructors ----- */

public inline fun <T> KorneaResult.Companion.spiralWrongFormat(): KorneaResult<T> =
    failure(SpiralFormatError.WrongFormat)

public inline fun <T> KorneaResult.Companion.spiralWrongFormat(errorMessage: String): KorneaResult<T> =
    failure(SpiralFormatError.WrongFormat.of(errorMessage))

public inline fun <T> KorneaResult.Companion.spiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
): KorneaResult<T> =
    failure(SpiralFormatError.MissingProperty.of(errorMessage, property))

/** ------- Spiral Locale Constructors ------- */

public inline fun <T> SpiralLocale.localisedSpiralWrongFormat(errorMessage: String): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat.of(localise(errorMessage)))

public inline fun <T> SpiralLocale.localisedSpiralWrongFormat(errorMessage: String, arg: Any): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat.of(localise(errorMessage, arg)))

public inline fun <T> SpiralLocale.localisedSpiralWrongFormat(
    errorMessage: String,
    arg1: Any,
    arg2: Any,
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat.of(localise(errorMessage, arg1, arg2)))

public inline fun <T> SpiralLocale.localisedSpiralWrongFormat(errorMessage: String, vararg args: Any): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat.of(localiseArray(errorMessage, args)))

public inline fun <T> SpiralLocale.localisedArraySpiralWrongFormat(
    errorMessage: String,
    args: Array<out Any>,
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat.of(localiseArray(errorMessage, args)))

public inline fun <T> SpiralLocale.localisedSpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty.of(localise(errorMessage), property))

public inline fun <T> SpiralLocale.localisedSpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
    arg: Any,
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty.of(localise(errorMessage, arg), property))

public inline fun <T> SpiralLocale.localisedSpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
    arg1: Any,
    arg2: Any,
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty.of(localise(errorMessage, arg1, arg2), property))

public inline fun <T> SpiralLocale.localisedSpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
    vararg args: Any,
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty.of(localiseArray(errorMessage, args), property))

public inline fun <T> SpiralLocale.localisedArraySpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
    args: Array<out Any>,
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty.of(localiseArray(errorMessage, args), property))

/** ----- Format Success Functions ----- */

public inline fun <T, F, R> FormatSuccess<T, F>.map(transform: (T) -> R): FormatSuccess<R, F> =
    FormatSuccess(transform(value), format, confidence)

public inline fun <T, R> SpiralGenericFormatResult<T>.mapGenericFormat(transform: (T) -> R): SpiralGenericFormatResult<R> =
    map { it.map(transform) }

public inline fun <T, F, R> SpiralFormatResult<T, F>.mapFormat(transform: (T) -> R): SpiralFormatResult<R, F> =
    map { it.map(transform) }

@Suppress("UNCHECKED_CAST")
public inline fun <reified R> Iterable<*>.filterIsFormatResult(): List<SpiralGenericFormatResult<R>> =
    filterIsInstance<KorneaResult<*>>()
        .filter { result -> result.isFailure || result.getOrThrow() is R }
            as List<SpiralGenericFormatResult<R>>


/** KorneaResult */
@OptIn(ExperimentalContracts::class)
public inline fun <T> SpiralGenericFormatResult<T>.doOnSuccessInner(block: (T) -> Unit): SpiralGenericFormatResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    getOrNull()?.value?.let(block)
    return this
}

public inline fun <T> KorneaResult<T>.doOnSpiralFormatError(block: (SpiralFormatError) -> Unit): KorneaResult<T> =
    doOnTypedFailure(block)

public inline fun <T> KorneaResult<T>.doOnSpiralWrongFormat(block: (SpiralFormatError.WrongFormat) -> Unit): KorneaResult<T> =
    doOnTypedFailure(block)

public inline fun <T> KorneaResult<T>.doOnSpiralMissingProperty(block: (SpiralFormatError.MissingProperty<*>) -> Unit): KorneaResult<T> =
    doOnTypedFailure(block)

public inline fun <T> SpiralGenericFormatResult<T>.filterInner(predicate: (T) -> Boolean): SpiralGenericFormatResult<T> =
    filter { success -> predicate(success.value) }

public inline fun <R, T : R> SpiralGenericFormatResult<T>.filterInnerOrMap(
    predicate: (T) -> Boolean,
    failedPredicate: (T) -> R,
): SpiralGenericFormatResult<R> =
    filterOrMap({ success -> predicate(success.value) }, { success -> success.map(failedPredicate) })

public inline fun <R, T : R> SpiralGenericFormatResult<T>.filterInnerOrFlatMap(
    predicate: (T) -> Boolean,
    failedPredicate: (T) -> SpiralGenericFormatResult<R>,
): SpiralGenericFormatResult<R> =
    filterOrFlatMap({ success -> predicate(success.value) }, { success -> failedPredicate(success.value) })

@Suppress("UNCHECKED_CAST")
public inline fun <reified R> SpiralGenericFormatResult<*>.filterInnerIsInstance(): SpiralGenericFormatResult<R> =
    flatMap { success ->
        if (success.value is R) KorneaResult.success(success as FormatSuccess<R, *>)
        else KorneaResult.typeCastEmpty()
    }

public inline fun <R, T : R> KorneaResult<T>.switchIfSpiralFormatError(block: (SpiralFormatError) -> KorneaResult<R>): KorneaResult<R> =
    switchIfTypedFailure(block)

public inline fun <R, T : R> KorneaResult<T>.switchIfSpiralWrongFormat(block: (SpiralFormatError.WrongFormat) -> KorneaResult<R>): KorneaResult<R> =
    switchIfTypedFailure(block)

public inline fun <R, T : R> KorneaResult<T>.switchIfSpiralMissingProperty(block: (SpiralFormatError.MissingProperty<*>) -> KorneaResult<R>): KorneaResult<R> =
    switchIfTypedFailure(block)