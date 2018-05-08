package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.spiral.core.objects.game.DRGame
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CPKFormat: SpiralFormat {
    override val name: String = "CPK"
    override val extension: String = "cpk"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat, WADFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        try {
            return CPK(dataSource).files.isNotEmpty()
        } catch(iea: IllegalArgumentException) {
            return false
        }
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, context, dataSource, output, params)) return true
        val cpk = CPK(dataSource)

        when(format) {
            ZIPFormat -> {
                val zip = ZipOutputStream(output)
                cpk.files.forEach { entry ->
                    zip.putNextEntry(ZipEntry("${entry.directoryName}/${entry.fileName}"))
                    entry.inputStream.use { stream -> stream.copyTo(zip) }

                    return@forEach
                }
                zip.finish()
            }
        }

        return true
    }
}