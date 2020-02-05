package info.spiralframework.formats.common.scripting.lin

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.binding.manuallyEncode
import info.spiralframework.base.common.sumByLong
import info.spiralframework.base.common.NULL_TERMINATOR
import org.abimon.kornea.io.common.flow.BinaryOutputFlow
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.common.writeInt16LE
import org.abimon.kornea.io.common.writeInt32LE

class CustomLinScript {
    private val _textData: MutableList<String> = ArrayList()
    val textData: List<String>
        get() = _textData

    val _scriptData: MutableList<LinEntry> = ArrayList()
    val scriptData: List<LinEntry>
        get() = _scriptData

    var writeMagicNumber: Boolean = false
    var includeTextByteOrderMarker: Boolean = true

    fun addText(text: String): Int {
        if (text !in _textData) _textData.add(text)
        return _textData.indexOf(text)
    }

    fun addEntry(entry: LinEntry) {
        _scriptData.add(entry)
    }

    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    suspend fun compile(output: OutputFlow) {
        if (writeMagicNumber) output.writeInt32LE(LinScript.MAGIC_NUMBER_LE)

        val scriptDataSize = scriptData.sumByLong { entry -> 2 + entry.rawArguments.size}

        if (textData.isEmpty()) {
            output.writeInt32LE(1)          // 1 section
            output.writeInt32LE(12)         // Script Data Offset
            output.writeInt32LE(12 + scriptDataSize)   // File Size

            scriptData.forEach { entry ->
                output.write(0x70)
                output.write(entry.opcode)
                output.write(ByteArray(entry.rawArguments.size) { entry.rawArguments[it].toByte() })
            }
        } else {
            val textOffsets = IntArray(textData.size) { 4 + (textData.size * 4) }
            val textOutputFlow = BinaryOutputFlow()

            var textOffset = 0
            textData.forEachIndexed { index, text ->
                textOffsets[index] += textOffset
                val textBytes = manuallyEncode(text.trimEnd(NULL_TERMINATOR), TextCharsets.UTF_16LE, includeByteOrderMarker = includeTextByteOrderMarker)

                textOutputFlow.write(textBytes)
                textOutputFlow.writeInt16LE(0x00)

                textOffset += textBytes.size + 2
            }

            output.writeInt32LE(2)
            output.writeInt32LE(16)
            output.writeInt32LE(16 + scriptDataSize)
            output.writeInt32LE(16 + scriptDataSize + 4 + (textData.size * 4) + textOffset)

            scriptData.forEach { entry ->
                output.write(0x70)
                output.write(entry.opcode)
                output.write(ByteArray(entry.rawArguments.size) { entry.rawArguments[it].toByte() })
            }

            output.writeInt32LE(textData.size)
            textOffsets.forEach { offset -> output.writeInt32LE(offset) }
            output.write(textOutputFlow.getData())
        }
    }
}

inline fun linScript(block: CustomLinScript.() -> Unit): CustomLinScript {
    val lin = CustomLinScript()
    lin.block()
    return lin
}
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun OutputFlow.compileLinScript(block: CustomLinScript.() -> Unit) {
    val lin = CustomLinScript()
    lin.block()
    lin.compile(this)
}