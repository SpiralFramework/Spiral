package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.OffsetDataSource

@ExperimentalUnsignedTypes
class SrdArchive(val entries: Array<BaseSrdEntry>) {
    companion object {
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): SrdArchive? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.srd.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): SrdArchive {
            withFormats(context) {
                val entries: MutableList<BaseSrdEntry> = ArrayList()

                var pos: ULong = 0uL
                while (true) {
                    val offsetDataSource = OffsetDataSource(dataSource, pos, closeParent = false)
                    val entry = BaseSrdEntry.pseudoSafe(this, offsetDataSource) ?: break
                    pos += 16uL + entry.mainDataLength + entry.mainDataLength.alignmentNeededFor(0x10).toUInt() + entry.subDataLength + entry.subDataLength.alignmentNeededFor(0x10).toUInt()
                    entries.add(entry)
                }

                require(entries.isNotEmpty()) { localise("formats.srd.no_entries") }

                return SrdArchive(entries.toTypedArray())
            }
        }


    }
}