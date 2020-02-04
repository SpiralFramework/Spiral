package info.spiralframework.base.common

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.Url
import org.abimon.kornea.io.common.*
import kotlin.math.max

@ExperimentalUnsignedTypes
class HttpDataSource(val url: Url, val maxInstanceCount: Int = -1): DataSource<ByteReadChannelInputFlow> {
    private val client = HttpClient()

    override var dataSize: ULong? = null
    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()

    private val openInstances: MutableList<ByteReadChannelInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override val reproducibility: DataSourceReproducibility = DataSourceReproducibility(isUnreliable = true)

    override suspend fun openInputFlow(): ByteReadChannelInputFlow? {
        if (canOpenInputFlow()) {
            val stream = ByteReadChannelInputFlow(client.get(url))
            stream.addCloseHandler(this::instanceClosed)
            openInstances.add(stream)
            return stream
        } else {
            return null
        }
    }

    override suspend fun canOpenInputFlow(): Boolean = !closed && (maxInstanceCount == -1 || openInstances.size < maxInstanceCount)

    private suspend fun instanceClosed(closeable: DataCloseable) {
        if (closeable is ByteReadChannelInputFlow) {
            openInstances.remove(closeable)
        }
    }

    override suspend fun close() {
        super.close()

        if (!closed) {
            closed = true
            openInstances.toTypedArray().closeAll()
            openInstances.clear()
            client.close()
        }
    }
}