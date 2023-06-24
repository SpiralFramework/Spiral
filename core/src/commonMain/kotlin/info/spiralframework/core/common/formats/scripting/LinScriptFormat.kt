package info.spiralframework.core.common.formats.scripting

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.errorAsLocalisedIllegalArgument
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.common.formats.*
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.osb.common.LinCompiler
import info.spiralframework.osb.common.OpenSpiralBitcodeWrapper

public object LinScriptFormat : ReadableSpiralFormat<LinScript>, WritableSpiralFormat<LinCompiler> {
    override val name: String = "Lin"
    override val extension: String = "lin"
    public val REQUIRED_PROPERTIES: List<ISpiralProperty.PropertyKey<*>> = listOf(DrGame.LinScriptable)

    override fun preferredConversionFormat(
        context: SpiralContext,
        properties: SpiralProperties?
    ): WritableSpiralFormat<*> = OpenSpiralBitcodeFormat

    override suspend fun identify(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatOptionalResult<LinScript> {
        //Check here if we have an explicit game override that says this *isn't* a game from HPA.
        //ie: V3
        val game = readContext[DrGame]
        if (game != null && game !is DrGame.LinScriptable)
            return context.errorAsLocalisedIllegalArgument(-1, "core.formats.lin.invalid_game_provided", game)

        //We're able to use an UnknownHopesPeakGame here because I'm pretty sure the lin format is... 'universal'
        //The format itself doesn't change from game to game - only the op codes.
        //However, we *can* say that all games will follow the format of 0x70 [op code] {parameters}
        //For the purposes of this, we can ignore flag check fuckery
        return LinScript(context, (game as? DrGame.LinScriptable) ?: DrGame.LinScriptable.Unknown, source)
            .map { Optional.of(it) }
            .ensureOptionalFormatSuccess { lin -> if (lin.scriptData.isEmpty()) 0.45 else 0.85 }
    }

    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<LinScript> {
        val game = readContext?.get(DrGame).let { game ->
            if (game !is DrGame.LinScriptable) return context.errorAsLocalisedIllegalArgument(
                -1,
                "core.formats.lin.invalid_game_provided", game ?: "(no game provided)"
            )

            game
        }

        return LinScript(context, game, source)
            .ensureFormatSuccess { lin -> if (lin.scriptData.isEmpty()) 0.6 else 0.9 }
    }

    override fun requiredPropertiesForWrite(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any
    ): List<ISpiralProperty.PropertyKey<*>> = REQUIRED_PROPERTIES

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is OpenSpiralBitcodeWrapper

    override suspend fun write(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<LinCompiler> {
        when (data) {
            is OpenSpiralBitcodeWrapper -> {
                val game = writeContext[DrGame.LinScriptable]
                    ?: return context.localisedSpiralMissingProperty(DrGame.LinScriptable, "Missing game")

                return data.parseBitcode(context, LinCompiler(flow, game))
            }
            else -> return KorneaResult.spiralWrongFormat()
        }
    }
}