package info.spiralframework.core.formats.archives

import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.archives.AWB
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource

object AWBFormat: ReadableSpiralFormat<AWB> {
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
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<AWB> {
        val awb = AWB(source) ?: return FormatResult.Fail(1.0)

        if (awb.entries.size == 1)
            return FormatResult.Success(awb, 0.75)
        return FormatResult(awb, awb.entries.isNotEmpty(), 1.0) //Not positive on this one chief but we're going with it
    }
}