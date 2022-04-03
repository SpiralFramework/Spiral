package info.spiralframework.core

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*

class DefaultSpiralHttp: SpiralHttp {
    override val httpClient: HttpClient = HttpClient(Apache) {
        followRedirects = true
        expectSuccess = false

        // Shortcut for the curl-like user agent.
        CurlUserAgent()
    }
}