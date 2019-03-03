package info.spiralframework.core.formats.archives

import info.spiralframework.base.path
import info.spiralframework.core.formats.EnumFormatWriteResponse
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.archives.*
import info.spiralframework.formats.archives.srd.SRDEntry
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.base.util.copyFromStream
import info.spiralframework.base.util.copyToStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipFormat : ReadableSpiralFormat<ZipFile>, WritableSpiralFormat {
    override val name: String = "Zip"

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
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<ZipFile> {
        source().use { stream ->
            val possibleFile = stream.path?.let(::File)
            if (possibleFile?.exists() == true) {
                try {
                    return FormatResult.Success(ZipFile(possibleFile), 1.0)
                } catch (io: IOException) {
                    return FormatResult.Fail(1.0)
                }
            } else {
                val zip: ZipFile
                val tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".dat")
                tmpFile.deleteOnExit()

                try {
                    FileOutputStream(tmpFile).use(stream::copyToStream)
                    zip = ZipFile(tmpFile)
                } catch (io: IOException) {
                    tmpFile.delete()

                    return FormatResult.Fail(1.0)
                }

                return FormatResult.Success(zip, 1.0)
            }
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
    override fun supportsWriting(data: Any): Boolean = data is IArchive || data is ZipFile

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
        val zipOut = ZipOutputStream(stream)

        try {
            when (data) {
                is ZipFile -> data.entries().iterator().forEach { entry ->
                    zipOut.putNextEntry(entry)
                    data.getInputStream(entry).use(zipOut::copyFromStream)
                }

                is AWB -> data.entries.forEach { entry ->
                    zipOut.putNextEntry(ZipEntry(entry.id.toString()))
                    entry.inputStream.use(zipOut::copyFromStream)
                }
                is CPK -> data.files.forEach { entry ->
                    zipOut.putNextEntry(ZipEntry(entry.name))
                    entry.inputStream.use(zipOut::copyFromStream)
                }
                is Pak -> data.files.forEach { entry ->
                    zipOut.putNextEntry(ZipEntry(entry.index.toString()))
                    entry.inputStream.use(zipOut::copyFromStream)
                }
                is SPC -> data.files.forEach { entry ->
                    zipOut.putNextEntry(ZipEntry(entry.name))
                    entry.inputStream.use(zipOut::copyFromStream)
                }
                is SRD -> data.entries.groupBy(SRDEntry::dataType).forEach { (_, list) ->
                    list.forEachIndexed { index, entry ->
                        zipOut.putNextEntry(ZipEntry("${entry.dataType}-$index-data"))
                        entry.dataStream.use(zipOut::copyFromStream)
                        zipOut.putNextEntry(ZipEntry("${entry.dataType}-$index-subdata"))
                        entry.subdataStream.use(zipOut::copyFromStream)
                    }
                }
                is WAD -> data.files.forEach { entry ->
                    zipOut.putNextEntry(ZipEntry(entry.name))
                    entry.inputStream.use(zipOut::copyFromStream)
                }
                else -> return EnumFormatWriteResponse.WRONG_FORMAT
            }
        } finally {
            zipOut.finish()
        }

        return EnumFormatWriteResponse.SUCCESS
    }
}