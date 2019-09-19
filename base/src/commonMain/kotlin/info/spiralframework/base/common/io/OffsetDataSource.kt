package info.spiralframework.base.common.io

import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.OffsetInputFlow
import info.spiralframework.base.common.io.flow.setCloseHandler
import kotlin.math.max

@ExperimentalUnsignedTypes
class OffsetDataSource(val parent: DataSource<*>, val offset: ULong, val maxInstanceCount: Int = -1, val closeParent: Boolean = true) : DataSource<OffsetInputFlow> {
    companion object {}

    override val dataSize: ULong?
        get() = parent.dataSize
    private val openInstances: MutableList<OffsetInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false

    override val reproducibility: DataSourceReproducibility
        get() = parent.reproducibility or DataSourceReproducibility.DETERMINISTIC_MASK

    override suspend fun openInputFlow(): OffsetInputFlow? {
        if (canOpenInputFlow()) {
            val parentFlow = parent.openInputFlow() ?: return null
            val flow = OffsetInputFlow(parentFlow, offset)
            flow.setCloseHandler(this::instanceClosed)
            openInstances.add(flow)
            return flow
        } else {
            return null
        }
    }

    override fun canOpenInputFlow(): Boolean = !closed && parent.canOpenInputFlow() && (maxInstanceCount == -1 || openInstances.size < maxInstanceCount)

    private suspend fun instanceClosed(flow: InputFlow) {
        openInstances.remove(flow)
    }

    override suspend fun close() {
        if (!closed) {
            closed = true
            openInstances.toTypedArray().closeAll()
            openInstances.clear()

            if (closeParent) {
                parent.close()
            }
        }
    }
}