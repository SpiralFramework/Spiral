package org.abimon.spiral.util.rocketFuel

import com.github.kittinunf.fuel.core.Response
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL

class LargeResponse(
        val url: URL,
        val statusCode: Int = -1,
        val responseMessage: String = "",
        val headers: Map<String, List<String>> = emptyMap(),
        val contentLength: Long = 0L,
        val dataStream: InputStream = ByteArrayInputStream(ByteArray(0))
) {
    override fun toString(): String = buildString {
        appendln("<-- $statusCode ($url)")
        appendln("Response : $responseMessage")
        appendln("Length : $contentLength")
        appendln("Body : ${if (contentLength > 0) "$contentLength Bytes" else "(empty)"}")
        appendln("Headers : (${headers.size})")
        for ((key, value) in headers) {
            appendln("$key : $value")
        }
    }

    companion object {
        fun error(): Response = Response(URL("http://."))
    }
}