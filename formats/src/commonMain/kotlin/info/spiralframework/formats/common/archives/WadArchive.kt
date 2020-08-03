package info.spiralframework.formats.common.archives

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readString
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.toolkit.common.SemanticVersion

@ExperimentalUnsignedTypes
class WadArchive(val version: SemanticVersion, val header: ByteArray, val files: Array<WadFileEntry>, val directories: Array<WadDirectoryEntry>, val dataOffset: ULong, val dataSource: DataSource<*>) {
    @ExperimentalStdlibApi
    companion object {
        /** 'AGAR' */
        const val MAGIC_NUMBER_LE = 0x52414741

        /** 'AGAR' */
        const val MAGIC_NUMBER_BE = 0x41474152

        const val INVALID_MAGIC_NUMBER = 0x0000
        const val HEADER_INCOMPLETE = 0x0001

        const val NOT_ENOUGH_DATA_KEY = "formats.wad.not_enough_data"
        const val INVALID_MAGIC_NUMBER_KEY = "formats.wad.invalid_magic_number"
        const val HEADER_INCOMPLETE_KEY = "formats.wad.header_incomplete"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<WadArchive> =
            withFormats(context) {
                val flow = dataSource.openInputFlow().getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_MAGIC_NUMBER, localise(INVALID_MAGIC_NUMBER_KEY, "0x${magic.toString(16)}", "0x${MAGIC_NUMBER_LE.toString(16)}"))
                    }

                    val majorVersion = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorVersion = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val headerSize = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val header = ByteArray(headerSize)
                    val headerBytesRead = flow.read(header)
                    if (headerBytesRead != headerSize) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(HEADER_INCOMPLETE, localise(HEADER_INCOMPLETE_KEY, headerSize, headerBytesRead
                                ?: constNull()))
                    }

                    val fileCount = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val files = Array(fileCount) {
                        val nameLength = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val name = flow.readString(nameLength, encoding = TextCharsets.UTF_8)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val fileSize = flow.readInt64LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val fileOffset = flow.readInt64LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                        WadFileEntry(name, fileSize, fileOffset)
                    }

                    val directoryCount = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val directories = Array(directoryCount) {
                        val nameLength = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val name = flow.readString(nameLength, encoding = TextCharsets.UTF_8)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val subEntryCount = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                        val subEntries = Array(subEntryCount) {
                            val subNameLength = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val subName = flow.readString(subNameLength, encoding = TextCharsets.UTF_8)
                                    ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val isDirectory = flow.read()?.equals(1)
                                    ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                            WadSubEntry(subName, isDirectory)
                        }

                        WadDirectoryEntry(name, subEntries)
                    }

                    val dataOffset = flow.position()

                    return@closeAfter KorneaResult.success(WadArchive(SemanticVersion(majorVersion, minorVersion), header, files, directories, dataOffset, dataSource))
                }
            }
    }

    operator fun get(name: String): WadFileEntry? = files.firstOrNull { entry -> entry.name == name }

    suspend fun openSource(file: WadFileEntry): DataSource<out InputFlow> =
            WindowedDataSource(dataSource, dataOffset + file.offset.toULong(), file.size.toULong(), closeParent = false)

    suspend fun openFlow(file: WadFileEntry): KorneaResult<InputFlow> =
            dataSource.openInputFlow().map { parent -> WindowedInputFlow(parent, dataOffset + file.offset.toULong(), file.size.toULong()) }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.WadArchive(dataSource: DataSource<*>) = WadArchive(this, dataSource)

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.UnsafeWadArchive(dataSource: DataSource<*>) = WadArchive(this, dataSource).get()