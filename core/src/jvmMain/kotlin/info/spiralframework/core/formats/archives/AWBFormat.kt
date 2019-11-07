package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.archives.AWB
import info.spiralframework.formats.utils.DataSource

object AWBFormat: ReadableSpiralFormat<AWB> {
    override val name: String = "AWB"
    override val extension: String = "awb"

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<AWB> {
        val awb = AWB(context, source) ?: return FormatResult.Fail(this, 1.0)

        if (awb.entries.size == 1)
            return FormatResult.Success(this, awb, 0.75)
        return FormatResult(this, awb, awb.entries.isNotEmpty(), 1.0) //Not positive on this one chief but we're going with it
    }
}