package info.spiralframework.base.binding

import dev.brella.kornea.io.common.flow.BinaryOutputFlow
import dev.brella.kornea.io.common.flow.extensions.writeInt16BE
import dev.brella.kornea.io.common.flow.extensions.writeInt16LE
import dev.brella.kornea.io.common.toUTF16BE
import dev.brella.kornea.io.common.toUTF16LE

enum class TextCharsets(val bytesForNull: Int) {
    ASCII(1),
    UTF_8(1),
    UTF_16(2),
    UTF_16LE(2),
    UTF_16BE(2);

    companion object {
        const val UTF_16BE_BOM = 0xFEFF
        const val UTF_16LE_BOM = 0xFFFE
        const val UTF_16_BOM = 0xFEFF
    }
}

expect fun ByteArray.decodeToString(charset: TextCharsets): String
inline fun ByteArray.decodeToUTF8String(): String = decodeToString(TextCharsets.UTF_8)
inline fun ByteArray.decodeToUTF16String(): String = decodeToString(TextCharsets.UTF_16)
inline fun ByteArray.decodeToUTF16LEString(): String = decodeToString(TextCharsets.UTF_16LE)
inline fun ByteArray.decodeToUTF16BEString(): String = decodeToString(TextCharsets.UTF_16BE)

fun manuallyDecode(array: ByteArray, charset: TextCharsets): String {
    when (charset) {
        TextCharsets.ASCII -> return CharArray(array.size) { array[it].toChar() }.concatToString()
        TextCharsets.UTF_8 -> return array.decodeToString()
        TextCharsets.UTF_16 -> {
            if (array.size < 2)
                return ""

            val bom = toUTF16LE(array[0], array[1])

            val builder = StringBuilder()
            if (bom == TextCharsets.UTF_16BE_BOM) {
                for (i in 1 until array.size / 2)
                    builder.append(toUTF16BE(array[i * 2], array[i * 2 + 1]).toChar())
            } else {
                if (bom != TextCharsets.UTF_16LE_BOM)
                    builder.append(bom.toChar())
                for (i in 1 until array.size / 2)
                    builder.append(toUTF16LE(array[i * 2], array[i * 2 + 1]).toChar())
            }

            return builder.toString()
        }
        TextCharsets.UTF_16LE -> {
            val builder = StringBuilder()

            for (i in 0 until array.size / 2)
                builder.append(toUTF16LE(array[i * 2], array[i * 2 + 1]).toChar())

            return builder.toString()
        }
        TextCharsets.UTF_16BE -> {
            val builder = StringBuilder()

            for (i in 0 until array.size / 2)
                builder.append(toUTF16BE(array[i * 2], array[i * 2 + 1]).toChar())

            return builder.toString()
        }
    }
}

expect suspend fun String.encodeToByteArray(charset: TextCharsets): ByteArray
suspend inline fun String.encodeToUTF8ByteArray(): ByteArray = encodeToByteArray(TextCharsets.UTF_8)
suspend inline fun String.encodeToUTF16ByteArray(): ByteArray = encodeToByteArray(TextCharsets.UTF_16)
suspend inline fun String.encodeToUTF16LEByteArray(): ByteArray = encodeToByteArray(TextCharsets.UTF_16LE)
suspend inline fun String.encodeToUTF16BEByteArray(): ByteArray = encodeToByteArray(TextCharsets.UTF_16BE)

@ExperimentalUnsignedTypes
suspend fun manuallyEncode(text: String, charset: TextCharsets, includeByteOrderMarker: Boolean = true): ByteArray {
    when (charset) {
        TextCharsets.ASCII -> return ByteArray(text.length) { text[it].toByte() }
        TextCharsets.UTF_8 -> return text.encodeToByteArray()
        TextCharsets.UTF_16 -> return manuallyEncode(text, TextCharsets.UTF_16LE, includeByteOrderMarker)
        TextCharsets.UTF_16LE -> {
            val output = BinaryOutputFlow()

            if (includeByteOrderMarker)
                output.writeInt16LE(TextCharsets.UTF_16_BOM)

            text.forEach { character -> output.writeInt16LE(character.toInt()) }

            return output.getData()
        }
        TextCharsets.UTF_16BE -> {
            val output = BinaryOutputFlow()

            if (includeByteOrderMarker)
                output.writeInt16BE(TextCharsets.UTF_16_BOM)

            text.forEach { character -> output.writeInt16BE(character.toInt()) }

            return output.getData()
        }
    }
}

expect fun formatPercent(percentage: Double): String
//expect fun uuidString(): String