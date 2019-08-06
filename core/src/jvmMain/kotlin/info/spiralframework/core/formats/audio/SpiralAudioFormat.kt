package info.spiralframework.core.formats.audio

import info.spiralframework.base.util.locale
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.FormatWriteResponse
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import java.io.File
import java.io.OutputStream
import java.util.*

open class SpiralAudioFormat(override val name: String, override val extension: String): ReadableSpiralFormat<File>, WritableSpiralFormat {
    open val needsMediaPlugin: Boolean = true

    override fun identify(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<Optional<File>> {
        try {
            return super.identify(name, game, context, source)
        } catch (ise: IllegalStateException) {
            return FormatResult.Fail(this, 1.0, ise)
        }
    }

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
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<File> {
        throw locale<IllegalStateException>("core.formats.no_audio_impl.read", this)
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
        throw locale<IllegalStateException>("core.formats.no_audio_impl.support_write", this)
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
    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): FormatWriteResponse {
        throw locale<IllegalStateException>("core.formats.no_audio_impl.write", this)
    }
}