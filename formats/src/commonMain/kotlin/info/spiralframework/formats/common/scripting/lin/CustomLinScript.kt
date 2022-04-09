package info.spiralframework.formats.common.scripting.lin

import dev.brella.kornea.io.common.TextCharsets
import dev.brella.kornea.io.common.encodeToUTF16LEByteArray
import dev.brella.kornea.io.common.flow.BinaryOutputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.writeInt16LE
import dev.brella.kornea.io.common.flow.extensions.writeInt32LE
import dev.brella.kornea.toolkit.common.sumByLong
import info.spiralframework.base.common.NULL_TERMINATOR

public class CustomLinScript {
    public val _textData: MutableList<String> = ArrayList()
    public val textData: List<String>
        get() = _textData

    public val _scriptData: MutableList<LinEntry> = ArrayList()
    public val scriptData: List<LinEntry>
        get() = _scriptData

    public var writeMagicNumber: Boolean = false
    public var includeTextByteOrderMarker: Boolean = true

    public fun addText(text: String): Int {
        if (text !in _textData) _textData.add(text)
        return _textData.indexOf(text)
    }

    public fun addEntry(entry: LinEntry) {
        _scriptData.add(entry)
    }

    public suspend fun compile(output: OutputFlow) {
        if (writeMagicNumber) output.writeInt32LE(LinScript.MAGIC_NUMBER_LE)

        val scriptDataSize = scriptData.sumByLong { entry -> 2 + entry.rawArguments.size }

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
                val textBytes = text.trimEnd(NULL_TERMINATOR).encodeToUTF16LEByteArray()
                    .let {
                        if (!includeTextByteOrderMarker) {
                            if (it.readInt16LE() == TextCharsets.UTF_16LE_BOM) it.copyOfRange(2, it.size)
                            else it
                        } else it
                    }

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

public inline fun linScript(block: CustomLinScript.() -> Unit): CustomLinScript {
    val lin = CustomLinScript()
    lin.block()
    return lin
}

public suspend fun OutputFlow.compileLinScript(block: CustomLinScript.() -> Unit) {
    val lin = CustomLinScript()
    lin.block()
    lin.compile(this)
}