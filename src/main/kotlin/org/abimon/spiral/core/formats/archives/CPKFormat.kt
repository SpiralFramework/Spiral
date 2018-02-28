package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.compression.CRILAYLAFormat
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.util.InputStreamFuncDataSource
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
            return CPK(InputStreamFuncDataSource(dataSource)).fileTable.isNotEmpty()
        } catch(iea: IllegalArgumentException) {
            return false
        }
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, dataSource, output, params)) return true
        val cpk = CPK(InputStreamFuncDataSource(dataSource))

        when(format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                cpk.fileTable.forEach { entry ->
                    zip.putNextEntry(ZipEntry(entry.name))
                    if(entry.isCompressed)
                        CRILAYLAFormat.convert(game, SpiralFormat.BinaryFormat, entry.name, entry::inputStream, zip, params)
                    else
                        entry.pipe(zip)
                }
                zip.finish()
            }
        }

        return true
    }
}