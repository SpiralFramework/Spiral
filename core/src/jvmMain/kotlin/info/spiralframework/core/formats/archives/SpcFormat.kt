package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.archives.CustomSPC
import info.spiralframework.formats.archives.IArchive
import info.spiralframework.formats.archives.SPC
import info.spiralframework.formats.utils.DataSource
import java.io.OutputStream
import java.util.zip.ZipFile

object SpcFormat: ReadableSpiralFormat<SPC>, WritableSpiralFormat {
    override val name: String = "Spc"
    override val extension: String = "spc"

    override fun preferredConversionFormat(): WritableSpiralFormat? = ZipFormat

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param dataContext Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<SPC> {
        val spc = SPC(context, source) ?: return FormatResult.Fail(this, 1.0)

        if (spc.files.size == 1)
            return FormatResult.Success(this, spc,0.75)

        return FormatResult(this, spc, spc.files.isNotEmpty(), 1.0) //Not positive on this one chief but we're going with it
    }

    /**
     * Does this format support writing [data]?
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     *
     * @return If we are able to write [data] as this format
     */
    override fun supportsWriting(context: SpiralContext, data: Any): Boolean = data is IArchive || data is ZipFile

    /**
     * Writes [data] to [stream] in this format
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param dataContext Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    override fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, stream: OutputStream): FormatWriteResponse {
        val customSpc = CustomSPC()
        when (data) {
            is IArchive -> customSpc.add(data)
            is ZipFile -> data.entries().iterator().forEach { entry ->
                customSpc.add(entry.name, entry.size) { data.getInputStream(entry) }
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }

        customSpc.compile(stream)
        return FormatWriteResponse.SUCCESS
    }
}