package info.spiralframework.core.formats.video

import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.video.SFL

object SFLFormat: ReadableSpiralFormat<SFL> {
    /** A **RECOGNISABLE** name, not necessarily the full name. May commonly be the extension */
    override val name: String = "SFL"

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
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<SFL> {
        val sfl = SFL(source) ?: return FormatResult.Fail(1.0)
        return FormatResult.Success(sfl, 1.0)
    }
}