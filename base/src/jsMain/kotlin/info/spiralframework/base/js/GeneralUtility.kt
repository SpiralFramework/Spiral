package info.spiralframework.base.js

import kotlinx.coroutines.await
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

suspend fun urlExists(url: String): Boolean =
        Promise { resolve: (Boolean) -> Unit, _: (Throwable) -> Unit ->
        val headRequest = XMLHttpRequest()
        headRequest.open("HEAD", url)
        headRequest.onreadystatechange = { if (headRequest.readyState == XMLHttpRequest.DONE) resolve(headRequest.status.toInt() == 404) }
        headRequest.send()
    }.await()