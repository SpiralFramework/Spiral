@file:Suppress("unused")

package info.spiralframework.base.common.io

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.binding.decodeToString
import org.abimon.kornea.io.common.flow.BinaryOutputFlow
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.readExact
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readIntXLE
import org.abimon.kornea.io.common.writeInt16LE
import org.abimon.kornea.io.common.writeIntXLE

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun InputFlow.readString(len: Int, encoding: TextCharsets, overrideMaxLen: Boolean = false): String? {
    val data = ByteArray(if (overrideMaxLen) len.coerceAtLeast(0) else len.coerceIn(0, 1024 * 1024))
    return readExact(data)?.decodeToString(encoding)
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readAsciiString(len: Int, overrideMaxLen: Boolean = false): String? {
    val data = ByteArray(if (overrideMaxLen) len.coerceAtLeast(0) else len.coerceIn(0, 1024 * 1024))
    if (read(data) != data.size) return null
    return String(CharArray(data.size) { data[it].toChar() })
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readNumBytes(num: Int): ByteArray {
    val data = ByteArray(num)
    read(data)
    return data
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun InputFlow.readNullTerminatedUTF8String(): String = readNullTerminatedString(encoding = TextCharsets.UTF_8)

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun InputFlow.readNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
    val data = BinaryOutputFlow()

    while (true) {
        val read = readIntXLE(encoding.bytesForNull) ?: break //This **should** work
        require(read != -1) { "Uho..., it's -1 somehow" }
        if (read == 0x00)
            break

        data.writeIntXLE(read, encoding.bytesForNull)
    }

    return data.getData().decodeToString(encoding)
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun InputFlow.readSingleByteNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
    val data = BinaryOutputFlow()

    while (true) {
        val read = read() ?: break
        require(read != -1) { "Uho..., it's -1 somehow" }
        if (read == 0x00)
            break

        data.write(read)
    }

    return data.getData().decodeToString(encoding)
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun InputFlow.readDoubleByteNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_16): String {
    val data = BinaryOutputFlow()

    while (true) {
        val read = readInt16LE() ?: break
        require(read != -1) { "Uho..., it's -1 somehow" }
        if (read == 0x00)
            break

        data.writeInt16LE(read)
    }

    return data.getData().decodeToString(encoding)
}