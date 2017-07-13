package org.abimon.spiral.core.formats

import org.abimon.spiral.core.objects.Pak
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.writeTo
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PAKFormat : SpiralFormat {
    override val name = "PAK"
    override val extension = "pak"
    override val preferredConversions: Array<SpiralFormat> = arrayOf(ZIPFormat)

    override fun isFormat(source: DataSource): Boolean {
        try {
            Pak(source)
            return true
        } catch (e: IllegalArgumentException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is ZIPFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        val pak = Pak(source)
        when (format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                pak.files.forEach {
                    zip.putNextEntry(ZipEntry(it.name))
                    it.inputStream.writeTo(zip, closeAfter = true)
                }
                zip.close()
            }
        }
    }
}