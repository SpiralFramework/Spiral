package info.spiralframework.osb.common

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.games.*
import info.spiralframework.formats.common.get
import info.spiralframework.formats.common.scripting.lin.CustomLinScript
import info.spiralframework.formats.common.scripting.lin.UnknownLinEntry
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1VoiceLineEntry
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.OutputFlow

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class LinCompiler(val flow: OutputFlow, val game: DrGame.LinScriptable) : OpenSpiralBitcodeVisitor {
    val custom = CustomLinScript()
    val stack: MutableMap<String, OSLUnion> = HashMap()
    val registry: MutableMap<String, MutableList<SpiralFunction<OSLUnion>>> = HashMap()

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

    override suspend fun functionCall(functionName: String, parameters: Array<OSLUnion.FunctionParameterType>): OSLUnion? {
//        println("calling $functionName(${parameters.map { (name, value) -> if (name != null) "$name = ${stringify(value)}" else stringify(value) }.joinToString()})")

        val function = registry[functionName.toUpperCase().replace("_", "")]
                ?.firstOrNull { func -> func.parameterNames.size == parameters.size }
                ?: return null

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

        return function.suspendInvoke(passedParams)
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

    override suspend fun addVariableOpcode(opcode: Int, arguments: Array<OSLUnion>) =
            addPlainOpcode(opcode, collectArgs(arguments))

    override suspend fun addVariableOpcodeNamed(opcodeName: String, arguments: Array<OSLUnion>) =
            addPlainOpcodeNamed(opcodeName, collectArgs(arguments))

    private suspend fun MutableList<Int>.addUnion(union: OSLUnion) {
        when (union) {
            is OSLUnion.Int8NumberType -> add(union.number.toInt())
            is OSLUnion.Int16LENumberType -> {
                val num = union.number.toInt()
                add((num shr 0) and 0xFF)
                add((num shr 8) and 0xFF)
            }
            is OSLUnion.Int16BENumberType -> {
                val num = union.number.toInt()
                add((num shr 8) and 0xFF)
                add((num shr 0) and 0xFF)
            }
            is OSLUnion.Int32LENumberType -> {
                val num = union.number.toInt()
                add((num shr 0) and 0xFF)
                add((num shr 8) and 0xFF)
                add((num shr 16) and 0xFF)
                add((num shr 24) and 0xFF)
            }
            is OSLUnion.Int32BENumberType -> {
                val num = union.number.toInt()
                add((num shr 24) and 0xFF)
                add((num shr 16) and 0xFF)
                add((num shr 8) and 0xFF)
                add((num shr 0) and 0xFF)
            }
            is OSLUnion.NumberType -> add(union.number.toInt())
            is OSLUnion.StringType -> {
                val id = custom.addText(union.string)
                add((id shr 8) and 0xFF)
                add(id and 0xFF)
            }
            is OSLUnion.BooleanType -> add(if (union.boolean) 1 else 0)
            is OSLUnion.FunctionCallType -> functionCall(union.functionName, union.parameters)?.let { addUnion(it) }
            is OSLUnion.VariableReferenceType -> getData(union.variableName)?.let { addUnion(it) }
        }
    }
    private suspend fun collectArgs(arguments: Array<OSLUnion>): IntArray {
        val intArgs: MutableList<Int> = ArrayList()
        arguments.forEach { intArgs.addUnion(it) }
        return intArgs.toIntArray()
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
                is OSLUnion.FunctionCallType -> "${data.functionName}(${data.parameters.map { (name, value) -> if (name != null) "$name = ${stringify(value)}" else stringify(value) }.joinToString()})"
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
                is OSLUnion.FunctionCallType -> 0
                OSLUnion.UndefinedType -> 0
                OSLUnion.NullType -> 0
                OSLUnion.NoOpType -> 0
            }

    //    suspend fun speakStub(character: Any?, chapter: Any?, line: Any?, volume: Any?) = speak(speakerNameStub(character), intStub(chapter), intStub(line), intStub(volume))
//    suspend fun speak(character: Int, chapter: Int, line: Int, volume: Int) {
//        println("Voice File ID: ${game.getVoiceFileID(character, chapter, line)}")
//    }
    suspend fun speakerStub(voiceID: Any?, volume: Any?) = speak(intStub(voiceID), intStub(volume))

    suspend fun speak(voiceID: Int, volume: Int): OSLUnion.NoOpType = runNoOp {
        val (character, chapter, line) = game.getVoiceLineDetails(voiceID)
                ?: return@runNoOp println("(no voice line details for $voiceID)")
        custom.addEntry((Dr1VoiceLineEntry(character, chapter, line, volume)))
    }

    fun register(func: SpiralFunction<OSLUnion>) {
        val functions: MutableList<SpiralFunction<OSLUnion>>
        if (func.name !in registry) {
            functions = ArrayList()
            registry[func.name.toUpperCase().replace("_", "")] = functions
        } else {
            functions = registry.getValue(func.name.toUpperCase().replace("_", ""))
        }

        functions.add(func)
    }

    init {
        register(SpiralSuspending.Func2("speak", "voiceID", "volume", func = this::speakerStub))
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun OutputFlow.compileLinFromBitcode(context: SpiralContext, game: DrGame.LinScriptable, bitcode: InputFlow) {
    bitcode.parseOpenSpiralBitcode(context, LinCompiler(this, game))
}