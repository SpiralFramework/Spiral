package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.archives.Pak
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.replaceLast
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PAKFormat : SpiralFormat {
    override val name = "PAK"
    override val extension = "pak"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat)

    override fun isFormat(source: DataSource): Boolean {
        try {
            return Pak(source).files.size >= 1
        } catch (e: IllegalArgumentException) {
        }
        return false
    }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        val pak = Pak(source)
        val convert = "${params["pak:convert"] ?: false}".toBoolean()
        when (format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                pak.files.forEach {
                    val data = SpiralFormats.decompressFully(it)
                    if (convert) {
                        val innerFormat = SpiralFormats.formatForData(data, SpiralFormats.drArchiveFormats)
                        val convertTo = innerFormat?.conversions?.firstOrNull()

                        if (innerFormat != null && convertTo != null) {
                            zip.putNextEntry(ZipEntry(it.name.replaceLast(".${innerFormat.extension}", "") + ".${convertTo.extension ?: "unk"}"))
                            innerFormat.convert(convertTo, data, zip, params)
                            return@forEach
                        } else if (innerFormat != null) {
                            zip.putNextEntry(ZipEntry(it.name.replaceLast(".${innerFormat.extension}", "") + ".${innerFormat.extension}"))
                            data.use { stream -> stream.copyTo(zip) }
                            return@forEach
                        }
                    }

                    zip.putNextEntry(ZipEntry(it.name))
                    data.use { stream -> stream.copyTo(zip) }
                }
                zip.finish()
            }
        }

        return true
    }
}