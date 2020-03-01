package info.spiralframework.core.formats.scripting

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.osb.common.LinCompiler
import info.spiralframework.osb.common.OpenSpiralBitcodeWrapper
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.OutputFlow
import java.io.OutputStream
import java.util.*

object LinScriptFormat : ReadableSpiralFormat<LinScript>, WritableSpiralFormat {
    override val name: String = "Lin"
    override val extension: String = "lin"

    override fun preferredConversionFormat(): WritableSpiralFormat? = OpenSpiralLanguageFormat

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<LinScript>> {
        //Check here if we have an explicit game override that says this *isn't* a game from HPA.
        //ie: V3
        if (readContext?.game != null && readContext.game !is DrGame.LinScriptable)
            return FormatResult.Fail(this, 1.0, IllegalArgumentException(context.localise("core.formats.lin.invalid_game_provided", readContext.game ?: "(no game provided)")))

        //We're able to use an UnknownHopesPeakGame here because I'm pretty sure the lin format is... 'universal'
        //The format itself doesn't change from game to game - only the op codes.
        //However, we *can* say that all games will follow the format of 0x70 [op code] {parameters}
        //For the purposes of this, we can ignore flag check fuckery
        val lin = LinScript(context, (readContext?.game as? DrGame.LinScriptable) ?: DrGame.LinScriptable.Unknown, source)
                ?: return FormatResult.Fail(this, 0.9)
        if (lin.scriptData.isEmpty())
            return FormatResult.Success(this, Optional.of(lin), 0.55)
        return FormatResult.Success(this, Optional.of(lin), 0.85)
    }

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<LinScript> {
        val game = readContext?.game as? DrGame.LinScriptable
                ?: return FormatResult.Fail(this, 1.0, IllegalArgumentException(context.localise("core.formats.lin.invalid_game_provided", readContext?.game ?: "(no game provided)")))

        val lin = LinScript(context, game, source) ?: return FormatResult.Fail(this, 0.9)
        if (lin.scriptData.isEmpty())
            return FormatResult.Success(this, lin, 0.6)
        return FormatResult.Success(this, lin, 0.9)
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