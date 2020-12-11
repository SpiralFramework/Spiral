package info.spiralframework.core.common.formats.scripting

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.PrintOutputFlow
import info.spiralframework.base.common.PrintOutputFlowWrapper
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.osb.common.OpenSpiralBitcodeWrapper

object OpenSpiralLanguageFormat : ReadableSpiralFormat<OpenSpiralBitcodeWrapper>, WritableSpiralFormat {
    override val name: String = "OpenSpiralLanguage"
    override val extension: String = "osl"

    val REQUIRED_PROPERTIES = listOf(DrGame.LinScriptable)

    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<OpenSpiralBitcodeWrapper> =
        OpenSpiralBitcodeWrapper(context, source)
            .buildFormatResult(1.0)

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean = data is LinScript
    override fun requiredPropertiesForWrite(context: SpiralContext, writeContext: SpiralProperties?, data: Any): List<ISpiralProperty.PropertyKey<*>> = REQUIRED_PROPERTIES

    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse {
        when (data) {
            is LinScript -> {
                LinTranspiler(
                    data,
                    writeContext[DrGame.LinScriptable]
                    ?: data.game?.takeUnless { it == DrGame.LinScriptable.Unknown }
                    ?: return FormatWriteResponse.MISSING_PROPERTY(DrGame.LinScriptable)
                ).transpile(flow as? PrintOutputFlow ?: PrintOutputFlowWrapper(flow))

                return FormatWriteResponse.SUCCESS
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }
    }
}