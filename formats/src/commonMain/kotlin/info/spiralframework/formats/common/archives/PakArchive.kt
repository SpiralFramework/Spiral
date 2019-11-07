package info.spiralframework.formats.common.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.WindowedDataSource
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.WindowedInputFlow
import info.spiralframework.base.common.io.readInt32LE
import info.spiralframework.base.common.io.use
import info.spiralframework.formats.common.withFormats

@ExperimentalUnsignedTypes
class PakArchive(val files: Array<PakFileEntry>, val dataSource: DataSource<*>) {
    companion object {
        const val DEFAULT_MIN_FILE_COUNT = 1
        const val DEFAULT_MAX_FILE_COUNT = 1000
        const val DEFAULT_MIN_FILE_SIZE = 0
        const val DEFAULT_MAX_FILE_SIZE = 64_000_000 //64 MB

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>, minFileCount: Int = DEFAULT_MIN_FILE_COUNT, maxFileCount: Int = DEFAULT_MAX_FILE_COUNT, minFileSize: Int = DEFAULT_MIN_FILE_SIZE, maxFileSize: Int = DEFAULT_MAX_FILE_SIZE): PakArchive? {
            try {
                return unsafe(context, dataSource, minFileCount, maxFileCount, minFileSize, maxFileSize)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.pak.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>, minFileCount: Int = DEFAULT_MIN_FILE_COUNT, maxFileCount: Int = DEFAULT_MAX_FILE_COUNT, minFileSize: Int = DEFAULT_MIN_FILE_SIZE, maxFileSize: Int = DEFAULT_MAX_FILE_SIZE): PakArchive {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.pak.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val fileCount = requireNotNull(flow.readInt32LE(), notEnoughData)
                    require(fileCount in minFileCount..maxFileCount) { localise("formats.pak.invalid_file_count", fileCount, minFileCount, maxFileCount) }

                    val entryOffsets = IntArray(fileCount) { index ->
                        val offset = requireNotNull(flow.readInt32LE())
                        require(offset >= 0) { localise("formats.pak.offset_too_low", index, offset) }

                        offset
                    }

                    val files = Array(fileCount) { index ->
                        val offset = entryOffsets[index]
                        val size: Int
                        if (index == fileCount - 1) {
                            size = -1
                        } else {
                            size = entryOffsets[index + 1] - offset
                            require(size in minFileSize..maxFileSize) { localise("formats.pak.invalid_file_size", index, size, minFileSize, maxFileSize) }
                        }

                        PakFileEntry(index, size, offset)
                    }

                    return PakArchive(files, dataSource)
                }
            }
        }
    }

    operator fun get(index: Int): PakFileEntry = files[index]

    suspend fun openSource(file: PakFileEntry): DataSource<out InputFlow> = WindowedDataSource(dataSource, file.offset.toULong(), file.size.toULong())
    suspend fun openFlow(file: PakFileEntry): InputFlow? {
        val parent = dataSource.openInputFlow() ?: return null
        return WindowedInputFlow(parent, file.offset.toULong(), file.size.toULong())
    }
}