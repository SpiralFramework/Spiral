package info.spiralframework.base.binding

import org.abimon.kornea.io.common.flow.BinaryOutputFlow
import org.abimon.kornea.io.common.toUTF16BE
import org.abimon.kornea.io.common.toUTF16LE
import org.abimon.kornea.io.common.writeInt16BE
import org.abimon.kornea.io.common.writeInt16LE

enum class TextCharsets(val bytesForNull: Int) {
    UTF_8(1),
    UTF_16(2),
    UTF_16LE(2),
    UTF_16BE(2);

    companion object {
        const val UTF_16BE_BOM = 0xFEFF
        const val UTF_16LE_BOM = 0xFFFE
    }
}

@ExperimentalStdlibApi
expect fun ByteArray.decodeToString(charset: TextCharsets): String
@ExperimentalStdlibApi
fun ByteArray.decodeToUTF8String(): String = decodeToString(TextCharsets.UTF_8)
@ExperimentalStdlibApi
fun ByteArray.decodeToUTF16String(): String = decodeToString(TextCharsets.UTF_16)
@ExperimentalStdlibApi
fun ByteArray.decodeToUTF16LEString(): String = decodeToString(TextCharsets.UTF_16LE)
@ExperimentalStdlibApi
fun ByteArray.decodeToUTF16BEString(): String = decodeToString(TextCharsets.UTF_16BE)

@ExperimentalStdlibApi
fun manuallyDecode(array: ByteArray, charset: TextCharsets): String {
    when (charset) {
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

@ExperimentalStdlibApi
expect suspend fun String.encodeToByteArray(charset: TextCharsets): ByteArray
@ExperimentalStdlibApi
suspend fun String.encodeToUTF8ByteArray(): ByteArray = encodeToByteArray(TextCharsets.UTF_8)
@ExperimentalStdlibApi
suspend fun String.encodeToUTF16ByteArray(): ByteArray = encodeToByteArray(TextCharsets.UTF_16)
@ExperimentalStdlibApi
suspend fun String.encodeToUTF16LEByteArray(): ByteArray = encodeToByteArray(TextCharsets.UTF_16LE)
@ExperimentalStdlibApi
suspend fun String.encodeToUTF16BEByteArray(): ByteArray = encodeToByteArray(TextCharsets.UTF_16BE)

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun manuallyEncode(text: String, charset: TextCharsets, includeByteOrderMarker: Boolean = true): ByteArray {
    when (charset) {
        TextCharsets.UTF_8 -> return text.encodeToByteArray()
        TextCharsets.UTF_16 -> return manuallyEncode(text, TextCharsets.UTF_16LE, includeByteOrderMarker)
        TextCharsets.UTF_16LE -> {
            val output = BinaryOutputFlow()

            if (includeByteOrderMarker)
                output.writeInt16LE(TextCharsets.UTF_16LE_BOM)

            text.forEach { character -> output.writeInt16LE(character.toInt()) }

            return output.getData()
        }
        TextCharsets.UTF_16BE -> {
            val output = BinaryOutputFlow()

            if (includeByteOrderMarker)
                output.writeInt16LE(TextCharsets.UTF_16BE_BOM)

            text.forEach { character -> output.writeInt16BE(character.toInt()) }

            return output.getData()
        }
    }
}

expect fun formatPercent(percentage: Double): String
//expect fun uuidString(): String