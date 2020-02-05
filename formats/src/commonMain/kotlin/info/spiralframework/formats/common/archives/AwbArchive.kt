package info.spiralframework.formats.common.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.WindowedInputFlow

@ExperimentalUnsignedTypes
class AwbArchive(val unknown1: Int, val files: Array<AwbFileEntry>, val dataSource: DataSource<*>) {
    companion object {
        /** 'AFS2' */
        val MAGIC_NUMBER_LE = 0x32534641

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): AwbArchive? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.pak.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): AwbArchive {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.awb.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val magic = requireNotNull(flow.readInt32LE(), notEnoughData)
                    require(magic == MAGIC_NUMBER_LE) { localise("formats.awb.invalid_magic", "0x${magic.toString(16)}", "0x${MAGIC_NUMBER_LE.toString(16)}") }

                    val unk1 = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val numEntries = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val align = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val awbFileIDs = IntArray(numEntries) { requireNotNull(flow.readInt16LE(), notEnoughData) }
                    val headerEnd = requireNotNull(flow.readUInt32LE(), notEnoughData)

                    val awbFileEnds = UIntArray(numEntries) { requireNotNull(flow.readUInt32LE(), notEnoughData) }

                    var start: UInt
                    var end: UInt = headerEnd

                    val entries = Array(numEntries) { index ->
                        start = end
                        start += (end alignmentNeededFor align).toUInt()
                        end = awbFileEnds[index]

                        AwbFileEntry(awbFileIDs[index], start, end - start)
                    }

                    return AwbArchive(unk1, entries, dataSource)
                }
            }
        }
    }

    suspend fun openSource(file: AwbFileEntry): DataSource<out InputFlow> = WindowedDataSource(dataSource, file.offset.toULong(), file.size.toULong(), closeParent = false)
    suspend fun openFlow(file: AwbFileEntry): InputFlow? {
        val parent = dataSource.openInputFlow() ?: return null
        return WindowedInputFlow(parent, file.offset.toULong(), file.size.toULong())
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.AwbArchive(dataSource: DataSource<*>) = AwbArchive(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeAwbArchive(dataSource: DataSource<*>) = AwbArchive.unsafe(this, dataSource)