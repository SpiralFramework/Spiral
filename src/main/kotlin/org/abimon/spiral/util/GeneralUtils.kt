package org.abimon.spiral.util

import org.abimon.karnage.raw.BC4PixelData
import org.abimon.karnage.raw.BC7PixelData
import org.abimon.karnage.raw.DXT1PixelData
import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.archives.IArchive
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.images.SRDFormat.deswizzle
import org.abimon.spiral.core.hasBitSet
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.spiral.core.objects.archives.CPKFileEntry
import org.abimon.spiral.core.objects.archives.WAD
import org.abimon.spiral.core.objects.archives.srd.TXREntry
import org.abimon.spiral.core.utils.ChunkProcessingInputStream
import org.abimon.spiral.core.utils.CountingInputStream
import org.abimon.spiral.core.utils.WindowedInputStream
import org.abimon.visi.collections.copyFrom
import org.abimon.visi.io.skipBytes
import org.abimon.visi.lang.StringGroup
import org.abimon.visi.lang.StringGroups
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KFunction

operator fun SemanticVersion.compareTo(semver: SemanticVersion): Int {
    if (this.first > semver.first)
        return 1
    else if (this.first < semver.first)
        return -1

    if (this.second > semver.second)
        return 1
    else if (this.second < semver.second)
        return -1

    if (this.third > semver.third)
        return 1
    else if (this.third < semver.third)
        return -1

    return 0
}

fun intArrayOfPairs(vararg pairs: Pair<Int, Int>): IntArray {
    val array = IntArray(pairs.size * 2)
    for (i in pairs.indices) {
        val (a, b) = pairs[i]
        array[i * 2] = a
        array[i * 2 + 1] = b
    }

    return array
}

fun Pair(array: IntArray): Pair<Int, Int> = Pair(array[0], array[1])

fun Number.toUnsignedByte(): Int = this.toByte().toInt() and 0xFF

//fun <T> KFunction<T>.bind(vararg params: Pair<String, Any?>): () -> T = { this.call() }

fun <T> KFunction<T>.bind(vararg orderedParams: Any?): () -> T = { this.call(*orderedParams) }

fun CPKFileEntry.rawInputStreamFor(cpk: CPK): InputStream = WindowedInputStream(cpk.dataSource(), this.offset, this.fileSize)

inline fun getLastCaller(stepsDown: Int = 0): String? = Thread.currentThread().stackTrace.copyFrom(1 + stepsDown).firstOrNull { it.className != "org.abimon.spiral.util.LoggerKt" && !it.className.contains('$') }?.toString()

class SeekableInputStream(seekable: InputStream) : CountingInputStream(seekable) {
    fun seek(offset: Long) {
        reset()
        skipBytes(offset)
    }
}

fun InputStream.copyWithProgress(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE, progress: ((Long) -> Unit)?): Long {
    var bytesCopied = 0L
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        progress?.invoke(bytesCopied)
        bytes = read(buffer)
    }
    return bytesCopied
}

fun String.sanitisePath(): String = this.replace("/", File.separator).replace("\\", File.separator)

fun CPK.fileSourceForName(name: String): (() -> InputStream)? {
    val sanitised = name.sanitisePath()

    return files.firstOrNull { entry -> "${entry.directoryName}/${entry.fileName}" == sanitised }?.let { entry -> entry::inputStream }
}

fun WAD.fileSourceForName(name: String): (() -> InputStream)? {
    val sanitised = name.sanitisePath()

    return files.firstOrNull { entry -> entry.name == sanitised }?.let { entry -> entry::inputStream }
}

fun IArchive.fileSourceForname(name: String): (() -> InputStream)? {
    val sanitised = name.sanitisePath()

    return fileEntries.firstOrNull { (entryName) -> entryName == sanitised }?.second
}

val File.absoluteParentFile: File
    get() = File(absolutePath.substringBeforeLast(File.separator))

fun String.splitOutside(delimiter: String = "\\s", cap: Int = 0, group: StringGroup = StringGroups.SPEECH): Array<String> {
    val strings = ArrayList<String>()
    val m = Pattern.compile("${group.start}[^${group.end}\\\\]*(?:\\\\.[^${group.end}\\\\]*)*${group.end}|[^$delimiter]+").matcher(this)
    while (m.find()) {
        val param = m.group(0)

        if (param[0] == '"')
            strings.add(param.substring(1, param.length - 1).removeEscapes())
        else
            strings.add(param.removeEscapes())
    }

    return strings.toTypedArray()
}

fun String.removeEscapes(): String =
        buildString {
            var escaping: Boolean = false
            var controlCharacter = false

            this@removeEscapes.forEach { c ->
                if (escaping) {
                    if (c == '\\') {
                        controlCharacter = true
                        escaping = false
                    } else {
                        append(c)
                        escaping = false
                    }
                } else
                    if (escaping) {
                        when (c) {
                            'n' -> append('\n')
                            't' -> append('\t')
                            'b' -> append('\b')
                            'r' -> append('\r')
                            '0' -> append(0x00.toChar())
                            else -> {
                                append('\\')
                                append(c)
                            }
                        }

                        escaping = false
                    } else if (c == '\\') {
                        escaping = true
                    } else {
                        append(c)
                    }
            }
        }

fun TXREntry.readTexture(srdv: () -> InputStream): BufferedImage? {
    val texture = WindowedInputStream(srdv(), rsiEntry.mipmaps[0].start.toLong(), rsiEntry.mipmaps[0].length.toLong())

    val swizzled = !(swizzle hasBitSet 1)
    if (format in arrayOf(0x01, 0x02, 0x05, 0x1A)) {
        val bytespp: Int

        when (format) {
            0x01 -> bytespp = 4
            0x02 -> bytespp = 2
            0x05 -> bytespp = 2
            0x1A -> bytespp = 4
            else -> bytespp = 2
        }

        val width: Int = displayWidth.toInt() //(scanline / bytespp).toInt()
        val height: Int = displayHeight.toInt()

        val processing: InputStream

        if (swizzled) {
            val processingData = texture.use { it.readBytes() }
            processingData.deswizzle(width / 4, height / 4, bytespp)
            processing = processingData.inputStream()
        } else
            processing = texture

        when (format) {
            0x01 -> {
                val resultingImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val b = processing.read()
                        val g = processing.read()
                        val r = processing.read()
                        val a = processing.read()

                        resultingImage.setRGB(x, y, Color(r, g, b, a).rgb)
                    }
                }

                return resultingImage
            }
            else -> {
                SpiralData.LOGGER.debug("Raw format for {}: {} ({})", this.rsiEntry.name, format, format.toString(16))
                return null
            }
        }
    } else if (format in arrayOf(0x0F, 0x11, 0x14, 0x16, 0x1C)) {
        val bytespp: Int

        when (format) {
            0x0F -> bytespp = 8
            0x1C -> bytespp = 16
            else -> bytespp = 8
        }

        var width: Int = displayWidth
        var height: Int = displayHeight

        if (width % 4 != 0)
            width += 4 - (width % 4)

        if (height % 4 != 0)
            height += 4 - (height % 4)

        val processingStream: InputStream

        if (swizzled && width >= 4 && height >= 4) {
            val processingData = texture.use { it.readBytes() }
            processingData.deswizzle(width / 4, height / 4, bytespp)
            processingStream = processingData.inputStream()
        } else
            processingStream = ByteArrayInputStream(texture.readBytes())

        when (format) {
            0x0F -> return DXT1PixelData.read(width, height, processingStream)
            0x16 -> return BC4PixelData.read(width, height, processingStream)
            0x1C -> return BC7PixelData.read(width, height, processingStream)
            else -> {
                SpiralData.LOGGER.debug("Block format for {}: {} (0x{}) [{}x{}]", this.rsiEntry.name, format, format.toString(16), width, height)
                return null
            }
        }
    } else
        SpiralData.LOGGER.debug("Other format for {}: {} (0x{})", rsiEntry.name, format, format.toString(16))

    return null
}

fun <T> catchAndLog(operation: () -> T): T? {
    try {
        return operation()
    } catch (th: Throwable) {
        CacheHandler.logStackTrace(th)
        return null
    }
}

fun decompress(dataSource: () -> InputStream): () -> InputStream {
    for (method in SpiralFormats.compressionMethods) {
        if (method.isCompressed(dataSource)) {
            if (method.supportsChunking) {
                return decompress func@{
                    val stream = dataSource()
                    method.prepareChunkStream(stream)
                    return@func ChunkProcessingInputStream { method.decompressStreamChunk(stream) }
                }
            } else {
                val data = method.decompress(dataSource)
                return decompress { ByteArrayInputStream(data) }
            }
        }
    }

    return dataSource
}