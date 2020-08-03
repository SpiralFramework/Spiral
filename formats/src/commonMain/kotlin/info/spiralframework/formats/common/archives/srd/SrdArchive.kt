package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KORNEA_ERROR_NOT_ENOUGH_DATA
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.OffsetDataSource

@ExperimentalUnsignedTypes
class SrdArchive(val entries: Array<BaseSrdEntry>) {
    companion object {
        const val NO_ENTRIES = 0x0001
        const val INVALID_ENTRY = 0x0000

        const val NO_ENTRIES_KEY = "formats.srd.no_entries"
        const val INVALID_ENTRY_KEY = "formats.srd.invalid_entry"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<SrdArchive> {
            withFormats(context) {
                val entries: MutableList<BaseSrdEntry> = ArrayList()

                var pos: ULong = 0uL
                run loop@{
                    while (true) {
                        val offsetDataSource = OffsetDataSource(dataSource, pos, closeParent = false)
                        val entry = BaseSrdEntry(this, offsetDataSource)
                                .getOrBreak { error ->
                                    if (error is KorneaResult.WithErrorCode && error.errorCode == KORNEA_ERROR_NOT_ENOUGH_DATA) return@loop

                                    return KorneaResult.errorAsIllegalArgument(INVALID_ENTRY, localise(INVALID_ENTRY_KEY), error)
                                }

                        pos += 16uL + entry.mainDataLength + entry.mainDataLength.alignmentNeededFor(0x10).toUInt() + entry.subDataLength + entry.subDataLength.alignmentNeededFor(0x10).toUInt()
                        entries.add(entry)
                    }
                }

                if(entries.isEmpty()) {
                    return KorneaResult.errorAsIllegalArgument(NO_ENTRIES, localise(NO_ENTRIES_KEY))
                }

                return KorneaResult.success(SrdArchive(entries.toTypedArray()))
            }
        }


    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.SrdArchive(dataSource: DataSource<*>) = SrdArchive(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeSrdArchive(dataSource: DataSource<*>) = SrdArchive(this, dataSource).get()