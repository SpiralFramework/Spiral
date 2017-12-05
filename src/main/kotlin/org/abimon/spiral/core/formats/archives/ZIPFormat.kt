package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.archives.CustomPak
import org.abimon.spiral.core.objects.archives.CustomSPC
import org.abimon.visi.io.DataSource
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
    override val conversions: Array<SpiralFormat> = arrayOf(PAKFormat, SPCFormat)

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

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        when (format) {
            is PAKFormat -> {
                val convert = "${params["pak:convert"] ?: false}".toBoolean()
                source.use { stream ->
                    val customPak = make<CustomPak> {
                        val zipIn = ZipInputStream(stream)
                        val entries = make<HashMap<String, DataSource>> {
                            zipIn.use { zip ->
                                zip.forEach { entry ->
                                    if(entry.name.startsWith('.') || entry.name.startsWith("__"))
                                        return@forEach

                                    val (out, data) = CacheHandler.cacheStream()
                                    out.use { stream -> zip.copyTo(stream) }

                                    if (convert) {
                                        val innerFormat = SpiralFormats.formatForData(data)
                                        val convertTo = innerFormat?.conversions?.firstOrNull()

                                        if (innerFormat != null && convertTo != null && innerFormat !in SpiralFormats.drArchiveFormats) {
                                            val (convOut, convData) = CacheHandler.cacheStream()
                                            innerFormat.convert(convertTo, data, convOut, params)

                                            put(entry.name.substringBeforeLast('.'), convData)
                                            return@forEach
                                        }
                                    }

                                    put(entry.name.substringBeforeLast('.'), data)
                                }
                            }
                        }

                        entries.filterKeys { key -> key.toIntOrNull() != null }.toSortedMap(Comparator { o1, o2 -> o1.toInt().compareTo(o2.toInt()) }).forEach { _, data -> dataSource(data) }
                    }

                    customPak.compile(output)
                }
            }
            is SPCFormat -> {
                val convert = "${params["spc:convert"] ?: false}".toBoolean()
                source.use { stream ->
                    val customSpc = make<CustomSPC> {
                        val zipIn = ZipInputStream(stream)
                        zipIn.use { zip ->
                            zip.forEach { entry ->
                                if(entry.name.startsWith(".") || entry.name.startsWith("__"))
                                    return@forEach

                                val (out, data) = CacheHandler.cacheStream()
                                out.use { stream -> zip.copyTo(stream) }

                                if (convert) {
                                    val innerFormat = SpiralFormats.formatForData(data)
                                    val convertTo = innerFormat?.conversions?.firstOrNull()

                                    if (innerFormat != null && convertTo != null && innerFormat !in SpiralFormats.drArchiveFormats) {
                                        val (convOut, convData) = CacheHandler.cacheStream()
                                        innerFormat.convert(convertTo, data, convOut, params)
                                        file(entry.name.replace(innerFormat.extension ?: "unk", convertTo.extension ?: "unk"), convData)
                                        return@forEach
                                    }
                                }

                                file(entry.name, data)
                            }
                        }
                    }

                    customSpc.compile(output)
                }
            }
            else -> TODO("NYI PAK -> ${format::class.simpleName}")
        }

        return true
    }
}