package info.spiralframework.core.formats.scripting

import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.errors.HopesPeakMissingGameException
import info.spiralframework.formats.errors.V3MissingGameException
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.scripting.WordScriptFile
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import java.util.*

object WordScriptFormat: ReadableSpiralFormat<WordScriptFile> {
    /** A **RECOGNISABLE** name, not necessarily the full name. May commonly be the extension */
    override val name: String = "Word Script"
    /**
     * The usual extension for this format. Some formats don't have a proper extension, so this can be nullable
     */
    override val extension: String = "wrd"

    override fun identify(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<Optional<WordScriptFile>> {
        //Check here if we have an explicit game override that says this *isn't* a V3 game.
        if (game != null && game !is V3)
            return FormatResult.Fail(this, 1.0, HopesPeakMissingGameException(game))

        val wrd = WordScriptFile(V3, source) ?: return FormatResult.Fail(this, 0.9)
        if (wrd.entries.isEmpty())
            return FormatResult.Success(this, Optional.of(wrd), 0.55)
        return FormatResult.Success(this, Optional.of(wrd), 0.85)
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
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<WordScriptFile> {
        if (game !is V3)
            return FormatResult.Fail(this, 1.0, V3MissingGameException(game))

        val wrd = WordScriptFile(game, source) ?: return FormatResult.Fail(this, 0.9)
        if (wrd.entries.isEmpty())
            return FormatResult.Success(this, wrd, 0.6)
        return FormatResult.Success(this, wrd, 0.9)
    }
}