package info.spiralframework.osb.common

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.games.*
import info.spiralframework.formats.common.get
import info.spiralframework.formats.common.scripting.lin.CustomLinScript
import info.spiralframework.formats.common.scripting.lin.UnknownLinEntry
import info.spiralframework.formats.common.scripting.lin.dr1.*
import info.spiralframework.osb.common.OpenSpiralBitcode.EQUALITY_EQUAL
import info.spiralframework.osb.common.OpenSpiralBitcode.EQUALITY_GREATER_THAN
import info.spiralframework.osb.common.OpenSpiralBitcode.EQUALITY_GREATER_THAN_EQUAL_TO
import info.spiralframework.osb.common.OpenSpiralBitcode.EQUALITY_LESS_THAN
import info.spiralframework.osb.common.OpenSpiralBitcode.EQUALITY_LESS_THAN_EQUAL_TO
import info.spiralframework.osb.common.OpenSpiralBitcode.EQUALITY_NOT_EQUAL
import info.spiralframework.osb.common.OpenSpiralBitcode.LOGICAL_AND
import info.spiralframework.osb.common.OpenSpiralBitcode.LOGICAL_OR
import info.spiralframework.osb.common.OpenSpiralBitcode.TREE_TYPE_PRESENT_SELECTION
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.OutputFlow

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
open class LinCompiler protected constructor(val flow: OutputFlow, val game: DrGame.LinScriptable) : OpenSpiralBitcodeVisitor {
    companion object {
        operator fun invoke(flow: OutputFlow, game: DrGame.LinScriptable): LinCompiler =
                when (game) {
                    is Dr1 -> Dr1LinCompiler(flow, game)
                    else -> {
                        val compiler = LinCompiler(flow, game)
                        compiler.registerDefaults()
                        compiler
                    }
                }
    }

    val custom = CustomLinScript()
    val stack: MutableMap<String, OSLUnion> = HashMap()
    val registry: MutableMap<String, MutableList<SpiralFunction<OSLUnion>>> = HashMap()

    var failOnMissingCharacter = false

    override suspend fun setVersion(context: SpiralContext, version: SemanticVersion) {}
    override suspend fun addDialogue(context: SpiralContext, speakerName: String, dialogue: OSLUnion) {
        val speakerID = game.linCharacterIdentifiers[speakerName]
                ?: return if (failOnMissingCharacter) throw IllegalArgumentException("No speaker $speakerName") else Unit

        addDialogue(context, speakerID, dialogue)
    }

    override suspend fun addDialogue(context: SpiralContext, speaker: Int, dialogue: OSLUnion) {
        val speakerEntry = game.SpeakerEntry(speaker) ?: return
        val textEntry = game.TextEntry(custom.addText(stringify(context, dialogue))) ?: return
        val waitFrame = game.WaitFrame() ?: return
        val waitForInput = game.WaitForInput() ?: return

        //TODO: Add something here to not add a million speaker entries maybe?

        custom.addEntry(speakerEntry)
        custom.addEntry(textEntry)
        custom.addEntry(waitFrame)
        custom.addEntry(waitForInput)
    }

    override suspend fun functionCall(context: SpiralContext, functionName: String, parameters: Array<OSLUnion.FunctionParameterType>): OSLUnion? {
        with(context) {
            trace("calling $functionName(${parameters.map { (name, value) -> if (name != null) "$name = ${stringify(this, value)}" else stringify(this, value) }.joinToString()})")
        }

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

        return function.suspendInvoke(context, passedParams)
    }

    override suspend fun addPlainOpcode(context: SpiralContext, opcode: Int, arguments: IntArray) =
            custom.addEntry(
                    game.linOpcodeMap[opcode]?.entryConstructor?.invoke(opcode, arguments)
                            ?: UnknownLinEntry(opcode, arguments)
            )

    override suspend fun addPlainOpcodeNamed(context: SpiralContext, opcodeName: String, arguments: IntArray) {
        val opcode = game.linOpcodeMap[opcodeName] ?: return
        custom.addEntry(opcode.entryConstructor(opcode.opcode, arguments))
    }

    override suspend fun addVariableOpcode(context: SpiralContext, opcode: Int, arguments: Array<OSLUnion>) =
            addPlainOpcode(context, opcode, collectArgs(context, arguments))

    override suspend fun addVariableOpcodeNamed(context: SpiralContext, opcodeName: String, arguments: Array<OSLUnion>) =
            addPlainOpcodeNamed(context, opcodeName, collectArgs(context, arguments))

    override suspend fun addFlagCheck(context: SpiralContext, mainBranch: OpenSpiralBitcodeFlagBranch, elseIfBranches: Array<OpenSpiralBitcodeFlagBranch>, elseBranch: ByteArray?, level: Int): Unit =
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    override suspend fun addTree(context: SpiralContext, treeType: Int, scope: ByteArray, level: Int): Unit =
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    protected suspend fun MutableList<Int>.addUnion(context: SpiralContext, union: OSLUnion) {
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
            is OSLUnion.Int24LENumberType -> {
                val num = union.number.toInt()
                add((num shr 0) and 0xFF)
                add((num shr 8) and 0xFF)
                add((num shr 16) and 0xFF)
            }
            is OSLUnion.Int24BENumberType -> {
                val num = union.number.toInt()
                add((num shr 16) and 0xFF)
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
            is OSLUnion.FunctionCallType -> functionCall(context, union.functionName, union.parameters)?.let { addUnion(context, it) }
            is OSLUnion.VariableReferenceType -> getData(context, union.variableName)?.let { addUnion(context, it) }
        }
    }

    protected suspend fun MutableList<Int>.addUnionInt8(context: SpiralContext, union: OSLUnion) {
        when (union) {
            is OSLUnion.NumberType -> add(union.number.toInt())
            is OSLUnion.StringType -> {
                val id = custom.addText(union.string)
                add((id shr 8) and 0xFF)
                add(id and 0xFF)
            }
            is OSLUnion.BooleanType -> {
                add(if (union.boolean) 1 else 0)
            }
            is OSLUnion.FunctionCallType -> addUnionInt8(context, functionCall(context, union.functionName, union.parameters)
                    ?: OSLUnion.NullType)
            is OSLUnion.VariableReferenceType -> addUnionInt8(context, getData(context, union.variableName)
                    ?: OSLUnion.NullType)
            else -> add(0)
        }
    }

    protected suspend fun MutableList<Int>.addUnionInt16LE(context: SpiralContext, union: OSLUnion) {
        when (union) {
            is OSLUnion.Int8NumberType -> {
                add(union.number.toInt())
                add(0)
            }
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
            is OSLUnion.Int24LENumberType -> {
                val num = union.number.toInt()
                add((num shr 0) and 0xFF)
                add((num shr 8) and 0xFF)
            }
            is OSLUnion.Int24BENumberType -> {
                val num = union.number.toInt()
                add((num shr 8) and 0xFF)
                add((num shr 0) and 0xFF)
            }
            is OSLUnion.Int32LENumberType -> {
                val num = union.number.toInt()
                add((num shr 0) and 0xFF)
                add((num shr 8) and 0xFF)
            }
            is OSLUnion.Int32BENumberType -> {
                val num = union.number.toInt()
                add((num shr 8) and 0xFF)
                add((num shr 0) and 0xFF)
            }
            is OSLUnion.NumberType -> {
                val num = union.number.toInt()
                add((num shr 0) and 0xFF)
                add((num shr 8) and 0xFF)
            }
            is OSLUnion.StringType -> {
                val id = custom.addText(union.string)
                add((id shr 8) and 0xFF)
                add(id and 0xFF)
            }
            is OSLUnion.BooleanType -> {
                add(if (union.boolean) 1 else 0)
                add(0)
            }
            is OSLUnion.FunctionCallType -> addUnionInt16LE(context, functionCall(context, union.functionName, union.parameters)
                    ?: OSLUnion.NullType)
            is OSLUnion.VariableReferenceType -> addUnionInt16LE(context, getData(context, union.variableName)
                    ?: OSLUnion.NullType)
            else -> {
                add(0)
                add(0)
            }
        }
    }

    protected suspend fun MutableList<Int>.addUnionInt16BE(context: SpiralContext, union: OSLUnion) {
        when (union) {
            is OSLUnion.Int8NumberType -> {
                add(0)
                add(union.number.toInt())
            }
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
            is OSLUnion.Int24LENumberType -> {
                val num = union.number.toInt()
                add((num shr 0) and 0xFF)
                add((num shr 8) and 0xFF)
            }
            is OSLUnion.Int24BENumberType -> {
                val num = union.number.toInt()
                add((num shr 8) and 0xFF)
                add((num shr 0) and 0xFF)
            }
            is OSLUnion.Int32LENumberType -> {
                val num = union.number.toInt()
                add((num shr 0) and 0xFF)
                add((num shr 8) and 0xFF)
            }
            is OSLUnion.Int32BENumberType -> {
                val num = union.number.toInt()
                add((num shr 8) and 0xFF)
                add((num shr 0) and 0xFF)
            }
            is OSLUnion.NumberType -> {
                val num = union.number.toInt()
                add((num shr 8) and 0xFF)
                add((num shr 0) and 0xFF)
            }
            is OSLUnion.StringType -> {
                val id = custom.addText(union.string)
                add((id shr 8) and 0xFF)
                add(id and 0xFF)
            }
            is OSLUnion.BooleanType -> {
                add(0)
                add(if (union.boolean) 1 else 0)
            }
            is OSLUnion.FunctionCallType -> addUnionInt16LE(context, functionCall(context, union.functionName, union.parameters)
                    ?: OSLUnion.NullType)
            is OSLUnion.VariableReferenceType -> addUnionInt16LE(context, getData(context, union.variableName)
                    ?: OSLUnion.NullType)
            else -> {
                add(0)
                add(0)
            }
        }
    }

    protected suspend fun collectArgs(context: SpiralContext, arguments: Array<OSLUnion>): IntArray {
        val intArgs: MutableList<Int> = ArrayList()
        arguments.forEach { intArgs.addUnion(context, it) }
        return intArgs.toIntArray()
    }

    override suspend fun getData(context: SpiralContext, name: String): OSLUnion? = stack[name]
    override suspend fun setData(context: SpiralContext, name: String, data: OSLUnion) {
        stack[name] = data
    }

    override suspend fun colourCodeFor(context: SpiralContext, clt: String): String? = game.linColourCodes[clt]?.toString()

    override suspend fun closeLongReference(context: SpiralContext): String? = null
    override suspend fun stringify(context: SpiralContext, data: OSLUnion): String =
            when (data) {
                is OSLUnion.NumberType -> data.number.toString()
                is OSLUnion.StringType -> data.string
                is OSLUnion.LongReferenceType -> data.longReference.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.VariableReferenceType -> getData(context, data.variableName)?.let { stringify(context, it) }
                        ?: "null"
                is OSLUnion.LongLabelType -> data.longReference.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.LongParameterType -> data.longReference.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.ActionType -> data.actionName.joinToString(" ") { it.toString(16).padStart(2, '0') }
                is OSLUnion.BooleanType -> data.boolean.toString()
                is OSLUnion.FunctionParameterType -> if (data.parameterName != null) "${data.parameterName} = ${stringify(context, data.parameterValue)}" else stringify(context, data.parameterValue)
                is OSLUnion.FunctionCallType -> "${data.functionName}(${data.parameters.map { (name, value) -> if (name != null) "$name = ${stringify(context, value)}" else stringify(context, value) }.joinToString()})"
                OSLUnion.UndefinedType -> "undefined"
                OSLUnion.NullType -> "null"
                OSLUnion.NoOpType -> "NoOp"
            }

    override suspend fun end(context: SpiralContext) {
        custom.compile(flow)
    }

    suspend fun speakerNameStub(context: SpiralContext, param: Any?): Int =
            when (val union = requireNotNull(param) as OSLUnion) {
                is OSLUnion.StringType -> game.linCharacterIdentifiers[union.string] ?: union.string.toIntOrNull() ?: 0
                else -> intStub(context, union)
            }

    suspend fun stringStub(context: SpiralContext, param: Any?): String =
            when (val union = requireNotNull(param) as OSLUnion) {
                is OSLUnion.FunctionCallType -> stringStub(context, functionCall(context, union.functionName, union.parameters)
                        ?: OSLUnion.UndefinedType)
                else -> stringify(context, union)
            }

    suspend fun intStub(context: SpiralContext, param: Any?): Int =
            when (val union = requireNotNull(param) as OSLUnion) {
                is OSLUnion.NumberType -> union.number.toInt()
                is OSLUnion.StringType -> union.string.toIntOrNull() ?: 0
                is OSLUnion.LongReferenceType -> union.longReference[0].toInt()
                is OSLUnion.VariableReferenceType -> intStub(context, getData(context, union.variableName))
                is OSLUnion.LongLabelType -> union.longReference[0].toInt()
                is OSLUnion.LongParameterType -> union.longReference[0].toInt()
                is OSLUnion.ActionType -> union.actionName[0].toInt()
                is OSLUnion.BooleanType -> if (union.boolean) 1 else 0
                is OSLUnion.FunctionParameterType -> intStub(context, union.parameterValue)
                is OSLUnion.FunctionCallType -> intStub(context, (functionCall(context, union.functionName, union.parameters)
                        ?: OSLUnion.UndefinedType))
                OSLUnion.UndefinedType -> 0
                OSLUnion.NullType -> 0
                OSLUnion.NoOpType -> 0
            }

    suspend fun fromInt16BEStub(context: SpiralContext, num: Any?) = fromInt16BE(intStub(context, num))
    suspend fun fromInt16BE(num: Int) = OSLUnion.Int16BENumberType(num)

    suspend fun fromInt16LEStub(context: SpiralContext, num: Any?) = fromInt16LE(intStub(context, num))
    suspend fun fromInt16LE(num: Int) = OSLUnion.Int16LENumberType(num)

    suspend fun toInt16BEStub(context: SpiralContext, a: Any?, b: Any?) = toInt16BE(intStub(context, a), intStub(context, b))
    suspend fun toInt16BE(a: Int, b: Int) = OSLUnion.Int16BENumberType(b or (a shl 8))

    suspend fun toInt16LEStub(context: SpiralContext, a: Any?, b: Any?) = toInt16LE(intStub(context, a), intStub(context, b))
    suspend fun toInt16LE(a: Int, b: Int) = OSLUnion.Int16LENumberType(a or (b shl 8))

    suspend fun fromInt24BEStub(context: SpiralContext, num: Any?) = fromInt24BE(intStub(context, num))
    suspend fun fromInt24BE(num: Int) = OSLUnion.Int24BENumberType(num)

    suspend fun fromInt24LEStub(context: SpiralContext, num: Any?) = fromInt24LE(intStub(context, num))
    suspend fun fromInt24LE(num: Int) = OSLUnion.Int24LENumberType(num)

    fun register(func: SpiralFunction<OSLUnion>) {
        val functions: MutableList<SpiralFunction<OSLUnion>>
        if (func.name.toUpperCase().replace("_", "") !in registry) {
            functions = ArrayList()
            registry[func.name.toUpperCase().replace("_", "")] = functions
        } else {
            functions = registry.getValue(func.name.toUpperCase().replace("_", ""))
        }

        functions.add(func)
    }

    open fun registerDefaults() {
        register(SpiralSuspending.Func1("int16LE", "num", func = this::fromInt16LEStub))
        register(SpiralSuspending.Func1("int16BE", "num", func = this::fromInt16BEStub))
        register(SpiralSuspending.Func1("int24LE", "num", func = this::fromInt24LEStub))
        register(SpiralSuspending.Func1("int24BE", "num", func = this::fromInt24BEStub))

        register(SpiralSuspending.Func2("int16LE", "a", "b", func = this::toInt16LEStub))
        register(SpiralSuspending.Func2("int16BE", "a", "b", func = this::toInt16BEStub))
//        register(SpiralSuspending.Func2("int24LE", "num", func = this::fromInt24LEStub))
//        register(SpiralSuspending.Func2("int24BE", "num", func = this::fromInt24BEStub))

        register(SpiralSuspending.Func2("flagID", "group", "id", func = this::toInt16BEStub))
        register(SpiralSuspending.Func1("rgb", "num", func = this::fromInt24BEStub))
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
open class Dr1LinCompiler private constructor(flow: OutputFlow, game: Dr1) : LinCompiler(flow, game) {
    val labelsUsed: MutableList<Int> = ArrayList()
    var lastLabelUsed: Int = 0xFFFF //By starting at the end, we give ourselves the best chance of avoiding conflicts

    companion object {
        operator fun invoke(flow: OutputFlow, game: Dr1): LinCompiler {
            val compiler = Dr1LinCompiler(flow, game)
            compiler.registerDefaults()
            return compiler
        }
    }

    fun findLabel(): Int? {
        while (lastLabelUsed in labelsUsed) {
            lastLabelUsed--
        }
        labelsUsed.add(lastLabelUsed)
        return lastLabelUsed
    }

    override suspend fun addFlagCheck(context: SpiralContext, mainBranch: OpenSpiralBitcodeFlagBranch, elseIfBranches: Array<OpenSpiralBitcodeFlagBranch>, elseBranch: ByteArray?, level: Int) {
        val joiningLabel = requireNotNull(findLabel())

        if (elseIfBranches.isEmpty() && elseBranch == null) {
            //This just makes the resulting lin a bit nicer
            val mainBranchArgs: MutableList<Int> = ArrayList()
            mainBranchArgs.addUnionInt16BE(context, mainBranch.condition.checking)
            mainBranchArgs.add(toInvertedFlagCheckEqualityOperator(mainBranch.condition.operation))
            mainBranchArgs.addUnionInt8(context, mainBranch.condition.against)
            mainBranch.otherConditions.forEach { (logical, condition) ->
                mainBranchArgs.add(toInvertedFlagCheckLogicalOperator(logical))
                mainBranchArgs.addUnionInt16BE(context, condition.checking)
                mainBranchArgs.add(toInvertedFlagCheckEqualityOperator(condition.operation))
                mainBranchArgs.addUnionInt8(context, condition.against)
            }
            custom.addEntry(Dr1CheckFlagEntry(mainBranchArgs.toIntArray()))
            custom.addEntry(Dr1EndFlagCheckEntry())
            custom.addEntry(Dr1GoToLabelEntry(joiningLabel))
            OpenSpiralBitcodeParser(BinaryInputFlow(mainBranch.branch), this, level)
                    .parse(context)
        } else {
            val mainBranchArgs: MutableList<Int> = ArrayList()
            mainBranchArgs.addUnionInt16BE(context, mainBranch.condition.checking)
            mainBranchArgs.add(toFlagCheckEqualityOperator(mainBranch.condition.operation))
            mainBranchArgs.addUnionInt8(context, mainBranch.condition.against)
            mainBranch.otherConditions.forEach { (logical, condition) ->
                mainBranchArgs.add(toFlagCheckLogicalOperator(logical))
                mainBranchArgs.addUnionInt16BE(context, condition.checking)
                mainBranchArgs.add(toFlagCheckEqualityOperator(condition.operation))
                mainBranchArgs.addUnionInt8(context, condition.against)
            }
            custom.addEntry(Dr1CheckFlagEntry(mainBranchArgs.toIntArray()))
            custom.addEntry(Dr1EndFlagCheckEntry())

            val mainBranchLabel = requireNotNull(findLabel())
            custom.addEntry(Dr1GoToLabelEntry(mainBranchLabel))
            val elseIfBranchLabels = IntArray(elseIfBranches.size) { requireNotNull(findLabel()) }
            elseIfBranches.forEachIndexed { index, branch ->
                val branchArgs: MutableList<Int> = ArrayList()
                branchArgs.addUnionInt16LE(context, branch.condition.checking)
                branchArgs.add(toFlagCheckEqualityOperator(branch.condition.operation))
                branchArgs.addUnionInt8(context, branch.condition.against)
                branch.otherConditions.forEach { (logical, condition) ->
                    branchArgs.add(toFlagCheckLogicalOperator(logical))
                    branchArgs.addUnionInt16BE(context, condition.checking)
                    branchArgs.add(toFlagCheckEqualityOperator(condition.operation))
                    branchArgs.addUnionInt8(context, condition.against)
                }
                custom.addEntry(Dr1CheckFlagEntry(branchArgs.toIntArray()))
                custom.addEntry(Dr1EndFlagCheckEntry())
                custom.addEntry(Dr1GoToLabelEntry(elseIfBranchLabels[index]))
            }

            if (elseBranch != null) {
                OpenSpiralBitcodeParser(BinaryInputFlow(elseBranch), this, level)
                        .parse(context)
            }
            custom.addEntry(Dr1GoToLabelEntry(joiningLabel))

            elseIfBranches.indices.reversed().forEach { index ->
                custom.addEntry(Dr1MarkLabelEntry(elseIfBranchLabels[index]))
                OpenSpiralBitcodeParser(BinaryInputFlow(elseIfBranches[index].branch), this, level)
                        .parse(context)
                custom.addEntry(Dr1GoToLabelEntry(joiningLabel))
            }

            custom.addEntry(Dr1MarkLabelEntry(mainBranchLabel))
            OpenSpiralBitcodeParser(BinaryInputFlow(mainBranch.branch), this, level)
                    .parse(context)
        }

        custom.addEntry(Dr1MarkLabelEntry(joiningLabel))
    }

    override suspend fun addTree(context: SpiralContext, treeType: Int, scope: ByteArray, level: Int) {
        when (treeType) {
            TREE_TYPE_PRESENT_SELECTION -> {
                val label = requireNotNull(findLabel())
                custom.addEntry(Dr1ChangeUIEntry(Dr1ChangeUIEntry.PRESENT_SELECTION, true))
                custom.addEntry(Dr1GoToLabelEntry(label))
                OpenSpiralBitcodeParser(BinaryInputFlow(scope), this, level + 1)
                        .parse(context)
                custom.addEntry(Dr1BranchEntry(255))
                custom.addEntry(Dr1MarkLabelEntry(label))
            }
        }
    }

    /**
     *     0 -> "!="
    1 -> "=="
    2 -> "<="
    3 -> ">="
    4 -> "<"
    5 -> ">"
     */
    suspend fun toFlagCheckEqualityOperator(operation: Int): Int =
            when (operation) {
                EQUALITY_EQUAL -> Dr1CheckFlagEntry.EQUALITY_EQUAL
                EQUALITY_NOT_EQUAL -> Dr1CheckFlagEntry.EQUALITY_NOT_EQUAL
                EQUALITY_LESS_THAN -> Dr1CheckFlagEntry.EQUALITY_LESS_THAN
                EQUALITY_GREATER_THAN -> Dr1CheckFlagEntry.EQUALITY_GREATER_THAN
                EQUALITY_LESS_THAN_EQUAL_TO -> Dr1CheckFlagEntry.EQUALITY_LESS_THAN_EQUAL_TO
                EQUALITY_GREATER_THAN_EQUAL_TO -> Dr1CheckFlagEntry.EQUALITY_GREATER_THAN_EQUAL_TO
                else -> 0
            }

    /**
     * 6 -> "||"
     * 7 -> "&&"
     */
    suspend fun toFlagCheckLogicalOperator(operation: Int): Int =
            when (operation) {
                LOGICAL_OR -> Dr1CheckFlagEntry.LOGICAL_OR
                LOGICAL_AND -> Dr1CheckFlagEntry.LOGICAL_AND
                else -> 6
            }

    suspend fun toInvertedFlagCheckLogicalOperator(operation: Int): Int =
            when (operation) {
                LOGICAL_AND -> Dr1CheckFlagEntry.LOGICAL_OR //AND -> OR
                LOGICAL_OR -> Dr1CheckFlagEntry.LOGICAL_AND //OR -> AND
                else -> 6
            }

    suspend fun toInvertedFlagCheckEqualityOperator(operation: Int): Int =
            when (operation) {
                EQUALITY_EQUAL -> Dr1CheckFlagEntry.EQUALITY_NOT_EQUAL //Equal -> Not Equal
                EQUALITY_NOT_EQUAL -> Dr1CheckFlagEntry.EQUALITY_EQUAL //Not Equal -> Equal
                EQUALITY_LESS_THAN -> Dr1CheckFlagEntry.EQUALITY_GREATER_THAN_EQUAL_TO //Less Than -> Greater than or Equal To
                EQUALITY_GREATER_THAN -> Dr1CheckFlagEntry.EQUALITY_LESS_THAN_EQUAL_TO //Greater Than -> Less Than or Equal To
                EQUALITY_LESS_THAN_EQUAL_TO -> Dr1CheckFlagEntry.EQUALITY_GREATER_THAN //Less Than Equal To -> Greater Than
                EQUALITY_GREATER_THAN_EQUAL_TO -> Dr1CheckFlagEntry.EQUALITY_LESS_THAN //Greater Than Equal To -> Less Than
                else -> 0
            }

    suspend fun textStub(context: SpiralContext, text: Any?) = text(stringStub(context, text))
    suspend fun text(text: String) = runNoOp {
        val textEntry = game.TextEntry(custom.addText(text)) ?: return@runNoOp
        val waitFrame = game.WaitFrame() ?: return@runNoOp
        val waitForInput = game.WaitForInput() ?: return@runNoOp

        custom.addEntry(textEntry)
        custom.addEntry(waitFrame)
        custom.addEntry(waitForInput)
    }

    suspend fun speakStub(context: SpiralContext, voiceID: Any?, volume: Any?) = speak(intStub(context, voiceID), intStub(context, volume))
    suspend fun speak(voiceID: Int, volume: Int): OSLUnion.NoOpType = runNoOp {
        val (character, chapter, line) = game.getVoiceLineDetails(voiceID)
                ?: return@runNoOp println("(no voice line details for $voiceID)")
        custom.addEntry((Dr1VoiceLineEntry(character, chapter, line, volume)))
    }

    suspend fun cameraFocusStub(context: SpiralContext, character: Any?) = cameraFocus(intStub(context, character))
    suspend fun cameraFocus(character: Int) = runNoOp {
        custom.addEntry(Dr1ChangeUIEntry(26, character))
    }

    suspend fun showItemStub(context: SpiralContext, id: Any?) = showItem(intStub(context, id))
    suspend fun showItem(id: Int) = runNoOp {
        custom.addEntry(Dr1AnimationEntry(2000 + id, 0, 0, 0, 0, 0, 1))
    }

    suspend fun hideItemStub(context: SpiralContext, id: Any?) = hideItem(intStub(context, id))
    suspend fun hideItem(id: Int) = runNoOp {
        custom.addEntry(Dr1AnimationEntry(2000 + id, 0, 0, 0, 0, 0, 2))
    }

    suspend fun showCutinStub(context: SpiralContext, id: Any?) = showCutin(intStub(context, id))
    suspend fun showCutin(id: Int) = runNoOp {
        custom.addEntry(Dr1AnimationEntry(3000 + id, 0, 0, 0, 0, 0, 1))
    }

    suspend fun hideCutinStub(context: SpiralContext, id: Any?) = hideCutin(intStub(context, id))
    suspend fun hideCutin(id: Int) = runNoOp {
        custom.addEntry(Dr1AnimationEntry(3000 + id, 0, 0, 0, 0, 0, 2))
    }

    suspend fun disableUIStub(context: SpiralContext, element: Any?) = disableUI(intStub(context, element))
    suspend fun disableUI(element: Int) = runNoOp {
        custom.addEntry(Dr1ChangeUIEntry(element, 0))
    }

    suspend fun enableUIStub(context: SpiralContext, element: Any?) = enableUI(intStub(context, element))
    suspend fun enableUI(element: Int) = runNoOp {
        custom.addEntry(Dr1ChangeUIEntry(element, 1))
    }

    //    suspend fun disableFlagStub(context: SpiralContext, flagID: Any?) = enableFlag(intStub(context, flagID))
    suspend fun disableFlagStub(context: SpiralContext, flagGroup: Any?, flagID: Any?) = enableFlag(intStub(context, flagGroup), intStub(context, flagID))

    suspend fun disableFlag(flagGroup: Int, flagID: Int) = runNoOp {
        custom.addEntry(Dr1SetFlagEntry(flagGroup, flagID, 0))
    }

    //    suspend fun enableFlagStub(context: SpiralContext, flagID: Any?) = enableFlag(intStub(context, flagID))
    suspend fun enableFlagStub(context: SpiralContext, flagGroup: Any?, flagID: Any?) = enableFlag(intStub(context, flagGroup), intStub(context, flagID))

    suspend fun enableFlag(flagGroup: Int, flagID: Int) = runNoOp {
        custom.addEntry(Dr1SetFlagEntry(flagGroup, flagID, 1))
    }

    suspend fun fadeInFromBlackStub(context: SpiralContext, frameCount: Any?) = fadeInFromBlack(intStub(context, frameCount))
    suspend fun fadeInFromBlack(frameCount: Int) = runNoOp {
        custom.addEntry(Dr1ScreenFadeEntry(true, Dr1ScreenFadeEntry.FADE_COLOUR_BLACK, frameCount))
    }

    suspend fun fadeOutToBlackStub(context: SpiralContext, frameCount: Any?) = fadeOutToBlack(intStub(context, frameCount))
    suspend fun fadeOutToBlack(frameCount: Int) = runNoOp {
        custom.addEntry(Dr1ScreenFadeEntry(false, Dr1ScreenFadeEntry.FADE_COLOUR_BLACK, frameCount))
    }

    suspend fun waitStub(context: SpiralContext, frameCount: Any?) = wait(intStub(context, frameCount))
    suspend fun wait(frameCount: Int) = runNoOp {
        custom.addEntry(Dr1SetGameParameterEntry(Dr1SetGameParameterEntry.GAME_PARAMETER_WAIT_FORCE, Dr1SetGameParameterEntry.OPERATOR_SET, frameCount))
    }

    override fun registerDefaults() {
        super.registerDefaults()

        register(SpiralSuspending.Func2("Speak", "voiceID", "volume", func = this::speakStub))
        register(SpiralSuspending.Func1("Text", "text", func = this::textStub))
        register(SpiralSuspending.Func1("CameraFocus", "character", func = this::cameraFocusStub))

        register(SpiralSuspending.Func1("ShowCutin", "id", func = this::showCutinStub))
        register(SpiralSuspending.Func1("HideCutin", "id", func = this::hideCutinStub))
        register(SpiralSuspending.Func1("ShowItem", "id", func = this::showItemStub))
        register(SpiralSuspending.Func1("HideItem", "id", func = this::hideItemStub))

        register(SpiralSuspending.Func1("EnableUI", "element", func = this::enableUIStub))
        register(SpiralSuspending.Func1("DisableUI", "element", func = this::disableUIStub))

//        register(SpiralSuspending.Func1("EnableFlag", "flagID", func = this::enableFlagStub))
        register(SpiralSuspending.Func2("EnableFlag", "flagGroup", "flagID", func = this::enableFlagStub))
//        register(SpiralSuspending.Func1("DisableFlag", "flagID", func = this::enableFlagStub))
        register(SpiralSuspending.Func2("DisableFlag", "flagGroup", "flagID", func = this::enableFlagStub))

        register(SpiralSuspending.Func1("FadeInFromBlack", "frameCount", func = this::fadeInFromBlackStub))
        register(SpiralSuspending.Func1("FadeOutToBlack", "frameCount", func = this::fadeOutToBlackStub))

        register(SpiralSuspending.Func1("Wait", "frameCount", func = this::waitStub))
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun OutputFlow.compileLinFromBitcode(context: SpiralContext, game: DrGame.LinScriptable, bitcode: InputFlow) {
    bitcode.parseOpenSpiralBitcode(context, LinCompiler(this, game))
}