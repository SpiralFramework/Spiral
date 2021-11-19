package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KORNEA_ERROR_NOT_ENOUGH_DATA
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.OffsetDataSource
import info.spiralframework.formats.common.archives.SpiralArchive
import info.spiralframework.formats.common.archives.SpiralArchiveSubfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.withIndex

@ExperimentalUnsignedTypes
class SrdArchive(val entries: Array<BaseSrdEntry>, val dataSource: DataSource<*>): SpiralArchive {
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

                return KorneaResult.success(SrdArchive(entries.toTypedArray(), dataSource))
            }
        }


    }

    override val fileCount: Int
        get() = entries.sumBy { entry -> (if (entry.mainDataLength > 0uL) 1 else 0) + (if (entry.subDataLength > 0uL) 1 else 0) }

    override suspend fun SpiralContext.getSubfiles(): Flow<SpiralArchiveSubfile<*>> =
        entries.asFlow().withIndex().transform { (index, entry) ->
            if (entry.mainDataLength > 0uL) {
                emit(SpiralArchiveSubfile("${index}_${entry.classifierAsString}_data.dat", entry.openMainDataSource(dataSource)))
            }

            if (entry.subDataLength > 0uL) {
                emit(SpiralArchiveSubfile("${index}_${entry.classifierAsString}_sub-data.dat", entry.openSubDataSource(dataSource)))
            }
        }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.SrdArchive(dataSource: DataSource<*>) = SrdArchive(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeSrdArchive(dataSource: DataSource<*>) = SrdArchive(this, dataSource).get()