package info.spiralframework.osb.common

import info.spiralframework.base.binding.encodeToUTF8ByteArray
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.wrd.WordScriptValue
import info.spiralframework.formats.common.scripting.wrd.WrdEntry
import info.spiralframework.osb.common.OpenSpiralBitcode.ACTION_END
import info.spiralframework.osb.common.OpenSpiralBitcode.ACTION_TEXT
import info.spiralframework.osb.common.OpenSpiralBitcode.ACTION_VARIABLE
import info.spiralframework.osb.common.OpenSpiralBitcode.LONG_REFERENCE_COLOUR_CODE
import info.spiralframework.osb.common.OpenSpiralBitcode.LONG_REFERENCE_END
import info.spiralframework.osb.common.OpenSpiralBitcode.LONG_REFERENCE_TEXT
import info.spiralframework.osb.common.OpenSpiralBitcode.LONG_REFERENCE_VARIABLE
import info.spiralframework.osb.common.OpenSpiralBitcode.MAGIC_NUMBER_LE
import info.spiralframework.osb.common.OpenSpiralBitcode.OPERATION_ADD_DIALOGUE
import info.spiralframework.osb.common.OpenSpiralBitcode.OPERATION_ADD_DIALOGUE_VARIABLE
import info.spiralframework.osb.common.OpenSpiralBitcode.OPERATION_ADD_FUNCTION_CALL
import info.spiralframework.osb.common.OpenSpiralBitcode.OPERATION_ADD_PLAIN_OPCODE
import info.spiralframework.osb.common.OpenSpiralBitcode.OPERATION_ADD_PLAIN_OPCODE_NAMED
import info.spiralframework.osb.common.OpenSpiralBitcode.OPERATION_ADD_VARIABLE_OPCODE
import info.spiralframework.osb.common.OpenSpiralBitcode.OPERATION_ADD_VARIABLE_OPCODE_NAMED
import info.spiralframework.osb.common.OpenSpiralBitcode.OPERATION_SET_VARIABLE
import info.spiralframework.osb.common.OpenSpiralBitcode.OPERATION_SET_VERSION
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_ARBITRARY_DECIMAL
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_ARBITRARY_INTEGER
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_BOOL
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_FUNCTION_CALL
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_INT16BE
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_INT16LE
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_INT32BE
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_INT32LE
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_INT8
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_LABEL
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_LONG_LABEL
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_LONG_PARAMETER
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_LONG_REFERENCE
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_PARAMETER
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_TEXT
import info.spiralframework.osb.common.OpenSpiralBitcode.VARIABLE_VAR_REFERENCE
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.BinaryOutputFlow
import org.abimon.kornea.io.common.flow.OutputFlow
import kotlin.math.roundToInt

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class OpenSpiralBitcodeBuilder private constructor(val output: OutputFlow) {
    companion object {
        suspend operator fun invoke(output: OutputFlow): OpenSpiralBitcodeBuilder {
            val builder = OpenSpiralBitcodeBuilder(output)
            builder.writeMagicNumber()
            return builder
        }
    }

    class LongReference private constructor(val output: OutputFlow) {
        companion object {
            suspend operator fun invoke(output: OutputFlow, init: suspend LongReference.() -> Unit): LongReference {
                val ref = LongReference(output)
                ref.init()
                output.write(LONG_REFERENCE_END)
                return ref
            }
        }

        @ExperimentalStdlibApi
        suspend fun appendText(text: String) {
            output.write(LONG_REFERENCE_TEXT)
            output.write(text.encodeToUTF8ByteArray())
            output.write(0x00)
        }

        @ExperimentalStdlibApi
        suspend fun appendVariable(variableName: String) {
            output.write(LONG_REFERENCE_VARIABLE)
            output.write(variableName.encodeToUTF8ByteArray())
            output.write(0x00)
        }

        @ExperimentalStdlibApi
        suspend fun appendColourCode(colourCode: String) {
            output.write(LONG_REFERENCE_COLOUR_CODE)
            output.write(colourCode.encodeToUTF8ByteArray())
            output.write(0x00)
        }
    }

    class Action private constructor(val output: OutputFlow) {
        companion object {
            suspend operator fun invoke(output: OutputFlow, init: suspend Action.() -> Unit): Action {
                val ref = Action(output)
                ref.init()
                output.write(ACTION_END)
                return ref
            }
        }

        @ExperimentalStdlibApi
        suspend fun appendText(text: String) {
            output.write(ACTION_TEXT)
            output.write(text.encodeToUTF8ByteArray())
            output.write(0x00)
        }

        @ExperimentalStdlibApi
        suspend fun appendVariable(variableName: String) {
            output.write(ACTION_VARIABLE)
            output.write(variableName.encodeToUTF8ByteArray())
            output.write(0x00)
        }

//        @ExperimentalStdlibApi
//        suspend fun appendColourCode(colourCode: String) {
//            output.write(ACTION_COLOUR_CODE)
//            output.write(colourCode.encodeToUTF8ByteArray())
//            output.write(0x00)
//        }
    }

    suspend fun writeMagicNumber() {
        output.writeInt32LE(MAGIC_NUMBER_LE)
    }

    suspend fun setVersion(version: SemanticVersion) {
        output.write(OPERATION_SET_VERSION)
        output.write(version.major)
        output.write(version.minor)
        output.write(version.patch)
    }

    suspend fun addOpcode(entry: LinEntry) = addOpcode(entry.opcode, entry.rawArguments)
    @ExperimentalStdlibApi
    suspend fun addOpcode(entry: WrdEntry) {
        output.write(OPERATION_ADD_VARIABLE_OPCODE)
        output.write(entry.opcode)
        output.write(entry.arguments.size)

        entry.arguments.forEach { arg ->
            when (arg) {
                is WordScriptValue.Label -> {
                    output.write(VARIABLE_LABEL)
                    output.write(arg.label.encodeToUTF8ByteArray())
                    output.write(0x00)
                }
                is WordScriptValue.Parameter -> {
                    output.write(VARIABLE_PARAMETER)
                    output.write(arg.param.encodeToUTF8ByteArray())
                    output.write(0x00)
                }
                is WordScriptValue.InternalText -> {
                    output.write(VARIABLE_TEXT)
                    output.write(arg.text.encodeToUTF8ByteArray())
                    output.write(0x00)
                }
                else -> {
                    output.write(VARIABLE_INT8)
                    output.write(arg.raw)
                }
            }
        }
    }

    suspend fun addOpcode(opcode: Int, rawArguments: IntArray) {
        output.write(OPERATION_ADD_PLAIN_OPCODE)
        output.write(opcode)
        output.write(rawArguments.size)
        rawArguments.suspendForEach(output::writeVariableInt16)
    }

    @ExperimentalStdlibApi
    suspend fun addOpcode(opcode: Int, arguments: Array<OSLUnion>) {
        if (arguments.all { union -> union is OSLUnion.NumberType || union is OSLUnion.BooleanType }) {
            addOpcode(opcode, collectArgs(arguments))
        } else {
            output.write(OPERATION_ADD_VARIABLE_OPCODE)
            output.write(opcode)
            output.write(arguments.count { arg -> !(arg is OSLUnion.UndefinedType || arg is OSLUnion.NullType || arg is OSLUnion.NoOpType) })

            arguments.suspendForEach(this::writeArg)
        }
    }

    @ExperimentalStdlibApi
    suspend fun addOpcode(opcodeName: String, rawArguments: IntArray) {
        output.write(OPERATION_ADD_PLAIN_OPCODE_NAMED)
        output.write(opcodeName.encodeToUTF8ByteArray())
        output.write(0x00)
        output.write(rawArguments.size)
        rawArguments.suspendForEach(output::writeVariableInt16)
    }

    private suspend fun collectArgs(arguments: Array<OSLUnion>): IntArray {
        val args: MutableList<Int> = ArrayList()
        arguments.forEach { union ->
            when (union) {
                is OSLUnion.BooleanType -> args.add(if (union.boolean) 1 else 0)
                is OSLUnion.Int8NumberType -> args.add(union.number.toInt())
                is OSLUnion.Int16LENumberType -> {
                    val num = union.number.toInt()
                    args.add((num shr 0) and 0xFF)
                    args.add((num shr 8) and 0xFF)
                }
                is OSLUnion.Int16BENumberType -> {
                    val num = union.number.toInt()
                    args.add((num shr 8) and 0xFF)
                    args.add((num shr 0) and 0xFF)
                }
                is OSLUnion.Int32LENumberType -> {
                    val num = union.number.toInt()
                    args.add((num shr 0) and 0xFF)
                    args.add((num shr 8) and 0xFF)
                    args.add((num shr 16) and 0xFF)
                    args.add((num shr 24) and 0xFF)
                }
                is OSLUnion.Int32BENumberType -> {
                    val num = union.number.toInt()
                    args.add((num shr 24) and 0xFF)
                    args.add((num shr 16) and 0xFF)
                    args.add((num shr 8) and 0xFF)
                    args.add((num shr 0) and 0xFF)
                }
                is OSLUnion.IntegerNumberType -> args.add(union.number.toInt()) //Arbitrary number; assume wrongly formatted single byte
                is OSLUnion.DecimalNumberType -> args.add(union.number.toDouble().roundToInt()) //See above
                else -> error("Invalid number $union")
            }
        }
        return args.toIntArray()
    }

    @ExperimentalStdlibApi
    suspend fun addOpcode(opcodeName: String, arguments: Array<OSLUnion>) {
        if (arguments.all { union -> union is OSLUnion.NumberType || union is OSLUnion.BooleanType }) {
            addOpcode(opcodeName, collectArgs(arguments))
        } else {
            output.write(OPERATION_ADD_VARIABLE_OPCODE_NAMED)
            output.write(opcodeName.encodeToUTF8ByteArray())
            output.write(0x00)
            output.write(arguments.count { arg -> !(arg is OSLUnion.UndefinedType || arg is OSLUnion.NullType || arg is OSLUnion.NoOpType) })

            arguments.suspendForEach(this::writeArg)
        }
    }

    suspend fun setVariable(variableName: String, variableValue: OSLUnion) {
        output.write(OPERATION_SET_VARIABLE)
        output.write(variableName.encodeToUTF8ByteArray())
        output.write(0x00)
        writeArg(variableValue)
    }

    @ExperimentalStdlibApi
    suspend fun addDialogue(speakerName: String, dialogue: OSLUnion) {
        output.write(OPERATION_ADD_DIALOGUE)
        output.write(speakerName.encodeToUTF8ByteArray())
        output.write(0x00)
        writeArg(dialogue)
    }

    @ExperimentalStdlibApi
    suspend fun addDialogueVariable(speakerVariable: String, dialogue: OSLUnion) {
        output.write(OPERATION_ADD_DIALOGUE_VARIABLE)
        output.write(speakerVariable.encodeToUTF8ByteArray())
        output.write(0x00)
        writeArg(dialogue)
    }

    suspend fun addFunctionCall(functionName: String, parameters: List<OSLUnion.FunctionParameterType>) {
        output.write(OPERATION_ADD_FUNCTION_CALL)
        output.write(functionName.encodeToUTF8ByteArray())
        output.write(0x00)
        output.write(parameters.size)
        parameters.forEach { param ->
            if (param.parameterName != null) output.write(param.parameterName.encodeToUTF8ByteArray())
            output.write(0x00)
            writeArg(param.parameterValue)
        }
    }

    @ExperimentalStdlibApi
    suspend fun writeArg(arg: OSLUnion) {
        when (arg) {
            is OSLUnion.Int8NumberType -> {
                output.write(VARIABLE_INT8)
                output.write(arg.number.toInt())
            }
            is OSLUnion.Int16LENumberType -> {
                output.write(VARIABLE_INT16LE)
                output.writeVariableInt16(arg.number)
            }
            is OSLUnion.Int16BENumberType -> {
                output.write(VARIABLE_INT16BE)
                output.writeVariableInt16(arg.number)
            }
            is OSLUnion.Int32LENumberType -> {
                output.write(VARIABLE_INT32LE)
                output.writeVariableInt32(arg.number)
            }
            is OSLUnion.Int32BENumberType -> {
                output.write(VARIABLE_INT32BE)
                output.writeVariableInt32(arg.number)
            }
            is OSLUnion.IntegerNumberType -> {
                output.write(VARIABLE_ARBITRARY_INTEGER)
                output.writeVariableInt64(arg.number)
            }
            is OSLUnion.DecimalNumberType -> {
                output.write(VARIABLE_ARBITRARY_DECIMAL)
                output.writeFloatLE(arg.number.toFloat()) //TODO: Switch to double? Or break this into two unions?
            }
            is OSLUnion.RawStringType -> {
                output.write(VARIABLE_TEXT)
                output.write(arg.string.encodeToUTF8ByteArray())
                output.write(0x00)
            }
            is OSLUnion.LabelType -> {
                output.write(VARIABLE_LABEL)
                output.write(arg.label.encodeToUTF8ByteArray())
                output.write(0x00)
            }
            is OSLUnion.LongLabelType -> {
                output.write(VARIABLE_LONG_LABEL)
                output.write(arg.longReference)
            }
            is OSLUnion.ParameterType -> {
                output.write(VARIABLE_PARAMETER)
                output.write(arg.parameter.encodeToUTF8ByteArray())
                output.write(0x00)
            }
            is OSLUnion.LongParameterType -> {
                output.write(VARIABLE_LONG_PARAMETER)
                output.write(arg.longReference)
            }
            is OSLUnion.LongReferenceType -> {
                output.write(VARIABLE_LONG_REFERENCE)
                output.write(arg.longReference)
            }
            is OSLUnion.BooleanType -> {
                //TODO: Consider if it's worthwhile condensing this down to a bit
                output.write(VARIABLE_BOOL)
                output.write(if (arg.boolean) 1 else 0)
            }
            is OSLUnion.FunctionCallType -> {
                output.write(VARIABLE_FUNCTION_CALL)
                output.write(arg.functionName.encodeToUTF8ByteArray())
                output.write(0x00)
                output.write(arg.parameters.size)
                arg.parameters.forEach { param ->
                    if (param.parameterName != null) output.write(param.parameterName.encodeToUTF8ByteArray())
                    output.write(0x00)
                    writeArg(param.parameterValue)
                }
            }
            is OSLUnion.VariableReferenceType -> {
                output.write(VARIABLE_VAR_REFERENCE)
                output.write(arg.variableName.encodeToUTF8ByteArray())
                output.write(0x00)
            }
            else -> throw IllegalArgumentException("Invalid value $arg")
        }
    }
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun buildOpenSpiralBitcode(init: suspend OpenSpiralBitcodeBuilder.() -> Unit): ByteArray {
    val binary = BinaryOutputFlow()
    val builder = OpenSpiralBitcodeBuilder(binary)
    builder.init()
    return binary.getData()
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun buildLongReference(init: suspend OpenSpiralBitcodeBuilder.LongReference.() -> Unit): ByteArray {
    val binary = BinaryOutputFlow()
    OpenSpiralBitcodeBuilder.LongReference(binary, init)
    return binary.getData()
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun buildAction(init: suspend OpenSpiralBitcodeBuilder.Action.() -> Unit): ByteArray {
    val binary = BinaryOutputFlow()
    OpenSpiralBitcodeBuilder.Action(binary, init)
    return binary.getData()
}