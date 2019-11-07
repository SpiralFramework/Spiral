package info.spiralframework.core.formats.audio

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.utils.DataSource
import java.io.File
import java.io.OutputStream
import java.util.*

open class SpiralAudioFormat(override val name: String, override val extension: String): ReadableSpiralFormat<File>, WritableSpiralFormat {
    open val needsMediaPlugin: Boolean = true

    override fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<Optional<File>> {
        try {
            return super.identify(context, readContext, source)
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
    override fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<File> {
        throw IllegalStateException(context.localise("core.formats.no_audio_impl.read", this))
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
    override fun supportsWriting(context: SpiralContext, data: Any): Boolean {
        throw IllegalStateException(context.localise("core.formats.no_audio_impl.support_write", this))
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
    override fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, stream: OutputStream): FormatWriteResponse {
        throw IllegalStateException(context.localise("core.formats.no_audio_impl.write", this))
    }
}