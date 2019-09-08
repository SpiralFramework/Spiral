package info.spiralframework.base.binding

import info.spiralframework.base.common.io.toUTF16BE
import info.spiralframework.base.common.io.toUTF16LE

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

expect fun formatPercent(percentage: Double): String
expect fun uuidString(): String