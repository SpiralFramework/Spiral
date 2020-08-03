package info.spiralframework.core.formats.scripting

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.osb.common.OpenSpiralBitcodeWrapper
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.getOrElseTransform
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow

object OpenSpiralLanguageFormat : ReadableSpiralFormat<OpenSpiralBitcodeWrapper>, WritableSpiralFormat {
    override val name: String = "OpenSpiralLangauge"
    override val extension: String = "osl"

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<OpenSpiralBitcodeWrapper> {
        try {
            return OpenSpiralBitcodeWrapper(context, source).map { osb -> FormatResult.Success(this, osb, 1.0) }
                .getOrElseTransform { FormatResult.Fail(this, 1.0, it) }
        } catch (iae: IllegalArgumentException) {
            return FormatResult.Fail(this, 1.0, KorneaResult.WithException.of(iae))
        }
    }

    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = data is LinScript

    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        when (data) {
            is LinScript -> {
                LinTranspiler(data, writeContext?.game as? DrGame.LinScriptable ?: data.game)
                        .transpile(flow)
                return FormatWriteResponse.SUCCESS
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }
    }
}