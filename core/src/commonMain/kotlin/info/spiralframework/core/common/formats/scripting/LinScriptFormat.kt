package info.spiralframework.core.common.formats.scripting

import dev.brella.kornea.errors.common.*
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.osb.common.LinCompiler
import info.spiralframework.osb.common.OpenSpiralBitcodeWrapper
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.core.common.formats.FormatReadContext
import info.spiralframework.core.common.formats.FormatWriteContext
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat

object LinScriptFormat : ReadableSpiralFormat<LinScript>, WritableSpiralFormat {
    override val name: String = "Lin"
    override val extension: String = "lin"

    override fun preferredConversionFormat(): WritableSpiralFormat? = OpenSpiralLanguageFormat

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<Optional<LinScript>> {
        //Check here if we have an explicit game override that says this *isn't* a game from HPA.
        //ie: V3
        if (readContext?.game != null && readContext.game !is DrGame.LinScriptable)
            return KorneaResult.errorAsIllegalArgument(-1, context.localise("core.formats.lin.invalid_game_provided", readContext.game ?: "(no game provided)"))

        //We're able to use an UnknownHopesPeakGame here because I'm pretty sure the lin format is... 'universal'
        //The format itself doesn't change from game to game - only the op codes.
        //However, we *can* say that all games will follow the format of 0x70 [op code] {parameters}
        //For the purposes of this, we can ignore flag check fuckery
        return LinScript(context, (readContext?.game as? DrGame.LinScriptable) ?: DrGame.LinScriptable.Unknown, source)
            .map { Optional<LinScript>(it) }
            .buildFormatResult { it.map { lin -> if (lin.scriptData.isEmpty()) 0.45 else 0.85 }.getOrElse(0.0) }
    }

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<LinScript> {
        val game = readContext?.game as? DrGame.LinScriptable
                   ?: return KorneaResult.errorAsIllegalArgument(-1, context.localise("core.formats.lin.invalid_game_provided", readContext?.game ?: "(no game provided)"))

        return LinScript(context, game, source)
            .buildFormatResult { lin -> if (lin.scriptData.isEmpty()) 0.6 else 0.9 }
    }

    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = writeContext?.game is DrGame.LinScriptable && (data is OpenSpiralBitcodeWrapper)

    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        when (data) {
            is OpenSpiralBitcodeWrapper -> {
                data.parseBitcode(context, LinCompiler(flow, writeContext?.game as DrGame.LinScriptable))
                return FormatWriteResponse.SUCCESS
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }
    }
}