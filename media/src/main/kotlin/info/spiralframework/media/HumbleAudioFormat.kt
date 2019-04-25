package info.spiralframework.media

import info.spiralframework.base.util.copyToStream
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.FormatWriteResponse
import info.spiralframework.core.formats.audio.SpiralAudioFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.utils.use
import io.humble.video.Demuxer
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

open class HumbleAudioFormat(val format: String): SpiralAudioFormat(format, format) {
    override val needsMediaPlugin: Boolean = false

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
        val tmp = DataHandler.createTmpFile(UUID.randomUUID().toString())
        source.use { stream -> FileOutputStream(tmp).use(stream::copyToStream) }

        val demuxer = Demuxer.make()

        try {
            try {
                demuxer.open(tmp.absolutePath, null, false, true, null, null)
            } catch (runtime: RuntimeException) {
                tmp.delete()

                return FormatResult.Fail(this, 1.0)
            }
            if (demuxer.format.name.equals(format, true) || demuxer.format.longName.equals(format, true))
                return FormatResult.Success(this, tmp, 1.0)

            tmp.delete()

            return FormatResult.Fail(this, 1.0)
        } finally {
            demuxer.close()
        }
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
    override fun supportsWriting(data: Any): Boolean = data is File || data is ByteArray || data is InputStream

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
        when (data) {
            is File -> {
                val demuxer = Demuxer.make()

                try {
                    demuxer.open(data.absolutePath, null, false, true, null, null)
                } finally {
                    demuxer.close()
                }
            }
            is ByteArray -> {}
            is InputStream -> {}
            else -> return FormatWriteResponse.WRONG_FORMAT
        }

        return FormatWriteResponse.SUCCESS
    }
}