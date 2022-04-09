package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.SeekableInputFlow
import dev.brella.kornea.io.common.flow.bookmark
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readNullTerminatedUTF8String
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData

/** Texture Information? */
public data class TXISrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int
) : SrdEntryWithData.WithRsiSubdata(classifier, mainDataLength, subDataLength, unknown) {
    public companion object {
        public const val MAGIC_NUMBER_BE: Int = 0x24545849
    }

    var textureNames: Array<String> by oneTimeMutableInline()

    val fileID: String
        get() = rsiEntry.name

    override suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<TXISrdEntry> {
        flow.seek(0, EnumSeekMode.FROM_BEGINNING)

        val textureCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        val textureNameOffset = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        flow.seek(textureNameOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
        textureNames = Array(textureCount) {
            val nameOffset = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
            bookmark(flow) {
                flow.seek(nameOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
                flow.readNullTerminatedUTF8String()
            }
        }

        return KorneaResult.success(this@TXISrdEntry)
    }

    override suspend fun SpiralContext.writeMainData(out: OutputFlow) {
        TODO("Not yet implemented")
    }

    override suspend fun SpiralContext.writeSubData(out: OutputFlow) {
        TODO("Not yet implemented")
    }
}