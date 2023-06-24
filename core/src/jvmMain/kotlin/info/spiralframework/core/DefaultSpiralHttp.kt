package info.spiralframework.core

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*

public class DefaultSpiralHttp: SpiralHttp {
    override val httpClient: HttpClient = HttpClient(CIO) {
        followRedirects = true
        expectSuccess = false

        // Shortcut for the curl-like user agent.
        CurlUserAgent()
    }
}