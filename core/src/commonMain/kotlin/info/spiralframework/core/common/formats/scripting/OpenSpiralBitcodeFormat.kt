package info.spiralframework.core.common.formats.scripting

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.copyToOutputFlow
import dev.brella.kornea.io.common.useInputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.osb.common.OpenSpiralBitcodeWrapper

object OpenSpiralBitcodeFormat : ReadableSpiralFormat<OpenSpiralBitcodeWrapper>, WritableSpiralFormat {
    override val name: String = "OpenSpiralBitcode"
    override val extension: String = "osb"

    val REQUIRED_PROPERTIES = listOf(DrGame.LinScriptable)

    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<OpenSpiralBitcodeWrapper> =
        OpenSpiralBitcodeWrapper(context, source)
            .buildFormatResult(1.0)

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean = data is OpenSpiralBitcodeWrapper
    override fun requiredPropertiesForWrite(context: SpiralContext, writeContext: SpiralProperties?, data: Any): List<ISpiralProperty.PropertyKey<*>> = REQUIRED_PROPERTIES

    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse {
        when (data) {
            is OpenSpiralBitcodeWrapper -> {
                data.source.useInputFlow { it.copyToOutputFlow(flow) }

                return FormatWriteResponse.SUCCESS
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }
    }
}