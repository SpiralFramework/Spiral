package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.compression.CRILAYLAFormat
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.util.bind
import org.abimon.spiral.util.rawInputStreamFor
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CPKFormat: SpiralFormat {
    override val name: String = "CPK"
    override val extension: String = "cpk"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat, WADFormat)

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean {
        try {
            return CPK(dataSource).files.isNotEmpty()
        } catch(iea: IllegalArgumentException) {
            return false
        }
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, dataSource, output, params)) return true
        val cpk = CPK(dataSource)

        when(format) {
            ZIPFormat -> {
                val zip = ZipOutputStream(output)
                cpk.files.forEach { entry ->
                    zip.putNextEntry(ZipEntry("${entry.directoryName}/${entry.fileName}"))
                    if(entry.isCompressed)
                        CRILAYLAFormat.convert(game, SpiralFormat.BinaryFormat, "${entry.directoryName}/${entry.fileName}", entry::rawInputStreamFor.bind(cpk), zip, params)
                    else
                        entry.rawInputStreamFor(cpk).use { stream -> stream.copyTo(zip) }
                }
                zip.finish()
            }
        }

        return true
    }
}