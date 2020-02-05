package info.spiralframework.formats.common.scripting.wrd

import info.spiralframework.base.binding.encodeToUTF16LEByteArray
import info.spiralframework.base.binding.encodeToUTF8ByteArray
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.text.removeEscapes
import info.spiralframework.formats.common.games.DRv3
import org.abimon.kornea.io.common.flow.BinaryOutputFlow
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.common.writeInt16BE
import org.abimon.kornea.io.common.writeInt16LE
import org.abimon.kornea.io.common.writeInt32LE

class CustomWordScript {
    private val _labels: MutableList<String> = ArrayList()
    private val _parameters: MutableList<String> = ArrayList()
    private val _strings: MutableList<String> = ArrayList()
    private val _scriptData: MutableList<WrdEntry> = ArrayList()

    val labels: List<String> get() = _labels
    val parameters: List<String> get() = _parameters
    val strings: List<String> get() = _strings
    val scriptData: List<WrdEntry> = ArrayList()

    var writeMagicNumber: Boolean = false
    var rewriteSubLabels: Boolean = true
    var externalStringCount: Int = 0

    private fun add(base: String, list: MutableList<String>): Int {
        val str = base.removeEscapes()
        if (str !in list)
            list.add(str)
        return list.indexOf(str)
    }

    fun addEntry(entry: WrdEntry) {
        entry.arguments.forEach { value ->
            when (value) {
                is WordScriptValue.Label -> addLabel(value.label)
                is WordScriptValue.Parameter -> addParameter(value.param)
                is WordScriptValue.InternalText -> addText(value.text)
            }
        }

        _scriptData.add(entry)
    }

    fun addText(text: String): Int = add(text, _strings)
    fun addLabel(label: String): Int = add(label, _labels)
    fun addParameter(parameter: String): Int = add(parameter, _parameters)

    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.compile(output: OutputFlow) {
        if (writeMagicNumber) output.writeInt32LE(WordScript.MAGIC_NUMBER_LE)

        val entryData = BinaryOutputFlow()
        val labelData = BinaryOutputFlow()
        val parameterData = BinaryOutputFlow()

        val localBranches: MutableList<Pair<ULong, Int>> = ArrayList()

        val subLabels = scriptData.filter { entry -> entry.opcode == 0x4A }
                .map { entry -> entry.arguments[0].raw }
                .distinct()
                .sorted()

        if (rewriteSubLabels && subLabels.isNotEmpty() && subLabels[0] < 0) {
            warn("formats.custom_wrd.sublabel_too_small", 0, subLabels[0])
        }

        if (rewriteSubLabels && subLabels.isNotEmpty() && subLabels.last() > subLabels.size) {
            warn("formats.custom_wrd.sublabel_too_large", subLabels.size, subLabels.last())
        }

        if (strings.isEmpty() && externalStringCount == 0) {
            val highestTextID = scriptData.filter { entry -> entry.opcode == 0x46 }
                    .maxBy { script -> script.arguments[0].raw }
                    ?.arguments
                    ?.get(0)
                    ?.raw

            if (highestTextID != null) {
                externalStringCount = highestTextID + 1
            }
        }

        val sections: MutableList<Long> = ArrayList()

        scriptData.forEach { entry ->
            val mutatedArguments = entry.arguments

            when (entry.opcode) {
                0x14 -> sections.add(entryData.getDataSize().toLong())
                0x4A -> {
                    if (rewriteSubLabels && entry is UnknownWrdEntry && entry.wrdGame is DRv3) {
                        mutatedArguments[0] = WordScriptValue.Raw(subLabels.indexOf(mutatedArguments[0].raw))
                        localBranches.add(entryData.getDataSize() to mutatedArguments[0].raw)
                    }
                }
                0x4B -> {
                    if (rewriteSubLabels && entry is UnknownWrdEntry && entry.wrdGame is DRv3) {
                        mutatedArguments[0] = WordScriptValue.Raw(subLabels.indexOf(mutatedArguments[0].raw))
                    }
                }
            }

            entryData.write(0x70)
            entryData.write(entry.opcode)
            mutatedArguments.forEach { value -> entryData.writeInt16BE(value.raw) }
        }

        labels.forEach { label ->
            val labelBytes = label.encodeToUTF8ByteArray()

            labelData.write(labelBytes.size)
            labelData.write(labelBytes)
            labelData.write(0x00)
        }

        parameters.forEach { parameter ->
            val parameterBytes = parameter.encodeToUTF8ByteArray()

            parameterData.write(parameterBytes.size)
            parameterData.write(parameterBytes)
            parameterData.write(0x00)
        }

        output.writeInt16LE(if (strings.isEmpty()) externalStringCount else strings.size)
        output.writeInt16LE(labels.size)
        output.writeInt16LE(parameters.size)
        output.writeInt16LE(localBranches.size)

        output.writeInt32LE(0) // Headers
        output.writeInt32LE(0x20 + entryData.getDataSize().toLong()) // Local Branches Offset
        output.writeInt32LE(0x20 + entryData.getDataSize().toLong() + (4 * localBranches.size)) // Sections Offset
        output.writeInt32LE(0x20 + entryData.getDataSize().toLong() + (4 * localBranches.size) + (2 * sections.size)) // Labels Offset
        output.writeInt32LE(0x20 + entryData.getDataSize().toLong() + (4 * localBranches.size) + (2 * sections.size) + labelData.getDataSize().toLong()) // Parameters Offset

        if (strings.isEmpty())
            output.writeInt32LE(0)
        else
            output.writeInt32LE(0x20 + entryData.getDataSize().toLong() + (4 * localBranches.size) + (2 * sections.size) + labelData.getDataSize().toLong() + parameterData.getDataSize().toLong()) // Strings Offset

        output.write(entryData.getData())

        localBranches.forEach { (offset, arg) ->
            output.writeInt16LE(arg)
            output.writeInt16LE(offset.toLong())
        }

        sections.suspendForEach(output::writeInt16LE)

        output.write(labelData.getData())
        output.write(parameterData.getData())

        strings.forEach { str ->
            val bytes = str.encodeToUTF16LEByteArray()

            if (bytes.size > 0x7F) {
                output.write(0x80 or bytes.size)
                output.write(bytes.size shr 7)

                output.write(bytes)
                output.writeInt16LE(0x00)
            } else {
                output.write(bytes.size)
                output.write(bytes)
                output.writeInt16LE(0x00)
            }
        }
    }
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun CustomWordScript.compile(output: OutputFlow, context: SpiralContext) = context.compile(output)

inline fun wordScript(block: CustomWordScript.() -> Unit): CustomWordScript {
    val script = CustomWordScript()
    script.block()
    return script
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun OutputFlow.compileWordScript(context: SpiralContext, block: CustomWordScript.() -> Unit) {
    val script = CustomWordScript()
    script.block()
    return script.compile(this, context)
}