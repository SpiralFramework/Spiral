package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.archives.AwbArchive
import dev.brella.kornea.errors.common.getOrElse
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.DataSource
import java.util.*

object AwbArchiveFormat : ReadableSpiralFormat<AwbArchive> {
    override val name: String = "AWB"
    override val extension: String = "awb"

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<AwbArchive> =
            AwbArchive(context, source)
                    .map { awb ->
                        if (awb.files.size == 1) FormatResult.Success(this, awb, 0.75)
                        else FormatResult(this, awb, awb.files.isNotEmpty(), 1.0)
                    }
                    .getOrElse(FormatResult.Fail(this, 1.0))
}