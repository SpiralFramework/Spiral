package org.abimon.spiral.core.formats

import org.abimon.spiral.core.objects.CustomPak
import org.abimon.visi.io.DataSource
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipInputStream

object ZIPFormat : SpiralFormat {
    override val name = "ZIP"
    override val extension = "zip"

    override fun isFormat(source: DataSource): Boolean {
        try {
            return source.use { stream ->
                val zip = ZipInputStream(stream)
                var count = 0
                while (zip.nextEntry != null)
                    count++
                zip.close()
                return@use count > 0
            }
        } catch (e: NullPointerException) {
        } catch (e: IOException) {
        }

        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is PAKFormat || format is WADFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        when (format) {
            is PAKFormat -> CustomPak(source).compile(output)
            else -> TODO("NYI PAK -> ${format::class.simpleName}")
        }
    }
}