package info.spiralframework.formats.common.archives

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.WindowedDataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readUInt32LE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map

public class AwbArchive(public val unknown1: Int, public val files: Array<AwbFileEntry>, public val dataSource: DataSource<*>): SpiralArchive {
    public companion object {
        /** 'AFS2' */
        public const val MAGIC_NUMBER_LE: Int = 0x32534641

        public const val INVALID_MAGIC_NUMBER: Int = 0x0000

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.awb.not_enough_data"
        public const val INVALID_MAGIC_KEY: String = "formats.awb.invalid_magic"

        public suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<AwbArchive> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
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

    override val fileCount: Int
        get() = files.size

    public fun openSource(file: AwbFileEntry): DataSource<InputFlow> = WindowedDataSource(dataSource, file.offset.toULong(), file.size.toULong(), closeParent = false)
    public suspend fun openFlow(file: AwbFileEntry): KorneaResult<InputFlow> =
            dataSource.openInputFlow().map { parent ->
                WindowedInputFlow(parent, file.offset.toULong(), file.size.toULong())
            }

    override suspend fun SpiralContext.getSubfiles(): Flow<SpiralArchiveSubfile<*>> =
        files.asFlow().map { file -> SpiralArchiveSubfile("${file.id}_awb.dat", openSource(file)) }
}

@Suppress("FunctionName")
@ExperimentalUnsignedTypes
public suspend fun SpiralContext.AwbArchive(dataSource: DataSource<*>): KorneaResult<AwbArchive> = AwbArchive(this, dataSource)

@Suppress("FunctionName")
@ExperimentalUnsignedTypes
public suspend fun SpiralContext.UnsafeAwbArchive(dataSource: DataSource<*>): AwbArchive = AwbArchive(this, dataSource).getOrThrow()