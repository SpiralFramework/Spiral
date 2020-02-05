package info.spiralframework.formats.common.archives

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readString
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.WindowedInputFlow

@ExperimentalUnsignedTypes
class WadArchive(val version: SemanticVersion, val header: ByteArray, val files: Array<WadFileEntry>, val directories: Array<WadDirectoryEntry>, val dataOffset: ULong, val dataSource: DataSource<*>) {
    @ExperimentalStdlibApi
    companion object {
        /** 'AGAR' */
        const val MAGIC_NUMBER_LE = 0x52414741
        /** 'AGAR' */
        const val MAGIC_NUMBER_BE = 0x41474152

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): WadArchive? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.wad.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): WadArchive {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.wad.not_enough_data") }
                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val magic = requireNotNull(flow.readInt32LE())
                    require(magic == MAGIC_NUMBER_LE) { localise("formats.wad.invalid_magic", "0x${magic.toString(16)}", "0x${MAGIC_NUMBER_LE.toString(16)}") }

                    val majorVersion = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val minorVersion = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val headerSize = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val header = ByteArray(headerSize)
                    val headerBytesRead = flow.read(header)
                    require(headerBytesRead == headerSize) {
                        localise("formats.wad.header_incomplete", headerSize, headerBytesRead ?: constNull())
                    }

                    val fileCount = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val files = Array(fileCount) {
                        val nameLength = requireNotNull(flow.readInt32LE(), notEnoughData)
                        val name = flow.readString(nameLength, encoding = TextCharsets.UTF_8)
                        val fileSize = requireNotNull(flow.readInt64LE(), notEnoughData)
                        val fileOffset = requireNotNull(flow.readInt64LE(), notEnoughData)

                        WadFileEntry(name, fileSize, fileOffset)
                    }

                    val directoryCount = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val directories = Array(directoryCount) {
                        val nameLength = requireNotNull(flow.readInt32LE(), notEnoughData)
                        val name = flow.readString(nameLength, encoding = TextCharsets.UTF_8)
                        val subEntryCount = requireNotNull(flow.readInt32LE(), notEnoughData)

                        val subEntries = Array(subEntryCount) {
                            val subNameLength = requireNotNull(flow.readInt32LE())
                            val subName = flow.readString(subNameLength, encoding = TextCharsets.UTF_8)
                            val isDirectory = requireNotNull(flow.read(), notEnoughData) == 1

                            WadSubEntry(subName, isDirectory)
                        }

                        WadDirectoryEntry(name, subEntries)
                    }

                    val dataOffset = flow.position()

                    return WadArchive(SemanticVersion(majorVersion, minorVersion), header, files, directories, dataOffset, dataSource)
                }
            }
        }
    }

    operator fun get(name: String): WadFileEntry? = files.firstOrNull { entry -> entry.name == name }

    suspend fun openSource(file: WadFileEntry): DataSource<out InputFlow> = WindowedDataSource(dataSource, dataOffset + file.offset.toULong(), file.size.toULong(), closeParent = false)
    suspend fun openFlow(file: WadFileEntry): InputFlow? {
        val parent = dataSource.openInputFlow() ?: return null
        return WindowedInputFlow(parent, dataOffset + file.offset.toULong(), file.size.toULong())
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.WadArchive(dataSource: DataSource<*>) = WadArchive(this, dataSource)
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.UnsafeWadArchive(dataSource: DataSource<*>) = WadArchive.unsafe(this, dataSource)