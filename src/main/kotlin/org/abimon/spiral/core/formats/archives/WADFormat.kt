package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.UnsafeWAD
import org.abimon.spiral.core.objects.archives.WAD
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.utils.WindowedInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object WADFormat : SpiralFormat {
    override val name = "WAD"
    override val extension = "wad"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat)

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean {
        return WAD(dataSource)?.files?.isNotEmpty() == true
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, dataSource, output, params)) return true

        val wad = UnsafeWAD(dataSource)
        when (format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                wad.files.forEach { wadEntry ->
                    zip.putNextEntry(ZipEntry(wadEntry.name))
                    WindowedInputStream(wad.dataSource(), wad.dataOffset + wadEntry.offset, wadEntry.size)
                }
                zip.closeEntry()
            }
        }

        return true
    }
}