package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedUTF8String
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.properties.getValue
import info.spiralframework.base.common.properties.oneTimeMutable
import info.spiralframework.base.common.properties.setValue
import info.spiralframework.base.common.useAndFlatMap
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.erorrs.common.filterToInstance
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readInt32LE

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

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<RSISrdEntry> = BaseSrdEntry(context, dataSource).filterToInstance()
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
    override suspend fun SpiralContext.setup(): KorneaResult<RSISrdEntry> {
        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            return dataSource.openInputFlow().useAndFlatMap { flow -> setup(flow) }
        else {
            return dataSource.openInputFlow().useAndFlatMap { flow -> setup(BinaryInputFlow(flow.readBytes())) }
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: InputFlow): KorneaResult<RSISrdEntry> {
        flow.seek(0, InputFlow.FROM_BEGINNING) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        unk1 = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk2 = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk3 = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        resourceCount = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk4 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk5 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk6 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk7 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        val nameOffset = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        resources = Array(resourceCount) {
            ResourceIndex(
                    flow.readInt32LE()?.and(0x0FFFFFFF) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
            )
        }

        flow.seek(nameOffset.toLong(), InputFlow.FROM_BEGINNING)
        name = flow.readNullTerminatedUTF8String()

        return KorneaResult.Success(this@RSISrdEntry)
    }
}