package org.abimon.spiral.core.formats

import org.abimon.spiral.core.objects.WAD
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.writeTo
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object WADFormat : SpiralFormat {
    override val name = "WAD"
    override val extension = "wad"

    override fun isFormat(source: DataSource): Boolean {
        try {
            WAD(source)
            return true
        } catch(illegal: IllegalArgumentException) {
        }

        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is ZIPFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        val wad = WAD(source)
        when (format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                wad.files.forEach {
                    zip.putNextEntry(ZipEntry(it.name))
                    it.inputStream.writeTo(zip, closeAfter = true)
                }
                zip.close()
            }
        }
    }
}