package info.spiralframework.osb.common

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.games.*
import info.spiralframework.formats.common.get
import info.spiralframework.formats.common.scripting.lin.CustomLinScript
import info.spiralframework.formats.common.scripting.lin.UnknownLinEntry
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.OutputFlow
import kotlin.coroutines.Continuation
import kotlin.reflect.KCallable

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class LinCompiler(val flow: OutputFlow, val game: DrGame.LinScriptable) : OpenSpiralBitcodeVisitor {
    val custom = CustomLinScript()
    val stack: MutableMap<String, OSLUnion> = HashMap()
    val registry: MutableMap<String, MutableList<SpiralFunction>> = HashMap()

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

    override suspend fun addFunctionCall(functionName: String, parameters: Array<OSLUnion.FunctionParameterType>) {
        println("calling $functionName(${parameters.map { (name, value) -> if (name != null) "$name = ${stringify(value)}" else stringify(value) }.joinToString()})")

        val function = registry[functionName]
                ?.firstOrNull { func -> func.parameterNames.size == parameters.size }
                ?: return println("(function not found)")

        val functionParams = function.parameterNames.toMutableList()
        val passedParams: MutableMap<String, Any?> = HashMap()
        parameters.forEach { (name, value) ->
            val parameter = functionParams.firstOrNull { p -> p == name } ?: return@forEach
            passedParams[parameter] = value
            functionParams.remove(parameter)
        }
        parameters.forEach { (name, value) ->
            if (name != null) return@forEach
            passedParams[functionParams.removeAt(0)] = value
        }

        function.suspendInvoke(passedParams)
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
                is OSLUnion.NumberType -> data.number.toString()
                is OSLUnion.StringType -> data.string
                is OSLUnion.LongReferenceType -> data.longReference.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.VariableReferenceType -> getData(data.variableName)?.let { stringify(it) } ?: "null"
                is OSLUnion.LongLabelType -> data.longReference.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.LongParameterType -> data.longReference.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.ActionType -> data.actionName.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.BooleanType -> data.boolean.toString()
                is OSLUnion.FunctionParameterType -> if (data.parameterName != null) "${data.parameterName} = ${stringify(data.parameterValue)}" else stringify(data.parameterValue)
                OSLUnion.UndefinedType -> "undefined"
                OSLUnion.NullType -> "null"
                OSLUnion.NoOpType -> "NoOp"
            }

    override suspend fun end() {
        custom.compile(flow)
    }

    suspend fun speakerNameStub(param: Any?): Int =
            when (val union = requireNotNull(param) as OSLUnion) {
                is OSLUnion.StringType -> game.linCharacterIdentifiers[union.string] ?: union.string.toIntOrNull() ?: 0
                else -> intStub(union)
            }

    suspend fun stringStub(param: Any?): String = stringify(requireNotNull(param) as OSLUnion)
    suspend fun intStub(param: Any?): Int =
            when (val union = requireNotNull(param) as OSLUnion) {
                is OSLUnion.NumberType -> union.number.toInt()
                is OSLUnion.StringType -> union.string.toIntOrNull() ?: 0
                is OSLUnion.LongReferenceType -> union.longReference[0].toInt()
                is OSLUnion.VariableReferenceType -> intStub(getData(union.variableName))
                is OSLUnion.LongLabelType -> union.longReference[0].toInt()
                is OSLUnion.LongParameterType -> union.longReference[0].toInt()
                is OSLUnion.ActionType -> union.actionName[0].toInt()
                is OSLUnion.BooleanType -> if (union.boolean) 1 else 0
                is OSLUnion.FunctionParameterType -> intStub(union.parameterValue)
                OSLUnion.UndefinedType -> 0
                OSLUnion.NullType -> 0
                OSLUnion.NoOpType -> 0
            }

    suspend fun speakStub(character: Any?, chapter: Any?, line: Any?, volume: Any?) = speak(speakerNameStub(character), intStub(chapter), intStub(line), intStub(volume))
    suspend fun speak(character: Int, chapter: Int, line: Int, volume: Int) {
        println("Voice File ID: ${game.getVoiceFileID(character, chapter, line)}")
    }
    suspend fun speakerStub(voiceID: Any?, volume: Any?) = speak(intStub(voiceID), intStub(volume))
    suspend fun speak(voiceID: Int, volume: Int) {
        println("Character / Chapter / ID: ${game.getVoiceLineDetails(voiceID)}")
    }

    fun register(func: SpiralFunction) {
        val functions: MutableList<SpiralFunction>
        if (func.name !in registry) {
            functions = ArrayList()
            registry[func.name] = functions
        } else {
            functions = registry.getValue(func.name)
        }

        functions.add(func)
    }

    init {
        register(SpiralSuspending.Func4("speak", "character", "chapter", "line", "volume", func = this::speakStub))
        register(SpiralSuspending.Func2("speak", "voiceID", "volume", func = this::speakerStub))
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun OutputFlow.compileLinFromBitcode(context: SpiralContext, game: DrGame.LinScriptable, bitcode: InputFlow) {
    bitcode.parseOpenSpiralBitcode(context, LinCompiler(this, game))
}