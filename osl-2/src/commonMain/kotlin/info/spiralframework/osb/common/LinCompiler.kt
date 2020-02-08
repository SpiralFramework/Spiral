package info.spiralframework.osb.common

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.games.*
import info.spiralframework.formats.common.get
import info.spiralframework.formats.common.scripting.lin.CustomLinScript
import info.spiralframework.formats.common.scripting.lin.UnknownLinEntry
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.OutputFlow

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class LinCompiler(val flow: OutputFlow, val game: DrGame.LinScriptable) : OpenSpiralBitcodeVisitor {
    val custom = CustomLinScript()
    val stack: MutableMap<String, OSLUnion> = HashMap()

    var failOnMissingCharacter = false

    override suspend fun setVersion(version: SemanticVersion) {}
    override suspend fun addDialogue(speakerName: String, dialogue: OSLUnion) {
        val speakerID = game.linCharacterIdentifiers[speakerName]
                ?: return if (failOnMissingCharacter) throw IllegalArgumentException("No speaker $speakerName") else Unit

        addDialogue(speakerID, dialogue)
    }

    override suspend fun addDialogue(speaker: Int, dialogue: OSLUnion) {
        val speakerEntry = game.SpeakerEntry(speaker) ?: return
        val textEntry = game.TextEntry(custom.addText(stringify(dialogue))) ?: return
        val waitFrame = game.WaitFrame() ?: return
        val waitForInput = game.WaitForInput() ?: return

        //TODO: Add something here to not add a million speaker entries maybe?

        custom.addEntry(speakerEntry)
        custom.addEntry(textEntry)
        custom.addEntry(waitFrame)
        custom.addEntry(waitForInput)
    }

    override suspend fun addPlainOpcode(opcode: Int, arguments: IntArray) =
            custom.addEntry(
                    game.linOpcodeMap[opcode]?.entryConstructor?.invoke(opcode, arguments)
                            ?: UnknownLinEntry(opcode, arguments)
            )

    override suspend fun addPlainOpcodeNamed(opcodeName: String, arguments: IntArray) {
        val opcode = game.linOpcodeMap[opcodeName] ?: return
        custom.addEntry(opcode.entryConstructor(opcode.opcode, arguments))
    }

    override suspend fun addVariableOpcode(opcode: Int, arguments: Array<OSLUnion>) {
        val intArgs: MutableList<Int> = ArrayList()
        arguments.forEach { arg ->
            when (arg) {
                is OSLUnion.NumberType -> intArgs.add(arg.number.toInt())
                is OSLUnion.StringType -> {
                    val id = custom.addText(arg.string)
                    intArgs.add((id shr 8) and 0xFF)
                    intArgs.add(id and 0xFF)
                }
                is OSLUnion.BooleanType -> intArgs.add(if (arg.boolean) 1 else 0)
            }
        }

        addPlainOpcode(opcode, intArgs.toIntArray())
    }

    override suspend fun addVariableOpcodeNamed(opcodeName: String, arguments: Array<OSLUnion>) {
        val intArgs: MutableList<Int> = ArrayList()
        arguments.forEach { arg ->
            when (arg) {
                is OSLUnion.NumberType -> intArgs.add(arg.number.toInt())
                is OSLUnion.StringType -> {
                    val id = custom.addText(arg.string)
                    intArgs.add((id shr 8) and 0xFF)
                    intArgs.add(id and 0xFF)
                }
                is OSLUnion.BooleanType -> intArgs.add(if (arg.boolean) 1 else 0)
            }
        }

        addPlainOpcodeNamed(opcodeName, intArgs.toIntArray())
    }

    override suspend fun getData(name: String): OSLUnion? = stack[name]
    override suspend fun setData(name: String, data: OSLUnion) {
        stack[name] = data
    }

    override suspend fun colourCodeFor(clt: String): String? = game.linColourCodes[clt]?.toString()

    override suspend fun closeLongReference(): String? = null
    override suspend fun stringify(data: OSLUnion): String =
            when (data) {
                is OSLUnion.NumberType -> data.toString()
                is OSLUnion.StringType -> data.string
                is OSLUnion.LongReferenceType -> data.longReference.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.VariableReferenceType -> getData(data.variableName)?.let { stringify(it) } ?: "null"
                is OSLUnion.LongLabelType -> data.longReference.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.LongParameterType -> data.longReference.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.ActionType -> data.actionName.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.BooleanType -> data.boolean.toString()
                OSLUnion.UndefinedType -> "undefined"
                OSLUnion.NullType -> "null"
                OSLUnion.NoOpType -> "NoOp"
            }

    override suspend fun end() {
        custom.compile(flow)
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun OutputFlow.compileLinFromBitcode(context: SpiralContext, game: DrGame.LinScriptable, bitcode: InputFlow) {
    bitcode.parseOpenSpiralBitcode(context, LinCompiler(this, game))
}