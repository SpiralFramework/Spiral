package info.spiralframework.core.formats.data

import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.data.DataTableV3
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource

object DataTableV3Format: ReadableSpiralFormat<DataTableV3> {
    /** A **RECOGNISABLE** name, not necessarily the full name. May commonly be the extension */
    override val name: String = "Data Table V3"
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
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<DataTableV3> {
        val dataTable = DataTableV3(source) ?: return FormatResult.Fail(this, 1.0)

        return FormatResult.Success(this, dataTable, 0.7)
    }
}