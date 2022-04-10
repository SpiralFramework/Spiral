package info.spiralframework.core.common.formats.archives

import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.SpiralFormatReturnResult
import info.spiralframework.formats.common.archives.AwbArchive

public object AwbArchiveFormat : ReadableSpiralFormat<AwbArchive> {
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
    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): SpiralFormatReturnResult<AwbArchive> =
        AwbArchive(context, source)
            .filter { awb -> awb.files.isNotEmpty() }
            .ensureFormatSuccess { awb -> if (awb.files.size == 1) 0.75 else 1.0 }
}