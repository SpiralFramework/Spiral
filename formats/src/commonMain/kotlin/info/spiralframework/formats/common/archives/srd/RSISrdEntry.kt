package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedUTF8String
import info.spiralframework.base.common.properties.getValue
import info.spiralframework.base.common.properties.oneTimeMutable
import info.spiralframework.base.common.properties.setValue
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.readAndClose
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.use

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
    data class LabelledResourceIndex(val name: Int, val start: Int, val length: Int, val unk2: Int)

    var unk1: Int by oneTimeMutable()
    var unk2: Int by oneTimeMutable()
    var unk3: Int by oneTimeMutable()
    var resourceCount: Int by oneTimeMutable()
    var unk4: Int by oneTimeMutable()
    var unk5: Int by oneTimeMutable()
    var unk6: Int by oneTimeMutable()
    var unk7: Int by oneTimeMutable()

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

        unk1 = requireNotNull(flow.read())
        unk2 = requireNotNull(flow.read())
        unk3 = requireNotNull(flow.read())
        resourceCount = requireNotNull(flow.read())
        unk4 = requireNotNull(flow.readInt16LE())
        unk5 = requireNotNull(flow.readInt16LE())
        unk6 = requireNotNull(flow.readInt16LE())
        unk7 = requireNotNull(flow.readInt16LE())

        val nameOffset = requireNotNull(flow.readInt32LE())

        resources = Array(resourceCount) {
            ResourceIndex(requireNotNull(flow.readInt32LE()) and 0x0FFFFFFF, requireNotNull(flow.readInt32LE()), requireNotNull(flow.readInt32LE()), requireNotNull(flow.readInt32LE()))
        }

        flow.seek(nameOffset.toLong(), InputFlow.FROM_BEGINNING)
        name = flow.readNullTerminatedUTF8String()
    }
}