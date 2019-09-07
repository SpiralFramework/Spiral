package info.spiralframework.core.formats.scripting

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.errors.HopesPeakMissingGameException
import info.spiralframework.formats.game.hpa.HopesPeakDRGame
import info.spiralframework.formats.game.hpa.UnknownHopesPeakGame
import info.spiralframework.formats.scripting.Lin
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.osl.data.OSLDrone
import info.spiralframework.osl.results.CustomLinOSL
import java.io.OutputStream
import java.util.*

object LinFormat : ReadableSpiralFormat<Lin>, WritableSpiralFormat {
    override val name: String = "Lin"
    override val extension: String = "lin"

    override fun preferredConversionFormat(): WritableSpiralFormat? = OpenSpiralLanguageFormat

    override fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<Optional<Lin>> {
        //Check here if we have an explicit game override that says this *isn't* a game from HPA.
        //ie: V3
        if (readContext?.game != null && readContext.game !is HopesPeakDRGame)
            return FormatResult.Fail(this, 1.0, HopesPeakMissingGameException(context, readContext.game))

        //We're able to use an UnknownHopesPeakGame here because I'm pretty sure the lin format is... 'universal'
        //The format itself doesn't change from game to game - only the op codes.
        //However, we *can* say that all games will follow the format of 0x70 [op code] {parameters}
        //For the purposes of this, we can ignore flag check fuckery
        val lin = Lin(context, (readContext?.game as? HopesPeakDRGame) ?: UnknownHopesPeakGame, source)
                ?: return FormatResult.Fail(this, 0.9)
        if (lin.entries.isEmpty())
            return FormatResult.Success(this, Optional.of(lin), 0.55)
        return FormatResult.Success(this, Optional.of(lin), 0.85)
    }

    override fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<Lin> {
        val game = readContext?.game as? HopesPeakDRGame
                ?: return FormatResult.Fail(this, 1.0, HopesPeakMissingGameException(context, readContext?.game))

        val lin = Lin(context, game, source) ?: return FormatResult.Fail(this, 0.9)
        if (lin.entries.isEmpty())
            return FormatResult.Success(this, lin, 0.6)
        return FormatResult.Success(this, lin, 0.9)
    }

    override fun supportsWriting(context: SpiralContext, data: Any): Boolean = data is OSLDrone

    override fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, stream: OutputStream): FormatWriteResponse {
        with(context) {
            when (data) {
                is OSLDrone -> {
                    val blueprint = data.compiled.entries.firstOrNull { (_, blueprint) -> blueprint is CustomLinOSL }?.value as? CustomLinOSL
                            ?: return FormatWriteResponse.FAIL(IllegalStateException(localise("core.formats.lin.osl_drone_has_no_lin")))
                    val customLin = blueprint.produce()
                    customLin.compile(stream)
                    return FormatWriteResponse.SUCCESS
                }
                else -> return FormatWriteResponse.WRONG_FORMAT
            }
        }
    }
}