package info.spiralframework.core.formats.archives

import info.spiralframework.core.formats.EnumFormatWriteResponse
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.archives.*
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import java.io.OutputStream
import java.util.zip.ZipFile

object PakFormat: ReadableSpiralFormat<Pak>, WritableSpiralFormat {
    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<Pak> {
        val pak = Pak(source) ?: return FormatResult.Fail(1.0)

        if (pak.files.size == 1)
            return FormatResult.Success(pak,0.75)

        return FormatResult(pak, pak.files.isNotEmpty(), 1.0) //Not positive on this one chief but we're going with it
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
    override fun supportsWriting(data: Any): Boolean = data is WAD || data is CPK || data is SPC || data is Pak || data is ZipFile

    /**
     * Writes [data] to [stream] in this format
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): EnumFormatWriteResponse {
        val customPak = CustomPak()

        when (data) {
            is WAD -> data.files.forEach { entry -> customPak.add(entry.name, entry.size, entry::inputStream) }
            is CPK -> data.files.forEach { entry -> customPak.add(entry.name, entry.extractSize, entry::inputStream) }
            is SPC -> data.files.forEach { entry -> customPak.add(entry.name, entry.decompressedSize, entry::inputStream) }
            is Pak -> customPak.add(data)
            is ZipFile -> data.entries().iterator().forEach { entry ->
                customPak.add(entry.name, entry.size) { data.getInputStream(entry) }
            }
            else -> return EnumFormatWriteResponse.WRONG_FORMAT
        }

        customPak.compile(stream)
        return EnumFormatWriteResponse.SUCCESS
    }
}