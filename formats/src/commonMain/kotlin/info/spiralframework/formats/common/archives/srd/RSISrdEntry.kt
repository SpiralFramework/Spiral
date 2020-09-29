package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedUTF8String
import info.spiralframework.base.common.locale.localisedNotEnoughData
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.filterToInstance
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import info.spiralframework.base.common.logging.SpiralLogger.NoOp.trace

@ExperimentalUnsignedTypes
/** ResourceInfoEntry? */
data class RSISrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : SrdEntryWithData(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24525349

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<RSISrdEntry> = BaseSrdEntry(context, dataSource)
            .also { context.trace("'RSI': {0}", it) }
            .filterToInstance()
    }

    data class ResourceIndex(val start: Int, val length: Int, val unk1: Int, val unk2: Int)
    data class LabelledResourceIndex(val name: Int, val start: Int, val length: Int, val unk2: Int)

    var locationKey: String by oneTimeMutableInline()
    var setupFlow: InputFlow by oneTimeMutableInline()

    var unk1: Int by oneTimeMutableInline()
    var unk2: Int by oneTimeMutableInline()
    var unk3: Int by oneTimeMutableInline()
    var resourceCount: Int by oneTimeMutableInline()
    var unk4: Int by oneTimeMutableInline()
    var unk5: Int by oneTimeMutableInline()
    var unk6: Int by oneTimeMutableInline()
    var unk7: Int by oneTimeMutableInline()

    var resources: Array<ResourceIndex> by oneTimeMutableInline()
    var name: String by oneTimeMutableInline()

    override suspend fun <T> SpiralContext.setup(flow: T): KorneaResult<RSISrdEntry> where T: InputFlowState<SeekableInputFlow>, T: IntFlowState {
        locationKey = flow.location.toString()
        setupFlow = flow
        debug("Setting up RSI entry @ {0}", locationKey)

        flow.seek(0, EnumSeekMode.FROM_BEGINNING)

        debug("[{0}] Reading RSI entry @ {1}", locationKey, (flow.flow as? InputFlowWithBacking)?.absPosition() ?: flow.position())

        if (locationKey.endsWith("+5560h[A0h,120h][10h,65h]")) {
            println()
        }

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

        flow.seek(nameOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
        name = flow.readNullTerminatedUTF8String()

        return KorneaResult.success(this@RSISrdEntry)
    }
}