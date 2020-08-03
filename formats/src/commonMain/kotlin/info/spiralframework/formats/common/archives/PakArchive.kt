package info.spiralframework.formats.common.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignedTo
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.OffsetInputFlow
import dev.brella.kornea.io.common.flow.SinkOffsetInputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow

@ExperimentalUnsignedTypes
class PakArchive(val files: Array<PakFileEntry>, val dataSource: DataSource<*>) {
    companion object {
        const val MAGIC_NUMBER_LE = 0x2E50414B

        const val INVALID_FILE_COUNT = 0x0000
        const val FILE_OFFSET_TOO_LOW = 0x0001
        const val INVALID_FILE_SIZE = 0x0002
        const val INVALID_FILE_OFFSET = 0x0003

        const val NOT_ENOUGH_DATA_KEY = "formats.pak.not_enough_data"
        const val INVALID_FILE_COUNT_KEY = "formats.pak.invalid_file_count"
        const val FILE_OFFSET_TOO_LOW_KEY = "formats.pak.offset_too_low"
        const val INVALID_FILE_SIZE_KEY = "formats.pak.invalid_file_size"
        const val INVALID_FILE_OFFSET_KEY = "formats.pak.invalid_file_offset"

        const val DEFAULT_MIN_FILE_COUNT = 1
        const val DEFAULT_MAX_FILE_COUNT = 1000
        const val DEFAULT_MIN_FILE_SIZE = 0
        const val DEFAULT_MAX_FILE_SIZE = 64_000_000 //64 MB
        const val DEFAULT_STRICT_OFFSETS = false

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>, minFileCount: Int = DEFAULT_MIN_FILE_COUNT, maxFileCount: Int = DEFAULT_MAX_FILE_COUNT, minFileSize: Int = DEFAULT_MIN_FILE_SIZE, maxFileSize: Int = DEFAULT_MAX_FILE_SIZE, strictOffsets: Boolean = DEFAULT_STRICT_OFFSETS): KorneaResult<PakArchive> =
            withFormats(context) {
                val flow = dataSource.openInputFlow().getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val possibleMagicNumber = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val fileCount =
                            if (possibleMagicNumber == MAGIC_NUMBER_LE)
                                flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            else
                                possibleMagicNumber
                    if (fileCount !in minFileCount .. maxFileCount) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FILE_COUNT, localise(INVALID_FILE_COUNT_KEY, fileCount, minFileCount, maxFileCount))
                    }

                    val entryOffsets = IntArray(fileCount) { index ->
                        val offset = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        if (offset < 0) {
                            return@closeAfter KorneaResult.errorAsIllegalArgument(FILE_OFFSET_TOO_LOW, localise(FILE_OFFSET_TOO_LOW_KEY, index, offset))
                        }

                        offset
                    }

                    var lastOffset = entryOffsets[0]

                    val files = Array(fileCount) { index ->
                        val offset = entryOffsets[index]
                        val size: Int
                        if (index == fileCount - 1) {
                            size = -1
                        } else {
                            if (strictOffsets && offset != 0) {
                                if (offset < lastOffset) {
                                    return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FILE_OFFSET, localise(INVALID_FILE_OFFSET_KEY, index, offset, lastOffset))
                                }

                                lastOffset = offset
                            }

                            size = entryOffsets[index + 1] - offset
                            if (size !in minFileSize .. maxFileSize) {
                                return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FILE_SIZE, localise(INVALID_FILE_SIZE_KEY, index, size, minFileSize, maxFileSize))
                            }
                        }

                        PakFileEntry(index, size, offset)
                    }

                    return@closeAfter KorneaResult.success(PakArchive(files, dataSource))
                }
            }
    }

    operator fun get(index: Int): PakFileEntry = files[index]

    suspend fun openSource(file: PakFileEntry): DataSource<out InputFlow> = if (file.size == -1) OffsetDataSource(dataSource, file.offset.toULong(), closeParent = false) else WindowedDataSource(dataSource, file.offset.toULong(), file.size.toULong(), closeParent = false)
    suspend fun openFlow(file: PakFileEntry): KorneaResult<OffsetInputFlow> =
            dataSource.openInputFlow().map { parent ->
                if (file.size == -1)
                    SinkOffsetInputFlow(parent, file.offset.toULong())
                else
                    WindowedInputFlow(parent, file.offset.toULong(), file.size.toULong())
            }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.PakArchive(dataSource: DataSource<*>) = PakArchive(this, dataSource)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafePakArchive(dataSource: DataSource<*>) = PakArchive(this, dataSource).get()