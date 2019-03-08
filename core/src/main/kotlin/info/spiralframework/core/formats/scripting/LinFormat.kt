package info.spiralframework.core.formats.scripting

import info.spiralframework.core.formats.EnumFormatWriteResponse
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.game.hpa.HopesPeakDRGame
import info.spiralframework.formats.scripting.Lin
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.osl.data.OSLDrone
import info.spiralframework.osl.results.CustomLinOSL
import java.io.OutputStream

object LinFormat: ReadableSpiralFormat<Lin>, WritableSpiralFormat {
    override val name: String
        get() = "Lin"

    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<Lin> {
        if (game !is HopesPeakDRGame)
            return FormatResult.Fail(1.0)

        val lin = Lin(game, source) ?: return FormatResult.Fail(0.9)
        if (lin.entries.isEmpty())
            return FormatResult.Success(lin, 0.6)
        return FormatResult.Success(lin, 0.9)
    }

    override fun supportsWriting(data: Any): Boolean = data is OSLDrone

    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): EnumFormatWriteResponse {
        when (data) {
            is OSLDrone -> {
                val blueprint = data.compiled.entries.firstOrNull { (_, blueprint) -> blueprint is CustomLinOSL }?.value as? CustomLinOSL ?: return EnumFormatWriteResponse.FAIL
                val customLin = blueprint.produce()
                customLin.compile(stream)
                return EnumFormatWriteResponse.SUCCESS
            }
            else -> return EnumFormatWriteResponse.WRONG_FORMAT
        }
    }
}