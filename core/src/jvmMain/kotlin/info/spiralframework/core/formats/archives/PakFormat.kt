package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.archives.CustomPak
import info.spiralframework.formats.archives.IArchive
import info.spiralframework.formats.archives.Pak
import info.spiralframework.formats.utils.DataSource
import java.io.OutputStream
import java.util.zip.ZipFile

object PakFormat: ReadableSpiralFormat<Pak>, WritableSpiralFormat {
    override val name: String = "Pak"
    override val extension: String = "pak"

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
    override fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<Pak> {
        val pak = Pak(context = context, dataSource = source) ?: return FormatResult.Fail(this, 1.0)

        if (pak.files.size == 1)
            return FormatResult.Success(this, pak,0.75)

        return FormatResult(this, pak, pak.files.isNotEmpty(), 0.8) //Not positive on this one chief but we're going with it
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
        val customPak = CustomPak()

        when (data) {
            is IArchive -> customPak.add(data)
            is ZipFile -> data.entries().iterator().forEach { entry ->
                customPak.add(entry.name, entry.size) { data.getInputStream(entry) }
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }

        customPak.compile(stream)
        return FormatWriteResponse.SUCCESS
    }
}