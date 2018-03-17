package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.archives.CustomPak
import org.abimon.spiral.core.objects.archives.CustomSPC
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.readInt
import org.abimon.spiral.util.InputStreamFuncDataSource
import org.abimon.spiral.util.toInt
import org.abimon.visi.lang.make
import org.abimon.visi.util.zip.forEach
import java.io.InputStream
import java.io.OutputStream
import java.util.HashMap
import java.util.zip.ZipInputStream
import kotlin.Comparator

object ZIPFormat : SpiralFormat {
    override val name = "ZIP"
    override val extension = "zip"
    override val conversions: Array<SpiralFormat> = arrayOf(PAKFormat, SPCFormat)

    val VALID_HEADERS = intArrayOf(
            toInt(byteArrayOf(0x50, 0x4B, 0x03, 0x04), little = true),
            toInt(byteArrayOf(0x50, 0x4B, 0x05, 0x06), little = true),
            toInt(byteArrayOf(0x50, 0x4B, 0x07, 0x08), little = true)
    )

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean {
//        try {
//            return source.use { stream ->
//                val zip = ZipInputStream(stream)
//                var count = 0
//                while (zip.nextEntry != null)
//                    count++
//                zip.close()
//                return@use count > 0
//            }
//        } catch (e: NullPointerException) {
//        } catch (e: IOException) {
//        }


        return dataSource().use { stream -> stream.readInt(little = true).toInt() in VALID_HEADERS }
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, dataSource, output, params)) return true

        when (format) {
            is PAKFormat -> {
                val convert = "${params["pak:convert"] ?: false}".toBoolean()
                dataSource().use { stream ->
                    val customPak = make<CustomPak> {
                        val zipIn = ZipInputStream(stream)
                        val entries = HashMap<String, () -> InputStream>().apply {
                            zipIn.use { zip ->
                                zip.forEach { entry ->
                                    if (entry.name.startsWith('.') || entry.name.startsWith("__"))
                                        return@forEach

                                    val (out, data) = CacheHandler.cacheStream()
                                    out.use { stream -> zip.copyTo(stream) }

                                    if (convert) {
                                        val innerFormat = SpiralFormats.formatForData(game, data, "$name/${entry.name}")
                                        val convertTo = innerFormat?.conversions?.firstOrNull()

                                        if (innerFormat != null && convertTo != null && innerFormat !in SpiralFormats.drArchiveFormats) {
                                            val (convOut, convData) = CacheHandler.cacheStream()
                                            innerFormat.convert(game, convertTo, entry.name, data, convOut, params)

                                            put(entry.name.substringBeforeLast('.'), convData)
                                            return@forEach
                                        }
                                    }

                                    put(entry.name.substringBeforeLast('.'), data)
                                }
                            }
                        }

                        entries.filterKeys { key -> key.toIntOrNull() != null }.toSortedMap(Comparator { o1, o2 -> o1.toInt().compareTo(o2.toInt()) }).forEach { _, data -> dataSource(InputStreamFuncDataSource(data)) }
                    }

                    customPak.compile(output)
                }
            }
            SPCFormat -> {
                val convert = "${params["spc:convert"] ?: false}".toBoolean()
                dataSource().use { stream ->
                    val customSpc = make<CustomSPC> {
                        val zipIn = ZipInputStream(stream)
                        zipIn.use { zip ->
                            zip.forEach { entry ->
                                if (entry.name.startsWith(".") || entry.name.startsWith("__"))
                                    return@forEach

                                val (out, data) = CacheHandler.cacheStream()
                                out.use { stream -> zip.copyTo(stream) }

                                if (convert) {
                                    val innerFormat = SpiralFormats.formatForData(game, data, "$name/${entry.name}")
                                    val convertTo = innerFormat?.conversions?.firstOrNull()

                                    if (innerFormat != null && convertTo != null && innerFormat !in SpiralFormats.drArchiveFormats) {
                                        val (convOut, convData) = CacheHandler.cacheStream()
                                        innerFormat.convert(game, convertTo, entry.name, data, convOut, params)
                                        file(entry.name.replace(innerFormat.extension ?: "unk", convertTo.extension ?: "unk"), InputStreamFuncDataSource(convData))
                                        return@forEach
                                    }
                                }

                                file(entry.name, InputStreamFuncDataSource(data))
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