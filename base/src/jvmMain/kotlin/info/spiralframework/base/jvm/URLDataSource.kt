package info.spiralframework.base.jvm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.jvm.JVMInputFlow
import java.net.URL
import kotlin.math.max

class URLDataSource(val url: URL, val maxInstanceCount: Int = -1, override val location: String? = url.toExternalForm()) : DataSource<JVMInputFlow> {
    companion object {}

    override val dataSize: ULong by lazy { url.openConnection().contentLengthLong.toULong() }
    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()

    private val openInstances: MutableList<JVMInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override val reproducibility: DataSourceReproducibility =
            DataSourceReproducibility(isUnreliable = true)

    override suspend fun openNamedInputFlow(location: String?): KorneaResult<JVMInputFlow> {
        when {
            closed -> return KorneaResult.Error(DataSource.ERRORS_SOURCE_CLOSED, "Instance closed")
            canOpenInputFlow() -> {
                val stream = withContext(Dispatchers.IO) { JVMInputFlow(url.openStream(), location ?: this@URLDataSource.location) }
                stream.addCloseHandler(this::instanceClosed)
                openInstances.add(stream)
                return KorneaResult.Success(stream)
            }
            else -> return KorneaResult.Error(
                    DataSource.ERRORS_TOO_MANY_SOURCES_OPEN,
                    "Too many instances open (${openInstances.size}/${maxInstanceCount})"
            )
        }
    }

    override suspend fun canOpenInputFlow(): Boolean = !closed && (maxInstanceCount == -1 || openInstances.size < maxInstanceCount)

    private suspend fun instanceClosed(closeable: ObservableDataCloseable) {
        if (closeable is JVMInputFlow) {
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
}