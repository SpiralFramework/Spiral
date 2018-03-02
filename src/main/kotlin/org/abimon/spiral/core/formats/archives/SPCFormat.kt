package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.archives.SPC
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.util.InputStreamFuncDataSource
import org.abimon.visi.lang.replaceLast
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SPCFormat : SpiralFormat {
    override val name = "SPC"
    override val extension = "spc"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat)

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean {
        try {
            return SPC(InputStreamFuncDataSource(dataSource)).files.size >= 1
        } catch (e: IllegalArgumentException) {
        }
        return false
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, dataSource, output, params)) return true

        val spc = SPC(InputStreamFuncDataSource(dataSource))
        val convert = "${params["spc:convert"] ?: false}".toBoolean()
        when (format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                spc.files.forEach { entry ->
                    val data = SpiralFormats.decompressFully(entry::inputStream)
                    if (convert) {
                        val innerFormat = SpiralFormats.formatForData(data, SpiralFormats.drArchiveFormats)
                        val convertTo = innerFormat?.conversions?.firstOrNull()

                        if (innerFormat != null && convertTo != null) {
                            zip.putNextEntry(ZipEntry(entry.name.replaceLast(".${innerFormat.extension}", "") + ".${convertTo.extension ?: "unk"}"))
                            innerFormat.convert(game, convertTo, entry.name, data, zip, params)
                            return@forEach
                        } else if (innerFormat != null) {
                            zip.putNextEntry(ZipEntry(entry.name.replaceLast(".${innerFormat.extension}", "") + ".${innerFormat.extension}"))
                            data().use { stream -> stream.copyTo(zip) }
                            return@forEach
                        }
                    }

                    zip.putNextEntry(ZipEntry(entry.name))
                    data().use { stream -> stream.copyTo(zip) }
                }
                zip.finish()
            }
        }

        return true
    }
}