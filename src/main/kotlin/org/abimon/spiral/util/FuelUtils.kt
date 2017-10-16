package org.abimon.spiral.util

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.response
import java.io.InputStream
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.kotlinProperty

@Suppress("UNCHECKED_CAST")
val responseDataStream: KProperty<InputStream> = Response::class.java.getDeclaredField("dataStream").run {
    isAccessible = true
    return@run this.kotlinProperty as KProperty<InputStream>
}

val Response.stream: InputStream
    get() = responseDataStream.call(this)

fun Request.responseStream() = response(object: Deserializable<InputStream> {
    override fun deserialize(response: Response): InputStream = response.stream
})