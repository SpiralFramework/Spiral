package org.abimon.spiral.util.rocketFuel

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.util.TestConfiguration
import com.github.kittinunf.result.Result
import org.abimon.spiral.core.userAgent
import java.io.InputStream
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinProperty

@Suppress("UNCHECKED_CAST")
val responseDataStream: KProperty<InputStream> = Response::class.java.getDeclaredField("dataStream").run {
    isAccessible = true
    return@run this.kotlinProperty as KProperty<InputStream>
}

@Suppress("UNCHECKED_CAST")
//val fuelTestConfiguration: KProperty<TestConfiguration> = Fuel.Companion::class.java.getDeclaredField("testConfiguration").run {
//    isAccessible = true
//    return@run this.kotlinProperty as KProperty<TestConfiguration>
//}
val fuelTestConfiguration: KProperty<TestConfiguration> = Fuel.Companion::class["testConfiguration"]
val socketFactoryProperty: KProperty<SSLSocketFactory> = Request::class["socketFactory"]
val hostnameVerifierProperty: KProperty<HostnameVerifier> = Request::class["hostnameVerifier"]
val requestInterceptorProperty: KProperty<((Request) -> Request)> = Request::class["requestInterceptor"]
val responseInterceptorProperty: KProperty<((Request, Response) -> Response)> = Request::class["responseInterceptor"]

val Response.stream: InputStream
    get() = responseDataStream.call(this)

/** Not necessary but keeps appearances up */
val Fuel.Companion.publicTestConfiguration: TestConfiguration
    get() = fuelTestConfiguration.call(Fuel.Companion)

val Request.publicSocketFactory: SSLSocketFactory?
    get() = socketFactoryProperty.call(this)

val Request.publicHostnameVerifier: HostnameVerifier?
    get() = hostnameVerifierProperty.call(this)

val Request.publicRequestInterceptor: ((Request) -> Request)?
    get() = requestInterceptorProperty.call(this)

val Request.publicResponseInterceptor: ((Request, Response) -> Response)?
    get() = responseInterceptorProperty.call(this)

fun Request.responseStream() = response(object: Deserializable<InputStream> {
    override fun deserialize(response: Response): InputStream = response.stream
})

@Suppress("UNCHECKED_CAST")
operator fun <C: Any, T> KClass<C>.get(property: String): KProperty<T> {
    val field = this.declaredMemberProperties.firstOrNull { field -> field.name == property } ?: throw NoSuchFieldException(property)
    field.isAccessible = true
    return field as KProperty<T>
}

fun Request.largeResponse(): Pair<Request, Result<LargeResponse, FuelError>> = try {
    this.userAgent()
    val response = LargeHttpClient[null].executeRequest(this)
    Pair(this, Result.Success(response))
} catch (error: FuelError) {
    Pair(this, Result.error(error))
}