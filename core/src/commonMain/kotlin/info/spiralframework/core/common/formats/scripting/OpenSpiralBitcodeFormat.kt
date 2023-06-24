package info.spiralframework.core.common.formats.scripting

import dev.brella.kornea.base.common.use
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.success
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.PrintOutputFlow
import dev.brella.kornea.io.common.flow.extensions.copyToOutputFlow
import dev.brella.kornea.io.common.useInputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.common.formats.*
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.osb.common.OpenSpiralBitcodeWrapper

public object OpenSpiralBitcodeFormat : ReadableSpiralFormat<OpenSpiralBitcodeWrapper>, WritableSpiralFormat<Unit> {
    override val name: String = "OpenSpiralBitcode"
    override val extension: String = "osb"

    public val REQUIRED_PROPERTIES: List<ISpiralProperty.PropertyKey<*>> = listOf(DrGame.LinScriptable)

    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<OpenSpiralBitcodeWrapper> =
        OpenSpiralBitcodeWrapper(context, source)
            .ensureFormatSuccess(1.0)

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is OpenSpiralBitcodeWrapper || data is LinScript

    override fun requiredPropertiesForWrite(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any
    ): List<ISpiralProperty.PropertyKey<*>> = REQUIRED_PROPERTIES

    override suspend fun write(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<Unit> {
        when (data) {
            is OpenSpiralBitcodeWrapper -> {
                data.source.useInputFlow { it.copyToOutputFlow(flow) }

                return KorneaResult.success()
            }
            is LinScript -> {
                val game = writeContext?.get(DrGame.LinScriptable)
                    ?: return context.localisedSpiralMissingProperty(DrGame.LinScriptable, "Missing lin game")

                flow.use { LinTranspiler(data, game).transpile(it) }

                return KorneaResult.success()
            }
            else -> return KorneaResult.spiralWrongFormat()
        }
    }
}