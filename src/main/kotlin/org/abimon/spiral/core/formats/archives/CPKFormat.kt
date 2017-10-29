package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.formats.compression.CRILAYLAFormat
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.visi.io.DataSource
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CPKFormat: SpiralFormat {
    override val name: String = "CPK"
    override val extension: String = "cpk"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat, WADFormat)

    override fun isFormat(source: DataSource): Boolean {
        try {
            return CPK(source).fileTable.isNotEmpty()
        } catch(iea: IllegalArgumentException) {
            return false
        }
    }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true
        val cpk = CPK(source)

        when(format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                cpk.fileTable.forEach {
                    zip.putNextEntry(ZipEntry(it.name))
                    if(it.isCompressed)
                        CRILAYLAFormat.convert(SpiralFormat.BinaryFormat, it, zip, params)
                    else
                        it.pipe(zip)
                }
                zip.finish()
            }
        }

        return true
    }
}