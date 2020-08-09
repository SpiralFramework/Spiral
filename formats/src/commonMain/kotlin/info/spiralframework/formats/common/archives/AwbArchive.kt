package info.spiralframework.formats.common.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readUInt32LE
import dev.brella.kornea.toolkit.common.closeAfter

@ExperimentalUnsignedTypes
class AwbArchive(val unknown1: Int, val files: Array<AwbFileEntry>, val dataSource: DataSource<*>) {
    companion object {
        /** 'AFS2' */
        const val MAGIC_NUMBER_LE = 0x32534641

        const val INVALID_MAGIC_NUMBER = 0x0000

        const val NOT_ENOUGH_DATA_KEY = "formats.awb.not_enough_data"
        const val INVALID_MAGIC_KEY = "formats.awb.invalid_magic"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<AwbArchive> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .mapWithState(InputFlowStateSelector::int)
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_MAGIC_NUMBER, localise(INVALID_MAGIC_KEY, "0x${magic.toString(16)}", "0x${MAGIC_NUMBER_LE.toString(16)}"))
                    }

                    val unk1 = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val numEntries = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val align = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val awbFileIDs = IntArray(numEntries) {
                        flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    }
                    val headerEnd = flow.readUInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val awbFileEnds = UIntArray(numEntries) {
                        flow.readUInt32LE() ?: return@closeAfter korneaNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    }

                    var start: UInt
                    var end: UInt = headerEnd

                    val entries = Array(numEntries) { index ->
                        start = end
                        start += (end alignmentNeededFor align).toUInt()
                        end = awbFileEnds[index]

                        AwbFileEntry(awbFileIDs[index], start, end - start)
                    }

                    return@closeAfter KorneaResult.success(AwbArchive(unk1, entries, dataSource))
                }
            }
        }

    suspend fun openSource(file: AwbFileEntry): DataSource<out InputFlow> = WindowedDataSource(dataSource, file.offset.toULong(), file.size.toULong(), closeParent = false)
    suspend fun openFlow(file: AwbFileEntry): KorneaResult<InputFlow> =
            dataSource.openInputFlow().map { parent ->
                WindowedInputFlow(parent, file.offset.toULong(), file.size.toULong())
            }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.AwbArchive(dataSource: DataSource<*>) = AwbArchive(this, dataSource)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeAwbArchive(dataSource: DataSource<*>) = AwbArchive(this, dataSource).get()