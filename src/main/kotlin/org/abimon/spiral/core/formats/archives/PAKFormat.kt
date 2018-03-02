package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.UnsafePak
import org.abimon.spiral.core.objects.archives.Pak
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.visi.lang.extension
import org.abimon.visi.lang.replaceLast
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PAKFormat : SpiralFormat {
    override val name = "PAK"
    override val extension = "pak"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat)

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean {
        try {
            return Pak(dataSource)?.files?.isNotEmpty() == true
        } catch (oom: OutOfMemoryError) {
            oom.printStackTrace()
        }
        return false
    }

    override fun isFormatWithConfidence(game: DRGame?, name: String?, dataSource: () -> InputStream): Pair<Boolean, Double> {
        val isFormat = isFormat(game, name, dataSource)
        val confidence = if (name?.endsWith("pak") == true) 1.0 else 0.5
        return isFormat to confidence
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, dataSource, output, params)) return true

        val pak = UnsafePak(dataSource)
        val convert = "${params["pak:convert"] ?: false}".toBoolean()
        when (format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                val pakNames = if (game is HopesPeakDRGame && name != null) game.pakNames[name] else null
                pak.files.forEachIndexed { index, entry ->
                    val data = SpiralFormats.decompressFully(entry::inputStream)

                    if (pakNames != null && pakNames.size > index) {
                        val pakName = pakNames[index]

                        if (convert) {
                            val innerFormat = SpiralFormats.formatForNameAndData(pakName, data, game, SpiralFormats.drArchiveFormats)
                            val convertTo = innerFormat?.conversions?.firstOrNull()

                            if (innerFormat != null && convertTo != null) {
                                zip.putNextEntry(ZipEntry(pakName.replaceLast(".${innerFormat.extension ?: pakName.extension}", ".${convertTo.extension ?: pakName.extension}")))
                                innerFormat.convert(game, convertTo, pakName, data, zip, params)
                                return@forEachIndexed
                            }
                        }

                        zip.putNextEntry(ZipEntry(pakName))
                        data().use { stream -> stream.copyTo(zip) }
                        return@forEachIndexed
                    } else if (convert) {
                        val innerFormat = SpiralFormats.formatForData(data, SpiralFormats.drArchiveFormats)
                        val convertTo = innerFormat?.conversions?.firstOrNull()

                        if (innerFormat != null && convertTo != null) {
                            zip.putNextEntry(ZipEntry("$index.${convertTo.extension ?: "unk"}"))
                            innerFormat.convert(game, convertTo, null, data, zip, params)
                            return@forEachIndexed
                        } else if (innerFormat != null) {
                            zip.putNextEntry(ZipEntry("$index.${innerFormat.extension}"))
                            data().use { stream -> stream.copyTo(zip) }
                            return@forEachIndexed
                        }
                    }

                    zip.putNextEntry(ZipEntry(index.toString()))
                    data().use { stream -> stream.copyTo(zip) }
                }
                zip.finish()
            }
        }

        return true
    }
}