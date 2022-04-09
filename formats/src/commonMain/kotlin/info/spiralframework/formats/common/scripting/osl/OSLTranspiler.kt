package info.spiralframework.formats.common.scripting.osl

import dev.brella.kornea.toolkit.common.PrintFlow
import dev.brella.kornea.toolkit.common.freeze
import dev.brella.kornea.toolkit.common.printLine
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.formats.common.scripting.lin.dr1.*
import info.spiralframework.formats.common.scripting.lin.transpile
import kotlin.math.min

public sealed class TranspileOperation {
    public data class Dialogue(
        var speakerEntry: Dr1SpeakerEntry,
        var voiceLineEntry: Dr1VoiceLineEntry? = null,
        var text: Dr1TextEntry? = null,
        var waitFrame: Dr1WaitFrameEntry? = null
    ) : TranspileOperation()

    public data class Text(var text: Dr1TextEntry, var waitFrame: Dr1WaitFrameEntry? = null) : TranspileOperation()
    public data class CheckFlag(
        val flagCheck: Dr1CheckFlagEntry,
        var endFlagCheck: Dr1EndFlagCheckEntry? = null,
        var whenTrue: Int? = null,
        var whenTrueData: List<LinEntry>? = null,
        var whenFalse: Int? = null
    ) : TranspileOperation()

    public data class CheckCharacterOrObject(
        val checkCharacterEntry: Dr1CheckCharacterEntry?,
        val checkObjectEntry: Dr1CheckObjectEntry?
    ) : TranspileOperation()

    public data class PresentSelection(
        val uiEntry: Dr1ChangeUIEntry,
        var entryLabel: Dr1GoToLabelEntry? = null,
        val branchBuffer: MutableList<LinEntry> = ArrayList(),
        val branches: MutableMap<Int, List<LinEntry>> = HashMap()
    ) : TranspileOperation()
}

public class LinTranspiler(public val lin: LinScript, public val game: DrGame.LinScriptable? = lin.game) {
    public companion object {
        public val VARIABLE_NAME_REGEX: Regex = "([a-zA-Z0-9_]+)".toRegex()
        public val ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX: Regex = "[^a-zA-Z0-9_]".toRegex()
        public val VARIABLE_COMPARATOR: Comparator<String> = Comparator { a, b ->
            val aComponents = a.split('_')
            val bComponents = b.split('_')

            for (i in 0 until min(aComponents.size, bComponents.size)) {
                if (aComponents[i] == bComponents[i])
                    continue

                val aNum = aComponents[i].toIntOrNull() ?: return@Comparator aComponents[i].compareTo(bComponents[i])
                val bNum = bComponents[i].toIntOrNull() ?: return@Comparator aComponents[i].compareTo(bComponents[i])

                return@Comparator aNum.compareTo(bNum)
            }

            return@Comparator a.compareTo(b)
        }

        public fun sortVariableNames(keys: Set<String>): List<List<String>> =
            keys.groupBy { name -> name.substringBefore('_') }
                .mapValues { (_, list) -> list.sortedWith(VARIABLE_COMPARATOR) }
                .entries
                .sortedBy(Map.Entry<String, *>::key)
                .map(Map.Entry<String, List<String>>::value)
    }

    public val variables: MutableMap<String, TranspilerVariableValue> = HashMap()
    public val variableMappings: MutableMap<Regex, String> = HashMap()
    public val output: MutableList<String> = ArrayList()

    public suspend fun transpile(out: PrintFlow) {
        try {
            transpile(lin.scriptData.toList())
        } finally {
            out.printLine("OSL Script")
            out.print('\n')
            sortVariableNames(variables.keys)
                .forEach { variableGroup ->
                    out.printLine(buildString {
                        variableGroup.forEach { varName ->
                            append("val ")
                            append(varName)
                            append(" = ")
                            variables[varName]?.represent(this)
                            appendLine()
                        }
                    })
                }
            output.forEach { line ->
                out.printLine(variableMappings.entries.fold(line) { str, (k, v) ->
                    str.replace(k, v)
                })
            }
            output.clear()
        }
    }

    public fun nameFor(entry: LinEntry): String =
        game?.linOpcodeMap?.get(entry.opcode)?.names?.firstOrNull() ?: entry.opcode.toHexString()

    public fun addOutput(block: StringBuilder.() -> Unit) {
        val builder = StringBuilder()
        builder.block()
        output.add(builder.toString())
    }

    private suspend fun MutableList<LinEntry>.dumpEntries(indent: Int = 0) {
        if (size > 1) {
            get(0).transpile(this@LinTranspiler, indent)
            transpile(drop(1), indent)
        } else if (size == 1) {
            get(0).transpile(this@LinTranspiler, indent)
        }

        clear()
    }

    public fun String.sanitise(): String = replace("\"", "\\\"").replace("\n", " &br ")

    private suspend fun transpile(entries: List<LinEntry>, indent: Int = 0) {
        val buffer: MutableList<LinEntry> = ArrayList()
        var operation: TranspileOperation? = null
        var i = 0

        while (i in entries.indices) {
            val entry = entries[i++]

            if (entry is Dr1FormatEntry)
                continue

            freeze(operation) { op ->
                when (op) {
                    null -> {
                        when (entry) {
                            is Dr1SpeakerEntry -> {
                                buffer.add(entry)
                                operation = TranspileOperation.Dialogue(entry)
                            }
                            is Dr1TextEntry -> {
                                buffer.add(entry)
                                operation = TranspileOperation.Text(entry)
                            }
                            is Dr1CheckFlagEntry -> {
                                buffer.add(entry)
                                operation = TranspileOperation.CheckFlag(entry)
                            }
                            is Dr1CheckCharacterEntry -> {
                                if (entry.characterID != 255) {
                                    buffer.add(entry)
                                    operation = TranspileOperation.CheckCharacterOrObject(entry, null)
                                } else {
                                    entry.transpile(this, indent)
                                }
                            }
                            is Dr1CheckObjectEntry -> {
                                if (entry.objectID != 255) {
                                    buffer.add(entry)
                                    operation = TranspileOperation.CheckCharacterOrObject(null, entry)
                                } else {
                                    entry.transpile(this, indent)
                                }
                            }
                            is Dr1ChangeUIEntry -> {
                                if (entry.state == 1) {
                                    when (entry.element) {
                                        19 -> {
                                            buffer.add(entry)
                                            operation = TranspileOperation.PresentSelection(entry)
                                        }
                                        else -> entry.transpile(this, indent)
                                    }
                                } else {
                                    entry.transpile(this, indent)
                                }
                            }
                            else -> entry.transpile(this, indent)
                        }
                    }
                    is TranspileOperation.Dialogue -> {
                        buffer.add(entry)
                        when {
                            op.text == null && entry is Dr1VoiceLineEntry -> op.voiceLineEntry = entry
                            op.text == null && entry is Dr1TextEntry -> op.text = entry
                            op.text != null && entry is Dr1WaitFrameEntry -> op.waitFrame = entry
                            op.waitFrame != null && entry is Dr1WaitForInputEntry -> {
                                op.voiceLineEntry?.transpile(this, indent)

                                output.add {
                                    repeat(indent) { append('\t') }
                                    val speakerName = game?.linCharacterIDs?.get(op.speakerEntry.characterID)
                                    if (speakerName != null) {
                                        append(speakerName)
                                    } else {
                                        val variableName = "speaker_${op.speakerEntry.characterID}"
                                        if (variableName !in variables) {
                                            variables[variableName] = RawNumberValue(op.speakerEntry.characterID)
                                        }

                                        append('$')
                                        append(variableName)
                                    }
                                    append(": \"")
                                    append(lin.textData[op.text!!.textID].sanitise())
                                    append('"')

                                    buffer.clear()
                                    operation = null
                                }
                            }
                            else -> {
//                                println(">:( $entry")
                                buffer.removeAt(buffer.size - 1)
                                i--

                                buffer.dumpEntries(indent)
                                operation = null
                            }
                        }
                    }
                    is TranspileOperation.Text -> {
                        buffer.add(entry)

                        when {
                            entry is Dr1WaitFrameEntry -> op.waitFrame = entry
                            op.waitFrame != null && entry is Dr1WaitForInputEntry -> {
                                output.add {
                                    repeat(indent) { append('\t') }
                                    append("Text(\"")
                                    append(lin[op.text.textID].sanitise())
                                    append("\")")
                                }

                                buffer.clear()
                                operation = null
                            }
                            else -> {
//                                println(">:( $entry")
                                buffer.removeAt(buffer.size - 1)
                                i--
                                buffer.dumpEntries(indent)
                                operation = null
                            }
                        }
                    }
                    is TranspileOperation.CheckFlag -> {
                        buffer.add(entry)

                        when {
                            op.endFlagCheck == null -> {
                                if (entry is Dr1EndFlagCheckEntry) {
                                    op.endFlagCheck = entry
                                } else {
                                    buffer.removeAt(buffer.size - 1)
                                    i--
                                    buffer.dumpEntries(indent)
                                    operation = null
                                }
                            }
                            op.endFlagCheck != null && op.whenTrue == null -> {
                                if (entry is Dr1GoToLabelEntry) {
                                    op.whenTrue = entry.id
                                } else {
                                    buffer.removeAt(buffer.size - 1)
                                    i--
                                    buffer.dumpEntries(indent)
                                    operation = null
                                }
                            }
                            op.whenTrue != null && op.whenFalse == null -> {
                                if (entry is Dr1MarkLabelEntry && op.whenTrue == entry.id) {
                                    if (buffer[buffer.size - 2] !is Dr1GoToLabelEntry || buffer.size <= 5) {
                                        //No else check
                                        output.add {
                                            repeat(indent) { append('\t') }
                                            append("checkFlag (")
                                            freeze(op.flagCheck.conditions()) { condition ->
                                                append("flagID(")
                                                append(condition.check shr 8)
                                                append(",")
                                                append(condition.check and 0xFF)
                                                append(") ")
                                                append(Dr1CheckFlagEntry.formatInvertedEquality(condition.op))
                                                append(" ")
                                                when (condition.value) {
                                                    0 -> append("false")
                                                    1 -> append("true")
                                                    else -> append(condition.value)
                                                }

                                                condition.extraConditions.forEach { extraCondition ->
                                                    append(" ")
                                                    append(Dr1CheckFlagEntry.formatInvertedLogical(extraCondition.logicalOp))
                                                    append(" flagID(")
                                                    append(extraCondition.check shr 8)
                                                    append(",")
                                                    append(extraCondition.check and 0xFF)
                                                    append(") ")
                                                    append(Dr1CheckFlagEntry.formatInvertedEquality(extraCondition.op))
                                                    append(" ")

                                                    when (extraCondition.value) {
                                                        0 -> append("false")
                                                        1 -> append("true")
                                                        else -> append(extraCondition.value)
                                                    }
                                                }
                                            }
                                            append(") {")
                                        }

                                        transpile(buffer.drop(3).dropLast(1), indent + 1)
                                        buffer.clear()

                                        output.add {
                                            repeat(indent) { append('\t') }
                                            append("}")
                                        }

                                        operation = null
                                    } else {
                                        op.whenTrueData = buffer.drop(3).dropLast(2)
                                        op.whenFalse = (buffer[buffer.size - 2] as Dr1GoToLabelEntry).id
                                        buffer.clear()
                                    }
                                }
                            }
                            op.whenFalse != null -> {
                                if (entry is Dr1MarkLabelEntry && op.whenFalse == entry.id) {
                                    output.add {
                                        repeat(indent) { append('\t') }
                                        append("checkFlag (")
                                        freeze(op.flagCheck.conditions()) { condition ->
                                            append("flagID(")
                                            append(condition.check shr 8)
                                            append(",")
                                            append(condition.check and 0xFF)
                                            append(") ")
                                            append(Dr1CheckFlagEntry.formatEquality(condition.op))
                                            append(" ")

                                            when (condition.value) {
                                                0 -> append("false")
                                                1 -> append("true")
                                                else -> append(condition.value)
                                            }

                                            condition.extraConditions.forEach { extraCondition ->
                                                append(" ")
                                                append(Dr1CheckFlagEntry.formatLogical(extraCondition.logicalOp))
                                                append(" flagID(")
                                                append(extraCondition.check shr 8)
                                                append(",")
                                                append(extraCondition.check and 0xFF)
                                                append(") ")
                                                append(Dr1CheckFlagEntry.formatEquality(extraCondition.op))
                                                append(" ")

                                                when (extraCondition.value) {
                                                    0 -> append("false")
                                                    1 -> append("true")
                                                    else -> append(extraCondition.value)
                                                }
                                            }
                                        }
                                        append(") {")
                                    }

                                    transpile(op.whenTrueData!!, indent + 1)

                                    output.add {
                                        repeat(indent) { append('\t') }
                                        append("} else {")
                                    }

                                    transpile(buffer.dropLast(1), indent + 1)
                                    buffer.clear()

                                    output.add {
                                        repeat(indent) { append('\t') }
                                        append("}")
                                    }

                                    operation = null
                                }
                            }
                        }
                    }
                    is TranspileOperation.CheckCharacterOrObject -> {
                        if (entry is Dr1CheckCharacterEntry || entry is Dr1CheckObjectEntry) {
                            output.add {
                                repeat(indent) { append('\t') }
                                if (op.checkCharacterEntry != null) {
                                    append("checkCharacter (")
                                    append(op.checkCharacterEntry.characterID)
                                } else {
                                    append("checkObject (")
                                    append(op.checkObjectEntry!!.objectID)
                                }
                                append(") {")
                            }

                            transpile(buffer.drop(1), indent + 1)
                            buffer.clear()

                            output.add {
                                repeat(indent) { append('\t') }
                                append("}")
                            }

                            operation = null
                            i--
                        } else {
                            buffer.add(entry)
                        }
                    }

                    is TranspileOperation.PresentSelection -> {
                        buffer.add(entry)

                        when {
                            op.entryLabel == null && entry is Dr1GoToLabelEntry -> op.entryLabel = entry
                            op.entryLabel != null && entry is Dr1BranchEntry -> {
                                if (op.branchBuffer.isNotEmpty()) {
                                    val branchNumber = (op.branchBuffer[0] as Dr1BranchEntry).branchValue
                                    op.branches[branchNumber] = op.branchBuffer.drop(1)
                                    op.branchBuffer.clear()
                                }

                                op.branchBuffer.add(entry)
                            }
                            op.entryLabel != null && entry is Dr1MarkLabelEntry && entry.id == op.entryLabel?.id -> {
                                output.add {
                                    repeat(indent) { append("\t") }
                                    append("selectPresent {")
                                }

                                op.branches.entries
                                    .sortedBy(Map.Entry<Int, *>::key)
                                    .forEach { (branchNum, branchBuffer) ->
                                        output.add {
                                            repeat(indent + 1) { append("\t") }

                                            val itemName = game?.linItemNames
                                                ?.getOrNull(branchNum)
                                                ?.lowercase()
                                                ?.replace(' ', '_')
                                                ?.replace(ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

                                            if (itemName != null) {
                                                val itemVariable = "item_$itemName"
                                                if (itemVariable !in variables)
                                                    variables[itemVariable] = RawNumberValue(branchNum)

                                                append('$')
                                                append(itemVariable)
                                            } else {
                                                append(branchNum)
                                            }

                                            append(" -> {")
                                        }

                                        transpile(branchBuffer, indent + 2)

                                        output.add {
                                            repeat(indent + 1) { append("\t") }
                                            append("}")
                                        }
                                    }

                                output.add {
                                    repeat(indent) { append("\t") }
                                    append("}")
                                }

                                buffer.clear()
                                operation = null
                            }
                            op.entryLabel != null && op.branchBuffer.isNotEmpty() -> op.branchBuffer.add(entry)
                            else -> {
                                buffer.removeAt(buffer.size - 1)
                                i--

                                buffer.dumpEntries(indent)
                                operation = null
                            }
                        }
                    }
                }

                Unit
            }
        }

        freeze(operation) { op ->
            when (op) {
                is TranspileOperation.CheckFlag -> {
                    when {
                        op.whenTrue != null && op.whenTrueData?.isNotEmpty() == true && buffer.isEmpty() -> {
                            //No else check
                            output.add {
                                repeat(indent) { append('\t') }
                                append("checkFlag (")
                                freeze(op.flagCheck.conditions()) { condition ->
                                    append("flagID(")
                                    append(condition.check shr 8)
                                    append(",")
                                    append(condition.check and 0xFF)
                                    append(") ")
                                    append(Dr1CheckFlagEntry.formatInvertedEquality(condition.op))
                                    append(" ")

                                    when (condition.value) {
                                        0 -> append("false")
                                        1 -> append("true")
                                        else -> append(condition.value)
                                    }

                                    condition.extraConditions.forEach { extraCondition ->
                                        append(" ")
                                        append(Dr1CheckFlagEntry.formatInvertedLogical(extraCondition.logicalOp))
                                        append(" flagID(")
                                        append(extraCondition.check shr 8)
                                        append(",")
                                        append(extraCondition.check and 0xFF)
                                        append(") ")
                                        append(Dr1CheckFlagEntry.formatInvertedEquality(extraCondition.op))
                                        append(" ")

                                        when (extraCondition.value) {
                                            0 -> append("false")
                                            1 -> append("true")
                                            else -> append(extraCondition.value)
                                        }
                                    }
                                }
                                append(") {")
                            }

                            transpile(op.whenTrueData!!, indent + 1)
                            buffer.clear()

                            output.add {
                                repeat(indent) { append('\t') }
                                append("}")
                            }

                            operation = null
                        }
                        op.whenTrue != null -> {
                            output.add {
                                repeat(indent) { append('\t') }
                                append("checkFlag (")
                                freeze(op.flagCheck.conditions()) { condition ->
                                    append("flagID(")
                                    append(condition.check shr 8)
                                    append(",")
                                    append(condition.check and 0xFF)
                                    append(") ")
                                    append(Dr1CheckFlagEntry.formatEquality(condition.op))
                                    append(" ")

                                    when (condition.value) {
                                        0 -> append("false")
                                        1 -> append("true")
                                        else -> append(condition.value)
                                    }

                                    condition.extraConditions.forEach { extraCondition ->
                                        append(" ")
                                        append(Dr1CheckFlagEntry.formatLogical(extraCondition.logicalOp))
                                        append(" flagID(")
                                        append(extraCondition.check shr 8)
                                        append(",")
                                        append(extraCondition.check and 0xFF)
                                        append(") ")
                                        append(Dr1CheckFlagEntry.formatEquality(extraCondition.op))
                                        append(" ")

                                        when (extraCondition.value) {
                                            0 -> append("false")
                                            1 -> append("true")
                                            else -> append(extraCondition.value)
                                        }
                                    }
                                }
                                append(") {")
                            }

                            transpile(op.whenTrueData!!, indent + 1)

                            output.add {
                                repeat(indent) { append('\t') }
                                append("} else {")
                            }

                            transpile(buffer.drop(1), indent + 1)
                            buffer.clear()

                            output.add {
                                repeat(indent) { append('\t') }
                                append("}")
                            }
                        }
                    }
                }
                else -> buffer.dumpEntries(indent)
            }
        }
    }
}

private fun MutableList<String>.add(block: StringBuilder.() -> Unit) {
    val builder = StringBuilder()
    builder.block()
    add(builder.toString())
}