package info.spiralframework.core.formats.scripting

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.osb.common.OpenSpiralBitcodeWrapper
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.PrintOutputFlow
import info.spiralframework.base.common.PrintOutputFlowWrapper

object OpenSpiralLanguageFormat : ReadableSpiralFormat<OpenSpiralBitcodeWrapper>, WritableSpiralFormat {

    override val name: String = "OpenSpiralLangauge"
    override val extension: String = "osl"

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<OpenSpiralBitcodeWrapper> =
        OpenSpiralBitcodeWrapper(context, source)
            .buildFormatResult(1.0)

    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = data is LinScript

    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        when (data) {
            is LinScript -> {
                LinTranspiler(data, writeContext?.game as? DrGame.LinScriptable ?: data.game)
                        .transpile(flow as? PrintOutputFlow ?: PrintOutputFlowWrapper(flow))

                return FormatWriteResponse.SUCCESS
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }
    }
}