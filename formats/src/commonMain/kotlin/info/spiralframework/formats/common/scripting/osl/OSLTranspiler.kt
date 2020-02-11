package info.spiralframework.formats.common.scripting.osl

import info.spiralframework.base.binding.encodeToUTF8ByteArray
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.freeze
import info.spiralframework.base.common.text.appendln
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.formats.common.scripting.lin.dr1.*
import info.spiralframework.formats.common.scripting.lin.transpile
import org.abimon.kornea.io.common.flow.OutputFlow
import kotlin.contracts.ExperimentalContracts

sealed class TranspileOperation {
    data class Dialogue(var speakerEntry: Dr1SpeakerEntry, var text: Dr1TextEntry? = null, var waitFrame: Dr1WaitFrameEntry? = null) : TranspileOperation()
}

@ExperimentalUnsignedTypes
class LinTranspiler(val lin: LinScript, val game: DrGame.LinScriptable? = lin.game) {
    companion object {
        val VARIABLE_NAME_REGEX = "([a-zA-Z0-9_]+)".toRegex()
        val ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX = "[^a-zA-Z0-9_]".toRegex()
        val VARIABLE_COMPARATOR: Comparator<String> = Comparator { a, b ->
            if (a.length != b.length) a.length.compareTo(b.length)
            else a.compareTo(b)
        }

        fun sortVariableNames(keys: Set<String>): List<String> =
                keys.groupBy { name -> name.substringBefore('_') }
                        .mapValues { (_, list) -> list.sortedWith(VARIABLE_COMPARATOR) }
                        .entries
                        .sortedBy(Map.Entry<String, *>::key)
                        .flatMap(Map.Entry<String, List<String>>::value)
    }

    val variables: MutableMap<String, TranspilerVariableValue> = HashMap()
    val output: MutableList<String> = ArrayList()

    @ExperimentalContracts
    @ExperimentalStdlibApi
    suspend fun transpile(out: OutputFlow) {
        try {
            transpile(lin.scriptData.toList())
        } finally {
            out.println("OSL Script")
            out.write('\n'.toInt())
            out.println {
                sortVariableNames(variables.keys)
                        .forEach { varName ->
                            append("val ")
                            append(varName)
                            append(" = ")
                            variables[varName]?.represent(this)
                            appendln()
                        }
            }
            output.suspendForEach(out::println)
            output.clear()
        }
    }

    fun nameFor(entry: LinEntry) =
            game?.linOpcodeMap?.get(entry.opcode)?.names?.firstOrNull() ?: entry.opcode.toHexString()

    fun addOutput(block: StringBuilder.() -> Unit) {
        val builder = StringBuilder()
        builder.block()
        output.add(builder.toString())
    }

    @ExperimentalContracts
    @ExperimentalStdlibApi
    suspend fun MutableList<LinEntry>.dumpEntries(indent: Int = 0) {
        if (size > 1) {
            get(0).transpile(this@LinTranspiler, indent)
            transpile(drop(1), indent)
        } else if (size == 1) {
            get(0).transpile(this@LinTranspiler, indent)
        }

        clear()
    }

    @ExperimentalContracts
    @ExperimentalStdlibApi
    private suspend fun transpile(entries: List<LinEntry>, indent: Int = 0) {
        val buffer: MutableList<LinEntry> = ArrayList()
        var operation: TranspileOperation? = null

        entries.forEach { entry ->
            if (entry is Dr1FormatEntry)
                return@forEach

            freeze(operation) { op ->
                when (op) {
                    null -> {
                        when (entry) {
                            is Dr1SpeakerEntry -> {
                                buffer.add(entry)
                                operation = TranspileOperation.Dialogue(entry)
                            }
                            is Dr1TextEntry -> {
                                output.add {
                                    repeat(indent) { append('\t') }
                                    append(nameFor(entry))
                                    append("|\"")
                                    append(lin.textData[entry.textID].replace("\n", " &{br} "))
                                    append('"')
                                }
                            }
                            else -> entry.transpile(this, indent)
                        }
                    }
                    is TranspileOperation.Dialogue -> {
                        buffer.add(entry)
                        when {
                            op.text == null && entry is Dr1TextEntry -> op.text = entry
                            op.text != null && entry is Dr1WaitFrameEntry -> op.waitFrame = entry
                            op.waitFrame != null && entry is Dr1WaitForInputEntry -> {
                                output.add {
                                    repeat(indent) { append('\t') }
                                    val speakerName = game?.linCharacterIDs?.get(op.speakerEntry.characterID)
                                    if (speakerName != null) {
                                        append(speakerName)
                                    } else {
                                        val variableName = "speaker_${op.speakerEntry.characterID}"
                                        if (variableName !in variables) {
                                            variables[variableName] = NumberValue(op.speakerEntry.characterID)
                                        }

                                        repeat(indent) { append('\t') }
                                        append('$')
                                        append(variableName)
                                    }
                                    append(": \"")
                                    append(lin.textData[op.text!!.textID].replace("\n", " &{br} "))
                                    append('"')

                                    buffer.clear()
                                    operation = null
                                }
                            }
                            else -> {
                                println(">:( $entry")
                                buffer.dumpEntries()
                                operation = null
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun MutableList<String>.add(block: StringBuilder.() -> Unit) {
    val builder = StringBuilder()
    builder.block()
    add(builder.toString())
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
private suspend fun OutputFlow.println(string: String) {
    write(string.encodeToUTF8ByteArray())
    write('\n'.toInt())
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
private suspend fun OutputFlow.println(block: StringBuilder.() -> Unit) {
    val builder = StringBuilder()
    builder.block()
    builder.appendln()
    write(builder.toString().encodeToUTF8ByteArray())
}