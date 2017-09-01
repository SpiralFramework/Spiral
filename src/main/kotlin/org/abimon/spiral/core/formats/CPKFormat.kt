package org.abimon.spiral.core.formats

import org.abimon.spiral.core.objects.CPK
import org.abimon.visi.io.DataSource
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CPKFormat: SpiralFormat {
    override val name: String = "CPK"
    override val extension: String = "cpk"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat)

    override fun isFormat(source: DataSource): Boolean {
        try {
            return CPK(source).fileTable.isNotEmpty()
        } catch(iea: IllegalArgumentException) {
            return false
        }
    }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) {
        super.convert(format, source, output, params)
        val cpk = CPK(source)

        when(format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                cpk.fileTable.forEach {
                    zip.putNextEntry(ZipEntry(it.name))
                    it.pipe(zip)
                }
                zip.closeEntry()
            }
        }
    }
}