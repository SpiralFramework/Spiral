package info.spiralframework.formats.common.archives

import com.soywiz.krypto.sha256
import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.io.readString
import info.spiralframework.base.common.toHexString
import info.spiralframework.formats.common.compression.decompressSpcData
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.WindowedInputFlow
import org.abimon.kornea.io.common.flow.readBytes

@ExperimentalUnsignedTypes
class SpcArchive(val unknownFlag: Int, val files: Array<SpcFileEntry>, val dataSource: DataSource<*>) {
    companion object {
        /** .SPC */
        const val SPC_MAGIC_NUMBER_LE = 0x2E535043
        /** CPS. */
        const val SPC_MAGIC_NUMBER_BE = 0x4350532E

        /** Root */
        const val TABLE_MAGIC_NUMBER_LE = 0x746f6f52
        const val TABLE_MAGIC_NUMBER_BE = 0x526f6f74

        const val COMPRESSED_FLAG = 0x02

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): SpcArchive? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.spc.invalid", dataSource, iae) }

                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): SpcArchive {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.spc.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow()) { "Couldn't open file?" }

                use(flow) {
                    val magic = requireNotNull(flow.readInt32LE(), notEnoughData)
                    require(magic == SPC_MAGIC_NUMBER_LE) { localise("formats.spc.invalid_magic", "0x${magic.toString(16)}", "0x${SPC_MAGIC_NUMBER_LE.toString(16)}") }

                    flow.skip(0x24u)

                    val fileCount = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val unknown = requireNotNull(flow.readInt32LE(), notEnoughData)
                    flow.skip(0x10u)

                    val tableMagic = requireNotNull(flow.readInt32LE(), notEnoughData)
                    require(tableMagic == TABLE_MAGIC_NUMBER_LE) { localise("formats.spc.invalid_table_magic", "0x${tableMagic.toString(16)}", "0x${TABLE_MAGIC_NUMBER_LE.toString(16)}") }
                    flow.skip(0x0Cu)

                    val files = Array(fileCount) {
                        val compressionFlag = requireNotNull(flow.readInt16LE(), notEnoughData)
                        val unknownFlag = requireNotNull(flow.readInt16LE(), notEnoughData)

                        val compressedSize = requireNotNull(flow.readUInt32LE(), notEnoughData).toLong()
                        val decompressedSize = requireNotNull(flow.readUInt32LE(), notEnoughData).toLong()
                        val nameLength = requireNotNull(flow.readInt32LE(), notEnoughData)

                        //require(compressionFlag in COMPRESSION_FLAG_ARRAY)

                        flow.skip(0x10u)

                        val namePadding = (nameLength + 1) alignmentNeededFor 0x10
                        val dataPadding = compressedSize alignmentNeededFor 0x10

                        val name = requireNotNull(flow.readString(nameLength, encoding = TextCharsets.UTF_8), notEnoughData)
                        flow.skip(namePadding.toULong() + 1u)

                        val position = flow.position()

                        flow.skip((compressedSize + dataPadding).toULong())

                        SpcFileEntry(name, compressionFlag, unknownFlag, compressedSize, decompressedSize, position)
                    }

                    return SpcArchive(unknown, files, dataSource)
                }
            }
        }
    }

    operator fun get(name: String): SpcFileEntry? = files.firstOrNull { entry -> entry.name == name }

    suspend fun SpiralContext.openRawSource(file: SpcFileEntry): DataSource<out InputFlow> = WindowedDataSource(dataSource, file.offset, file.compressedSize.toULong(), closeParent = false)
    suspend fun SpiralContext.openRawFlow(file: SpcFileEntry): InputFlow? {
        val parent = dataSource.openInputFlow() ?: return null
        return WindowedInputFlow(parent, file.offset, file.compressedSize.toULong())
    }

    suspend fun SpiralContext.openDecompressedSource(file: SpcFileEntry): DataSource<out InputFlow>? {
        if (file.compressionFlag == COMPRESSED_FLAG) {
            val flow = openRawFlow(file) ?: return null
            val compressedData = use(flow) { flow.readBytes() }
            val cache = cacheShortTerm(compressedData.sha256().toHexString())
            val output = cache.openOutputFlow()
            if (output == null) {
                //Cache has failed; store in memory
                cache.close()
                return BinaryDataSource(decompressSpcData(compressedData, file.decompressedSize.toInt()))
            }

            output.write(decompressSpcData(compressedData, file.decompressedSize.toInt()))
            return cache
        } else {
            return openRawSource(file)
        }
    }

    suspend fun SpiralContext.openDecompressedFlow(file: SpcFileEntry): InputFlow? {
        if (file.compressionFlag == COMPRESSED_FLAG) {
            val source = openDecompressedSource(file) ?: return null
            val input = source.openInputFlow()
            if (input == null) {
                source.close()
                return null
            }

            input.addCloseHandler { source.close() }
            return input
        } else {
            return openRawFlow(file)
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun SpcArchive.openRawSource(context: SpiralContext, file: SpcFileEntry) = context.openRawSource(file)

@ExperimentalUnsignedTypes
suspend fun SpcArchive.openRawFlow(context: SpiralContext, file: SpcFileEntry) = context.openRawFlow(file)

@ExperimentalUnsignedTypes
suspend fun SpcArchive.openDecompressedSource(context: SpiralContext, file: SpcFileEntry) = context.openDecompressedSource(file)

@ExperimentalUnsignedTypes
suspend fun SpcArchive.openDecompressedFlow(context: SpiralContext, file: SpcFileEntry) = context.openDecompressedFlow(file)

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.SpcArchive(dataSource: DataSource<*>) = SpcArchive(this, dataSource)
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.UnsafeSpcArchive(dataSource: DataSource<*>) = SpcArchive.unsafe(this, dataSource)