package info.spiralframework.core.common.formats.data

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.formats.common.data.DataTableStructure

object DataTableStructureFormat : ReadableSpiralFormat<DataTableStructure> {
    /** A **RECOGNISABLE** name, not necessarily the full name. May commonly be the extension */
    override val name: String = "Data Table Structure"

    /**
     * The usual extension for this format. Some formats don't have a proper extension, so this can be nullable
     */
    override val extension: String = "dat"

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
    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<DataTableStructure> =
        DataTableStructure(context, source)
            .buildFormatResult(0.7)
}