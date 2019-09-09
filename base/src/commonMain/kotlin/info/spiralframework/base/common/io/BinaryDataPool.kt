package info.spiralframework.base.common.io

import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.common.io.flow.BinaryInputFlow
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.OutputFlow
import info.spiralframework.base.common.io.flow.setCloseHandler
import kotlin.math.max

@ExperimentalUnsignedTypes
class BinaryDataPool(val output: BinaryOutputFlow = BinaryOutputFlow(), val maxInstanceCount: Int = -1): DataPool<BinaryInputFlow, BinaryOutputFlow> {
    override val dataSize: ULong?
        get() = output.getDataSize()

    override val reproducibility: DataSourceReproducibility = DataSourceReproducibility(isDeterministic = true, isRandomAccess = true)
    private val openInstances: MutableList<BinaryInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false
    private var outputClosed: Boolean = false

    override suspend fun openInputFlow(): BinaryInputFlow? {
        if (canOpenInputFlow()) {
            val stream = BinaryInputFlow(output.getData())
            stream.setCloseHandler(this::instanceClosed)
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

    private suspend fun onOutputClosed(flow: OutputFlow) {
        outputClosed = true
    }

    override suspend fun close() {
        if (!closed) {
            closed = true
            if (!outputClosed)
                output.close()
            openInstances.closeAll()
            openInstances.clear()
        }
    }

    override fun openOutputFlow(): BinaryOutputFlow? = if (canOpenOutputFlow()) output else null
    override fun canOpenOutputFlow(): Boolean = !outputClosed

    init {
        output.onClose = this::onOutputClosed
    }
}