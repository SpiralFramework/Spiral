package info.spiralframework.base.js.io

import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.io.flow.BinaryInputFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.xhr.ARRAYBUFFER
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import kotlin.js.Promise
import kotlin.math.max

@ExperimentalUnsignedTypes
class AjaxDataSource (val url: String, val maxInstanceCount: Int = -1) : DataSource<BinaryInputFlow> {
    companion object {
        fun async(url: String, maxInstanceCount: Int = -1): Promise<AjaxDataSource> = GlobalScope.promise { AjaxDataSource(url, maxInstanceCount) }
    }

    private var data: ByteArray? = null
    private val dataMutex: Mutex = Mutex()
    private val dataPromise: Promise<ArrayBuffer?>
    override val dataSize: ULong?
        get() = data?.size?.toULong()
    override val reproducibility: DataSourceReproducibility = DataSourceReproducibility(isStatic = true, isRandomAccess = true)

    private val openInstances: MutableList<BinaryInputFlow> = ArrayList(max(maxInstanceCount, 0))
    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()
    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override suspend fun openInputFlow(): BinaryInputFlow? {
        waitIfNeeded()

        if (canOpenInputFlow()) {
            val stream = BinaryInputFlow(data!!)
            stream.addCloseHandler(this::instanceClosed)
            openInstances.add(stream)
            return stream
        } else {
            return null
        }
    }
    override suspend fun canOpenInputFlow(): Boolean {
        waitIfNeeded()

        return !closed && data != null && (maxInstanceCount == -1 || openInstances.size < maxInstanceCount)
    }

    private suspend fun instanceClosed(closeable: DataCloseable) {
        if (closeable is BinaryInputFlow) {
            openInstances.remove(closeable)
        }
    }

    override suspend fun close() {
        super.close()

        if (!closed) {
            closed = true
            openInstances.toTypedArray().closeAll()
            openInstances.clear()
        }
    }

    private suspend fun waitIfNeeded() {
        dataMutex.withLock {
            if (!closed && data == null) {
                val buffer = dataPromise.await()

                if (buffer == null) {
                    close()
                } else {
                    data = Int8Array(buffer).asDynamic() as ByteArray
                }
            }
        }
    }

    init {
        dataPromise = Promise { resolve: (ArrayBuffer?) -> Unit, _: (Throwable) -> Unit ->
            val headRequest = XMLHttpRequest()
            headRequest.open("GET", url)
            headRequest.responseType = XMLHttpRequestResponseType.ARRAYBUFFER
            headRequest.onreadystatechange = { if (headRequest.readyState == XMLHttpRequest.DONE) resolve(if (headRequest.status.toInt() == 200) headRequest.response as ArrayBuffer else null) }
            headRequest.send()
        }
    }
}