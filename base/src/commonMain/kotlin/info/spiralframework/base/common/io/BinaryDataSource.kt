package info.spiralframework.base.common.io

import info.spiralframework.base.common.io.flow.BinaryInputFlow
import kotlin.math.max

@ExperimentalUnsignedTypes
class BinaryDataSource(val byteArray: ByteArray, val maxInstanceCount: Int = -1): DataSource<BinaryInputFlow> {
    companion object {}
    override val dataSize: ULong
        get() = byteArray.size.toULong()
    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()

    private val openInstances: MutableList<BinaryInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override val reproducibility: DataSourceReproducibility
        = DataSourceReproducibility(isStatic = true, isRandomAccess = true)

    override suspend fun openInputFlow(): BinaryInputFlow? {
        if (canOpenInputFlow()) {
            val stream = BinaryInputFlow(byteArray)
            stream.addCloseHandler(this::instanceClosed)
            openInstances.add(stream)
            return stream
        } else {
            return null
        }
    }
    override suspend fun canOpenInputFlow(): Boolean = !closed && (maxInstanceCount == -1 || openInstances.size < maxInstanceCount)

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
}