package info.spiralframework.core

import io.ktor.client.*

interface SpiralHttp {
    val httpClient: HttpClient
}