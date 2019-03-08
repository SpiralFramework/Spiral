package info.spiralframework.core.formats.compression

import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.compression.ICompression
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import java.io.Closeable
import java.io.File
import java.util.*

interface CompressionFormat<T: ICompression>: ReadableSpiralFormat<DataSource> {
    val compressionFormat: T

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
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<DataSource> {
        if (compressionFormat.isCompressed(source)) {
            val tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".dat")
            tmpFile.deleteOnExit()

            tmpFile.outputStream().use { out -> compressionFormat.decompressToPipe(source, out) }

            val result = FormatResult.Success<DataSource>(this, tmpFile::inputStream, 1.0)
            result.release.add(Closeable { tmpFile.delete() })
            return result
        }

        return FormatResult.Fail(this, 1.0)
    }
}