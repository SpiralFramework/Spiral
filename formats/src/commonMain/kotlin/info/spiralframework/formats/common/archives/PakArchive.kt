package info.spiralframework.formats.common.archives

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.OffsetDataSource
import dev.brella.kornea.io.common.WindowedDataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.OffsetInputFlow
import dev.brella.kornea.io.common.flow.SinkOffsetInputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map

public class PakArchive(public val files: Array<PakFileEntry>, public val dataSource: DataSource<*>) : SpiralArchive {
    public companion object {
        public const val MAGIC_NUMBER_LE: Int = 0x2E50414B

        public const val INVALID_FILE_COUNT: Int = 0x0000
        public const val FILE_OFFSET_TOO_LOW: Int = 0x0001
        public const val INVALID_FILE_SIZE: Int = 0x0002
        public const val INVALID_FILE_OFFSET: Int = 0x0003

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.pak.not_enough_data"
        public const val INVALID_FILE_COUNT_KEY: String = "formats.pak.invalid_file_count"
        public const val FILE_OFFSET_TOO_LOW_KEY: String = "formats.pak.offset_too_low"
        public const val INVALID_FILE_SIZE_KEY: String = "formats.pak.invalid_file_size"
        public const val INVALID_FILE_OFFSET_KEY: String = "formats.pak.invalid_file_offset"

        public const val DEFAULT_MIN_FILE_COUNT: Int = 1
        public const val DEFAULT_MAX_FILE_COUNT: Int = 1000
        public const val DEFAULT_MIN_FILE_SIZE: Int = 0
        public const val DEFAULT_MAX_FILE_SIZE: Int = 64_000_000 //64 MB
        public const val DEFAULT_STRICT_OFFSETS: Boolean = false

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>,
            minFileCount: Int = DEFAULT_MIN_FILE_COUNT,
            maxFileCount: Int = DEFAULT_MAX_FILE_COUNT,
            minFileSize: Int = DEFAULT_MIN_FILE_SIZE,
            maxFileSize: Int = DEFAULT_MAX_FILE_SIZE,
            strictOffsets: Boolean = DEFAULT_STRICT_OFFSETS
        ): KorneaResult<PakArchive> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return it.cast() }

                closeAfter(flow) {
                    val possibleMagicNumber =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val fileCount =
                        if (possibleMagicNumber == MAGIC_NUMBER_LE)
                            flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        else
                            possibleMagicNumber

                    if (fileCount !in minFileCount..maxFileCount) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_FILE_COUNT,
                            localise(INVALID_FILE_COUNT_KEY, fileCount, minFileCount, maxFileCount)
                        )
                    }

                    val entryOffsets = IntArray(fileCount) { index ->
                        val offset = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        if (offset < 0) {
                            return@closeAfter KorneaResult.errorAsIllegalArgument(
                                FILE_OFFSET_TOO_LOW,
                                localise(FILE_OFFSET_TOO_LOW_KEY, index, offset)
                            )
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
                                    return@closeAfter KorneaResult.errorAsIllegalArgument(
                                        INVALID_FILE_OFFSET,
                                        localise(INVALID_FILE_OFFSET_KEY, index, offset, lastOffset)
                                    )
                                }

                                lastOffset = offset
                            }

                            size = entryOffsets[index + 1] - offset
                            if (size !in minFileSize..maxFileSize) {
                                return@closeAfter KorneaResult.errorAsIllegalArgument(
                                    INVALID_FILE_SIZE,
                                    localise(INVALID_FILE_SIZE_KEY, index, size, minFileSize, maxFileSize)
                                )
                            }
                        }

                        PakFileEntry(index, size, offset)
                    }

                    return@closeAfter KorneaResult.success(PakArchive(files, dataSource))
                }
            }
    }

    override val fileCount: Int
        get() = files.size

    public operator fun get(index: Int): PakFileEntry = files[index]

    public fun openSource(file: PakFileEntry): DataSource<InputFlow> =
        if (file.size == -1) OffsetDataSource(dataSource, file.offset.toULong(), closeParent = false)
        else WindowedDataSource(dataSource, file.offset.toULong(), file.size.toULong(), closeParent = false)

    public suspend fun openFlow(file: PakFileEntry): KorneaResult<OffsetInputFlow> =
        dataSource.openInputFlow().map { parent ->
            if (file.size == -1)
                SinkOffsetInputFlow(parent, file.offset.toULong())
            else
                WindowedInputFlow(parent, file.offset.toULong(), file.size.toULong())
        }

    override suspend fun SpiralContext.getSubfiles(): Flow<SpiralArchiveSubfile<*>> =
        files.asFlow().map { file -> SpiralArchiveSubfile("${file.index}.dat", openSource(file)) }
}

@Suppress("FunctionName")
public suspend fun SpiralContext.PakArchive(dataSource: DataSource<*>): KorneaResult<PakArchive> = PakArchive(this, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafePakArchive(dataSource: DataSource<*>): PakArchive = PakArchive(this, dataSource).getOrThrow()