package org.abimon.spiral.core.formats

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.objects.CustomPak
import org.abimon.visi.io.ByteArrayDataSource
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FunctionDataSource
import org.abimon.visi.lang.make
import org.abimon.visi.util.zip.forEach
import java.io.IOException
import java.io.OutputStream
import java.util.HashMap
import java.util.zip.ZipInputStream
import kotlin.Comparator

object ZIPFormat : SpiralFormat {
    override val name = "ZIP"
    override val extension = "zip"
    override val conversions: Array<SpiralFormat> = arrayOf(PAKFormat)

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

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) {
        super.convert(format, source, output, params)

        when (format) {
            is PAKFormat -> {
                val convert = "${params["pak:convert"] ?: false}".toBoolean()
                source.use { stream ->
                    val customPak = make<CustomPak> {
                        val zipIn = ZipInputStream(stream)
                        val entries = make<HashMap<String, DataSource>> {
                            zipIn.forEach { entry ->
                                val data = ByteArrayDataSource(zipIn.readBytes())
                                if (convert) {
                                    val innerFormat = SpiralFormats.formatForData(data)
                                    val convertTo = innerFormat?.conversions?.firstOrNull()

                                    if (innerFormat != null && convertTo != null && innerFormat !in SpiralFormats.drWadFormats) {
                                        put(entry.name.substringBeforeLast('.'), FunctionDataSource { innerFormat.convertToBytes(convertTo, data, params) })
                                        return@forEach
                                    }
                                }

                                put(entry.name.substringBeforeLast('.'), data) }
                        }

                        entries.filterKeys { key -> key.toIntOrNull() != null }.toSortedMap(Comparator { o1, o2 -> o1.toInt().compareTo(o2.toInt()) }).forEach { _, data -> dataSource(data) }
                    }

                    customPak.compile(output)
                }
            }
            else -> TODO("NYI PAK -> ${format::class.simpleName}")
        }
    }
}