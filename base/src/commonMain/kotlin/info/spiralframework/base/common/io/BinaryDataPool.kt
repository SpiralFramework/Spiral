package info.spiralframework.base.common.io

import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.common.io.flow.BinaryInputFlow
import kotlin.math.max

@ExperimentalUnsignedTypes
class BinaryDataPool(val output: BinaryOutputFlow = BinaryOutputFlow(), val maxInstanceCount: Int = -1): DataPool<BinaryInputFlow, BinaryOutputFlow> {
    override val dataSize: ULong?
        get() = output.getDataSize()

    override val reproducibility: DataSourceReproducibility = DataSourceReproducibility(isDeterministic = true, isRandomAccess = true)
    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()

    private val openInstances: MutableList<BinaryInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false
    private var outputClosed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override suspend fun openInputFlow(): BinaryInputFlow? {
        if (canOpenInputFlow()) {
            val stream = BinaryInputFlow(output.getData())
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

    private suspend fun onOutputClosed(closeable: DataCloseable) {
        if (closeable is BinaryOutputFlow) {
            outputClosed = true
            output.close()
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

    override suspend fun openOutputFlow(): BinaryOutputFlow? = if (canOpenOutputFlow()) output else null
    override suspend fun canOpenOutputFlow(): Boolean = !outputClosed

    init {
        output.addCloseHandler(this::onOutputClosed)
    }
}