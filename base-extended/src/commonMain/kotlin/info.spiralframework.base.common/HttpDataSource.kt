package info.spiralframework.base.common

import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.DataSourceReproducibility
import info.spiralframework.base.common.io.closeAll
import info.spiralframework.base.common.io.flow.InputFlow
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlin.math.max

@ExperimentalUnsignedTypes
class HttpDataSource(val url: Url, val maxInstanceCount: Int = -1): DataSource<ByteReadChannelInputFlow> {
    private val client = HttpClient()

    override var dataSize: ULong? = null

    private val openInstances: MutableList<ByteReadChannelInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false

    override val reproducibility: DataSourceReproducibility = DataSourceReproducibility(isUnreliable = true)

    override suspend fun openInputFlow(): ByteReadChannelInputFlow? {
        if (canOpenInputFlow()) {
            val stream = ByteReadChannelInputFlow(client.get(url))
            stream.onClose = this::instanceClosed
            openInstances.add(stream)
            return stream
        } else {
            return null
        }
    }

    override fun canOpenInputFlow(): Boolean = !closed && (maxInstanceCount == -1 || openInstances.size < maxInstanceCount)

    private suspend fun instanceClosed(flow: InputFlow) {
        openInstances.remove(flow)
    }

    override suspend fun close() {
        if (!closed) {
            closed = true
            openInstances.toTypedArray().closeAll()
            openInstances.clear()
            client.close()
        }
    }
}