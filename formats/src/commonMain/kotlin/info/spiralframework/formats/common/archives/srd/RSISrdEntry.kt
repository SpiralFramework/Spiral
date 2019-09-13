package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.io.flow.BinaryInputFlow
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.readAndClose
import info.spiralframework.base.common.properties.getValue
import info.spiralframework.base.common.properties.oneTimeMutable
import info.spiralframework.base.common.properties.setValue

@ExperimentalUnsignedTypes
/** ResourceInfoEntry? */
data class RSISrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24525349

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): RSISrdEntry? = BaseSrdEntry(context, dataSource) as? RSISrdEntry
        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): RSISrdEntry = BaseSrdEntry.unsafe(context, dataSource) as RSISrdEntry
    }

    data class ResourceIndex(val start: Int, val length: Int, val unk1: Int, val unk2: Int)

    var unk1: Int by oneTimeMutable()
    var unk2: Int by oneTimeMutable()
    var resourceCount: Int by oneTimeMutable()
    var unk3: Int by oneTimeMutable()
    var unk4: Int by oneTimeMutable()

    var resources: Array<ResourceIndex> by oneTimeMutable()
    var name: String by oneTimeMutable()

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup() {
        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            requireNotNull(dataSource.openInputFlow()).use { setup(it) }
        else {
            setup(BinaryInputFlow(requireNotNull(dataSource.openInputFlow()).readAndClose()))
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: InputFlow) {
        requireNotNull(flow.seek(0, InputFlow.FROM_BEGINNING))

        unk1 = requireNotNull(flow.readInt16LE())
        unk2 = requireNotNull(flow.read())
        resourceCount = requireNotNull(flow.read())
        unk3 = requireNotNull(flow.readInt32LE())
        unk4 = requireNotNull(flow.readInt32LE())

        val nameOffset = requireNotNull(flow.readInt32LE())

        resources = Array(resourceCount) {
            ResourceIndex(requireNotNull(flow.readInt32LE()) and 0x0FFFFFFF, requireNotNull(flow.readInt32LE()), requireNotNull(flow.readInt32LE()), requireNotNull(flow.readInt32LE()))
        }

        flow.seek(nameOffset.toLong(), InputFlow.FROM_BEGINNING)
        name = flow.readNullTerminatedUTF8String()
    }
}