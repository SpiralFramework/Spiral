package info.spiralframework.osb.common

import info.spiralframework.base.binding.encodeToUTF8ByteArray
import info.spiralframework.base.common.freeze
import info.spiralframework.base.common.text.appendln
import info.spiralframework.base.common.text.appendlnHex
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.formats.common.scripting.lin.dr1.*
import org.abimon.kornea.io.common.flow.OutputFlow
import kotlin.contracts.ExperimentalContracts

sealed class TranspileOperation {
    data class Dialogue(var speakerEntry: Dr1SpeakerEntry, var text: Dr1TextEntry? = null, var waitFrame: Dr1WaitFrameEntry? = null) : TranspileOperation()
}

@ExperimentalUnsignedTypes
class OSLTranspiler(val out: OutputFlow, val game: DrGame.LinScriptable?, val lin: LinScript) {
    val variables: MutableMap<String, OSLUnion> = HashMap()

    @ExperimentalContracts
    @ExperimentalStdlibApi
    suspend fun transpile() {
        out.println("OSL Script")
        transpile(lin.scriptData.toList())
    }

    fun nameFor(entry: LinEntry) =
            game?.linOpcodeMap?.get(entry.opcode)?.names?.firstOrNull() ?: entry.opcode.toHexString()

    @ExperimentalContracts
    @ExperimentalStdlibApi
    suspend fun MutableList<LinEntry>.dumpEntries(indent: Int = 0) {
        if (size > 1) {
            writeEntry(get(0), indent)
            transpile(drop(1), indent)
        } else if (size == 1) {
            writeEntry(get(0), indent)
        }

        clear()
    }

    @ExperimentalStdlibApi
    suspend fun writeEntry(entry: LinEntry, indent: Int = 0) =
            out.println {
                repeat(indent) { append('\t') }
                append(nameFor(entry))
                append('|')
                append(entry.rawArguments.joinToString())
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
                                out.println {
                                    repeat(indent) { append('\t') }
                                    append(nameFor(entry))
                                    append("|\"")
                                    append(lin.textData[entry.textID].replace("\n", " &{br} "))
                                    append('"')
                                }
                            }
                            else -> writeEntry(entry, indent)
                        }
                    }
                    is TranspileOperation.Dialogue -> {
                        buffer.add(entry)
                        when {
                            op.text == null && entry is Dr1TextEntry -> op.text = entry
                            op.text != null && entry is Dr1WaitFrameEntry -> op.waitFrame = entry
                            op.waitFrame != null && entry is Dr1WaitForInputEntry -> {
                                out.println {
                                    repeat(indent) { append('\t') }
                                    val speakerName = game?.linCharacterIDs?.get(op.speakerEntry.characterID)
                                    if (speakerName != null) {
                                        append(speakerName)
                                    } else {
                                        val variableName = "speaker_${op.speakerEntry.characterID}"
                                        if (variableName !in variables) {
                                            append("val ")
                                            append(variableName)
                                            append(" = ")
                                            appendlnHex(op.speakerEntry.characterID)
                                        }

                                        repeat(indent) { append('\t') }
                                        append('$')
                                        append(variableName)
                                        variables[variableName] = OSLUnion.NumberType(op.speakerEntry.characterID)
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