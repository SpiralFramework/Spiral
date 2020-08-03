package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.archives.srd.SrdArchive
import dev.brella.kornea.errors.common.getOrElse
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.DataSource
import java.util.*

object SrdArchiveFormat: ReadableSpiralFormat<SrdArchive> {
    override val name: String = "SRD"
    override val extension: String = "srd"

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<SrdArchive>> {
        val fileName = readContext?.name?.substringAfterLast('/')
        if (fileName != null && fileName.contains('.') && !fileName.substringAfterLast('.').equals(extension, true)) {
            return FormatResult.Fail(0.9)
        }

        return super.identify(context, readContext, source)
    }

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<SrdArchive> =
            SrdArchive(context, source)
                    .map { srd ->
                        //TODO: Bump up the 'chance' for these results after proper fail states are used
                        if (srd.entries.size == 1) FormatResult.Success(this, srd, 0.4)
                        else FormatResult(this, srd, srd.entries.isNotEmpty(), 0.5)
                    }.getOrElse(FormatResult.Fail(this, 1.0))
}