package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.archives.ICustomArchive
import org.abimon.spiral.core.objects.customPak
import org.abimon.spiral.core.objects.customSPC
import org.abimon.spiral.core.objects.customWAD
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.readInt
import org.abimon.spiral.util.toInt
import org.abimon.visi.util.zip.forEach
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.HashMap
import java.util.zip.ZipInputStream
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.forEach

object ZIPFormat : SpiralFormat {
    override val name = "ZIP"
    override val extension = "zip"
    override val conversions: Array<SpiralFormat> = arrayOf(PAKFormat, SPCFormat, WADFormat)

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

        if (format === PAKFormat) {
            val convert = "${params["pak:convert"] ?: false}".toBoolean()
            dataSource().use { stream ->
                val cacheFiles = ArrayList<File>()
                val customPak = customPak {
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

                    entries.filterKeys { key -> key.toIntOrNull() != null }.toSortedMap(Comparator { o1, o2 -> o1.toInt().compareTo(o2.toInt()) }).forEach { i, data ->
                        val (_, cacheFile) = data().use(CacheHandler::cacheStream)

                        add(i.toInt(), cacheFile)
                    }
                }

                try {
                    customPak.compile(output)
                } finally {
                    cacheFiles.forEach { file -> file.delete() }
                }
            }

            return true
        }

        val archiveOperation: ICustomArchive.() -> Unit = {
            val convert = "${params["${format.extension}:convert"] ?: false}".toBoolean()
            val zipIn = ZipInputStream(dataSource())
            zipIn.use { zip ->
                zip.forEach { entry ->
                    if (entry.name.startsWith(".") || entry.name.startsWith("__"))
                        return@forEach

                    val (data, file) = CacheHandler.cacheStream(zip)

                    if (convert) {
                        val innerFormat = SpiralFormats.formatForData(game, data, "$name/${entry.name}")
                        val convertTo = innerFormat?.conversions?.firstOrNull()

                        if (innerFormat != null && convertTo != null && innerFormat !in SpiralFormats.drArchiveFormats) {
                            val cacheFile = CacheHandler.newCacheFile()
                            FileOutputStream(cacheFile).use { convOut -> innerFormat.convert(game, convertTo, entry.name, data, convOut, params) }
                            add(entry.name.replace(innerFormat.extension ?: "unk", convertTo.extension
                                    ?: "unk"), cacheFile)
                            return@forEach
                        }
                    }

                    add(entry.name, file)
                }
            }
        }

        val archive: ICustomArchive = when(format) {
            SPCFormat -> customSPC(archiveOperation)
            WADFormat -> customWAD(archiveOperation)
            else -> TODO("NYI PAK -> ${format::class.simpleName}")
        }

        archive.compile(output)

        return true
    }
}