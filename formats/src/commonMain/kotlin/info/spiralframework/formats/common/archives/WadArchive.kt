package info.spiralframework.formats.common.archives

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.TextCharsets
import dev.brella.kornea.io.common.WindowedDataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readInt64LE
import dev.brella.kornea.io.common.flow.extensions.readString
import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map

public class WadArchive(
    public val version: SemanticVersion,
    public val header: ByteArray,
    public val files: Array<WadFileEntry>,
    public val directories: Array<WadDirectoryEntry>,
    public val dataOffset: ULong,
    public val dataSource: DataSource<*>
) : SpiralArchive {
    public companion object {
        /** 'AGAR' */
        public const val MAGIC_NUMBER_LE: Int = 0x52414741

        /** 'AGAR' */
        public const val MAGIC_NUMBER_BE: Int = 0x41474152

        public const val INVALID_MAGIC_NUMBER: Int = 0x0000
        public const val HEADER_INCOMPLETE: Int = 0x0001

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.wad.not_enough_data"
        public const val INVALID_MAGIC_NUMBER_KEY: String = "formats.wad.invalid_magic_number"
        public const val HEADER_INCOMPLETE_KEY: String = "formats.wad.header_incomplete"

        public suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<WadArchive> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_MAGIC_NUMBER,
                            localise(
                                INVALID_MAGIC_NUMBER_KEY,
                                "0x${magic.toString(16)}",
                                "0x${MAGIC_NUMBER_LE.toString(16)}"
                            )
                        )
                    }

                    val majorVersion =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorVersion =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val headerSize = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val header = ByteArray(headerSize)
                    val headerBytesRead = flow.read(header)
                    if (headerBytesRead != headerSize) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            HEADER_INCOMPLETE, localise(
                                HEADER_INCOMPLETE_KEY, headerSize, headerBytesRead
                                    ?: constNull()
                            )
                        )
                    }

                    val fileCount = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val files = Array(fileCount) {
                        val nameLength =
                            flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val name = flow.readString(nameLength, encoding = TextCharsets.UTF_8)
                            ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val fileSize =
                            flow.readInt64LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val fileOffset =
                            flow.readInt64LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                        WadFileEntry(name, fileSize, fileOffset)
                    }

                    val directoryCount =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val directories = Array(directoryCount) {
                        val nameLength =
                            flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val name = flow.readString(nameLength, encoding = TextCharsets.UTF_8)
                            ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val subEntryCount =
                            flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                        val subEntries = Array(subEntryCount) {
                            val subNameLength =
                                flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val subName = flow.readString(subNameLength, encoding = TextCharsets.UTF_8)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val isDirectory = flow.read()?.equals(1)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                            WadSubEntry(subName, isDirectory)
                        }

                        WadDirectoryEntry(name, subEntries)
                    }

                    val dataOffset = flow.position()

                    return@closeAfter KorneaResult.success(
                        WadArchive(
                            SemanticVersion(majorVersion, minorVersion),
                            header,
                            files,
                            directories,
                            dataOffset,
                            dataSource
                        )
                    )
                }
            }
    }

    override val fileCount: Int
        get() = files.size

    public operator fun get(name: String): WadFileEntry? = files.firstOrNull { entry -> entry.name == name }

    public fun openSource(file: WadFileEntry): DataSource<InputFlow> =
        WindowedDataSource(dataSource, dataOffset + file.offset.toULong(), file.size.toULong(), closeParent = false)

    public suspend fun openFlow(file: WadFileEntry): KorneaResult<InputFlow> =
        dataSource.openInputFlow()
            .map { parent -> WindowedInputFlow(parent, dataOffset + file.offset.toULong(), file.size.toULong()) }

    override suspend fun SpiralContext.getSubfiles(): Flow<SpiralArchiveSubfile<*>> =
        files.asFlow().map { file -> SpiralArchiveSubfile(file.name, openSource(file)) }
}

@Suppress("FunctionName")
public suspend fun SpiralContext.WadArchive(dataSource: DataSource<*>): KorneaResult<WadArchive> = WadArchive(this, dataSource)
@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeWadArchive(dataSource: DataSource<*>): WadArchive = WadArchive(this, dataSource).getOrThrow()