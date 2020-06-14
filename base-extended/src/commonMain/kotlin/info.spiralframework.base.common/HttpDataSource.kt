package info.spiralframework.base.common

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.fullPath
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.DataSource.Companion.korneaSourceClosed
import org.abimon.kornea.io.common.DataSource.Companion.korneaSourceUnknown
import org.abimon.kornea.io.common.DataSource.Companion.korneaTooManySourcesOpen
import kotlin.math.max

@ExperimentalUnsignedTypes
class HttpDataSource(val url: Url, val maxInstanceCount: Int = -1, override val location: String? = url.fullPath): DataSource<ByteReadChannelInputFlow> {
    private val client = HttpClient()

    override var dataSize: ULong? = null
    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()

    private val openInstances: MutableList<ByteReadChannelInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override val reproducibility: DataSourceReproducibility = DataSourceReproducibility(isUnreliable = true)

    override suspend fun openNamedInputFlow(location: String?): KorneaResult<ByteReadChannelInputFlow> {
        when {
            closed -> return korneaSourceClosed()
            openInstances.size == maxInstanceCount -> return korneaTooManySourcesOpen(maxInstanceCount)
            canOpenInputFlow() -> {
                val flow = ByteReadChannelInputFlow(client.get(url), location ?: this.location)
                flow.addCloseHandler(this::instanceClosed)
                openInstances.add(flow)
                return KorneaResult.success(flow)
            }
            else -> return korneaSourceUnknown()
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