package info.spiralframework.core.formats.archives

import info.spiralframework.core.DataContext
import info.spiralframework.core.DataSource
import info.spiralframework.core.FormatChance
import info.spiralframework.core.formats.EnumFormatWriteResponse
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.spiral.core.objects.game.DRGame
import java.io.OutputStream

object CpkFormat: ReadableSpiralFormat<CPK>, WritableSpiralFormat {
    /**
     * Check if [source] matches this format.
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a pair for if
     */
    override fun isFormat(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatChance {
        val cpk = CPK(source) ?: return FormatChance(false, 1.0)
    }

    /**
     * Reads the data source as an object of type [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return an object of type [T], or null if the stream isn't a valid instance of this format
     */
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): CPK? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
    override fun supportsWriting(data: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}