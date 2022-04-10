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
    public open class WrongFormat(public val errorMessage: String) : SpiralFormatError {
        public companion object : WrongFormat("Wrong format provided")

        override fun asException(): Throwable =
            IllegalArgumentException(errorMessage)
    }

    public data class MissingProperty<T>(
        public val property: ISpiralProperty.PropertyKey<T>,
        public val errorMessage: String
    ) : SpiralFormatError {
        override fun asException(): Throwable =
            IllegalArgumentException(errorMessage)
    }
//    public object WRONG_FORMAT : FormatWriteResponse()

//    public data class MISSING_PROPERTY(val property: ISpiralProperty.PropertyKey<*>) : FormatWriteResponse()
}

/** ----- Constructors ----- */

public inline fun <T> KorneaResult.Companion.spiralWrongFormat(): KorneaResult<T> =
    failure(SpiralFormatError.WrongFormat)

public inline fun <T> KorneaResult.Companion.spiralWrongFormat(errorMessage: String): KorneaResult<T> =
    failure(SpiralFormatError.WrongFormat(errorMessage))

public inline fun <T> KorneaResult.Companion.spiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String
): KorneaResult<T> =
    failure(SpiralFormatError.MissingProperty(property, errorMessage))

/** ------- Spiral Locale Constructors ------- */

public inline fun <T> SpiralLocale.localisedSpiralWrongFormat(errorMessage: String): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat(localise(errorMessage)))

public inline fun <T> SpiralLocale.localisedSpiralWrongFormat(errorMessage: String, arg: Any): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat(localise(errorMessage, arg)))

public inline fun <T> SpiralLocale.localisedSpiralWrongFormat(
    errorMessage: String,
    arg1: Any,
    arg2: Any
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat(localise(errorMessage, arg1, arg2)))

public inline fun <T> SpiralLocale.localisedSpiralWrongFormat(errorMessage: String, vararg args: Any): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat(localiseArray(errorMessage, args)))

public inline fun <T> SpiralLocale.localisedArraySpiralWrongFormat(
    errorMessage: String,
    args: Array<out Any>
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.WrongFormat(localiseArray(errorMessage, args)))

public inline fun <T> SpiralLocale.localisedSpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty(property, localise(errorMessage)))

public inline fun <T> SpiralLocale.localisedSpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
    arg: Any
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty(property, localise(errorMessage, arg)))

public inline fun <T> SpiralLocale.localisedSpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
    arg1: Any,
    arg2: Any
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty(property, localise(errorMessage, arg1, arg2)))

public inline fun <T> SpiralLocale.localisedSpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
    vararg args: Any
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty(property, localiseArray(errorMessage, args)))

public inline fun <T> SpiralLocale.localisedArraySpiralMissingProperty(
    property: ISpiralProperty.PropertyKey<*>,
    errorMessage: String,
    args: Array<out Any>
): KorneaResult<T> =
    KorneaResult.failure(SpiralFormatError.MissingProperty(property, localiseArray(errorMessage, args)))

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

@Suppress("UNCHECKED_CAST")
public inline fun <T> SpiralGenericFormatResult<T>.filterInner(predicate: (T) -> Boolean): SpiralGenericFormatResult<T> =
    filter { success -> predicate(success.value) }

@Suppress("UNCHECKED_CAST")
public inline fun <R, T : R> SpiralGenericFormatResult<T>.filterInnerOrMap(
    predicate: (T) -> Boolean,
    failedPredicate: (T) -> R
): SpiralGenericFormatResult<R> =
    filterOrMap({ success -> predicate(success.value) }, { success -> success.map(failedPredicate) })

@Suppress("UNCHECKED_CAST")
public inline fun <R, T : R> SpiralGenericFormatResult<T>.filterInnerOrFlatMap(
    predicate: (T) -> Boolean,
    failedPredicate: (T) -> SpiralGenericFormatResult<R>
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