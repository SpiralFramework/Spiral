package org.abimon.spiral.core.formats

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.isDebug
import org.abimon.spiral.core.objects.Pak
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.writeTo
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
            if(isDebug) e.printStackTrace()
        }
        return false
    }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) {
        super.convert(format, source, output, params)

        val pak = Pak(source)
        val convert = "${params["pak:convert"] ?: false}".toBoolean()
        when (format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                pak.files.forEach {
                    if (convert) {
                        val innerFormat = SpiralFormats.formatForData(it, SpiralFormats.drWadFormats)
                        val convertTo = innerFormat?.conversions?.firstOrNull()

                        if (innerFormat != null && convertTo != null) {
                            zip.putNextEntry(ZipEntry(it.name.replaceLast(".${innerFormat.extension}", "") + ".${convertTo.extension ?: "unk"}"))
                            innerFormat.convert(convertTo, it, zip, params)
                            return@forEach
                        }
                    }

                    zip.putNextEntry(ZipEntry(it.name))
                    it.use { stream -> stream.writeTo(zip) }
                }
                zip.finish()
            }
        }
    }
}