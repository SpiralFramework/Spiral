package info.spiralframework.base.common.io

import info.spiralframework.base.common.io.flow.BinaryInputFlow
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.setCloseHandler
import kotlin.math.max

@ExperimentalUnsignedTypes
class BinaryDataSource(val byteArray: ByteArray, val maxInstanceCount: Int = -1): DataSource<BinaryInputFlow> {
    companion object {}
    override val dataSize: ULong = byteArray.size.toULong()
    private val openInstances: MutableList<BinaryInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false

    override val reproducibility: DataSourceReproducibility
        = DataSourceReproducibility(isStatic = true, isRandomAccess = true)

    override suspend fun openInputFlow(): BinaryInputFlow? {
        if (canOpenInputFlow()) {
            val stream = BinaryInputFlow(byteArray)
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

    override suspend fun close() {
        if (!closed) {
            closed = true
            openInstances.toTypedArray().closeAll()
            openInstances.clear()
        }
    }
}