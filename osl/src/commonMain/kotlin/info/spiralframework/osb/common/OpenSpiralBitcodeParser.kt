package info.spiralframework.osb.common

import dev.brella.kornea.annotations.AvailableSince
import dev.brella.kornea.io.common.KorneaIO
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.Int16FlowState
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.*
import dev.brella.kornea.io.common.flow.readExact
import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.text.toHexString

public object OpenSpiralBitcode {
    public const val MAGIC_NUMBER_LE: Int = 0x494C534F

    /** Operations */

    public const val OPERATION_SET_VERSION: Int = 0x00
    public const val OPERATION_ADD_DIALOGUE: Int = 0x01
    public const val OPERATION_ADD_DIALOGUE_VARIABLE: Int = 0x02
    public const val OPERATION_ADD_FUNCTION_CALL: Int = 0x03
    public const val OPERATION_ADD_IF_CHECK: Int = 0x04
    public const val OPERATION_ADD_FLAG_CHECK: Int = 0x05
    public const val OPERATION_ADD_TREE: Int = 0x06
    public const val OPERATION_ADD_LOAD_MAP: Int = 0x07

    public const val OPERATION_ADD_PLAIN_OPCODE: Int = 0x70
    public const val OPERATION_ADD_VARIABLE_OPCODE: Int = 0x71
    public const val OPERATION_ADD_PLAIN_OPCODE_NAMED: Int = 0x72
    public const val OPERATION_ADD_VARIABLE_OPCODE_NAMED: Int = 0x73

    public const val OPERATION_ADD_LABEL: Int = 0x80
    public const val OPERATION_ADD_PARAMETER: Int = 0x81
    public const val OPERATION_ADD_TEXT: Int = 0x82
    public const val OPERATION_SET_VARIABLE: Int = 0x8F

    /** Other magic values */

    public const val VARIABLE_LABEL: Int = 0x60
    public const val VARIABLE_PARAMETER: Int = 0x61
    public const val VARIABLE_TEXT: Int = 0x62
    public const val VARIABLE_LONG_LABEL: Int = 0x63
    public const val VARIABLE_LONG_PARAMETER: Int = 0x64
    public const val VARIABLE_LONG_REFERENCE: Int = 0x65
    public const val VARIABLE_BOOL: Int = 0x6A
    public const val VARIABLE_FUNCTION_CALL: Int = 0x6B
    public const val VARIABLE_VAR_REFERENCE: Int = 0x6C
    public const val VARIABLE_NULL: Int = 0x6F

    public const val VARIABLE_INT8: Int = 0x70
    public const val VARIABLE_INT16LE: Int = 0x71
    public const val VARIABLE_INT16BE: Int = 0x72
    public const val VARIABLE_INT24LE: Int = 0x73
    public const val VARIABLE_INT24BE: Int = 0x74
    public const val VARIABLE_INT32LE: Int = 0x75
    public const val VARIABLE_INT32BE: Int = 0x76
    public const val VARIABLE_ARBITRARY_INTEGER: Int = 0x7E
    public const val VARIABLE_ARBITRARY_DECIMAL: Int = 0x7F

    public const val LONG_REFERENCE_TEXT: Int = 0xA0
    public const val LONG_REFERENCE_VARIABLE: Int = 0xA1
    public const val LONG_REFERENCE_COLOUR_CODE: Int = 0xA2
    public const val LONG_REFERENCE_END: Int = 0xAF

    public const val ACTION_TEXT: Int = 0xB0
    public const val ACTION_VARIABLE: Int = 0xB1
    public const val ACTION_COLOUR_CODE: Int = 0xB2
    public const val ACTION_END: Int = 0xBF

    public const val EQUALITY_EQUAL: Int = 0
    public const val EQUALITY_NOT_EQUAL: Int = 1
    public const val EQUALITY_LESS_THAN: Int = 2
    public const val EQUALITY_GREATER_THAN: Int = 3
    public const val EQUALITY_LESS_THAN_EQUAL_TO: Int = 4
    public const val EQUALITY_GREATER_THAN_EQUAL_TO: Int = 5

    public const val LOGICAL_AND: Int = 0
    public const val LOGICAL_OR: Int = 1

    public const val TREE_TYPE_PRESENT_SELECTION: Int = 0
}

public data class OpenSpiralBitcodeFlagBranch(
    val condition: OpenSpiralBitcodeFlagCondition,
    val otherConditions: Array<Pair<Int, OpenSpiralBitcodeFlagCondition>>,
    val branch: ByteArray
)

public data class OpenSpiralBitcodeFlagCondition(
    val checking: OSLUnion,
    val operation: Int,
    val against: OSLUnion
)

@AvailableSince(KorneaIO.VERSION_3_2_2_ALPHA)
public suspend inline fun <T> T.readVariableInt16NotBroken(): Int? where T : Int16FlowState, T : InputFlow =
    (this as InputFlow).readVariableInt16()

public class OpenSpiralBitcodeParser(
    public val flow: InputFlow,
    public val visitor: OpenSpiralBitcodeVisitor,
    public val level: Int
) {
    public companion object {
        public const val PREFIX: String = "osl.bitcode.parser"
        public const val INVALID_MAGIC: Int = 0x0000

        public const val NOT_ENOUGH_DATA_KEY: String = "$PREFIX.not_enough_data"
        public const val INVALID_MAGIC_KEY: String = "$PREFIX.invalid_magic"
    }

    public suspend fun parse(context: SpiralContext) {
        try {
            with(context) {
                val notEnoughData: () -> String = { localise("$PREFIX.not_enough_data") }

                while (true) {
                    val opcode = flow.read() ?: return
                    context.trace("[{0}] Reading opcode {1}", level, opcode.toHexString())
                    when (opcode) {
                        OpenSpiralBitcode.OPERATION_SET_VERSION -> setVersion(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_DIALOGUE -> addDialogue(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_DIALOGUE_VARIABLE -> addDialogueVariable(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_FUNCTION_CALL -> addFunctionCall(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_IF_CHECK -> addIfCheck(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_FLAG_CHECK -> addFlagCheck(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_TREE -> addTree(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_LOAD_MAP -> addLoadMap(notEnoughData)

                        OpenSpiralBitcode.OPERATION_ADD_PLAIN_OPCODE -> addPlainOpcode(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_PLAIN_OPCODE_NAMED -> addPlainOpcodeNamed(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_VARIABLE_OPCODE -> addVariableOpcode(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_VARIABLE_OPCODE_NAMED -> addVariableOpcodeNamed(notEnoughData)

                        OpenSpiralBitcode.OPERATION_SET_VARIABLE -> setVariable(notEnoughData)
                        else -> println("Unknown opcode ${opcode.toHexString()}")
                    }
                }
            }
        } finally {
            if (level == 0) {
                visitor.end(context)
            }
        }
    }

    private suspend fun SpiralContext.setVersion(notEnoughData: () -> String) {
        val major = requireNotNull(flow.read(), notEnoughData)
        val minor = requireNotNull(flow.read(), notEnoughData)
        val patch = requireNotNull(flow.read(), notEnoughData)

        visitor.setVersion(this, SemanticVersion(major, minor, patch))
    }

    private suspend fun SpiralContext.addDialogue(notEnoughData: () -> String) {
        val speakerName = flow.readNullTerminatedUTF8String()
        val dialogue = readArg(notEnoughData)

        visitor.addDialogue(this, speakerName, dialogue)
    }

    private suspend fun SpiralContext.addDialogueVariable(notEnoughData: () -> String) {
        val variableName = flow.readNullTerminatedUTF8String()
        val dialogue = readArg(notEnoughData)

        val data = visitor.getData(this, variableName) ?: return
        when (data) {
            is OSLUnion.NumberType -> visitor.addDialogue(this, data.number.toInt(), dialogue)
            else -> visitor.addDialogue(this, visitor.stringify(this, data), dialogue)
        }
    }

    private suspend fun SpiralContext.addFunctionCall(notEnoughData: () -> String) {
        val functionName = flow.readNullTerminatedUTF8String()
        val paramCount = requireNotNull(flow.read(), notEnoughData)
        val parameters = Array(paramCount) {
            val paramName = flow.readNullTerminatedUTF8String().takeUnless(String::isBlank)
            val param = readArg(notEnoughData)
            OSLUnion.FunctionParameterType(paramName, param)
        }

        visitor.functionCall(this, functionName, parameters)
    }

    private suspend fun SpiralContext.addIfCheck(notEnoughData: () -> String) {
        var checking = readArg(notEnoughData)
        var method = requireNotNull(flow.read(), notEnoughData)
        var against = readArg(notEnoughData)
        val elifFlag = requireNotNull(flow.read(), notEnoughData)
        val elseIfBranches = elifFlag and 0x7F
        val hasElseBranch = elifFlag and 0x80 == 0x80

        if (checking == against) {
            //Success! Run the code against our visitor
            val length = requireNotNull(flow.readInt32LE(), notEnoughData)
            OpenSpiralBitcodeParser(WindowedInputFlow(flow, 0uL, length.toULong()), visitor, level + 1)
                .parse(this)

            //Then, we need to discard the rest of the else if branches
            repeat(elseIfBranches) {
                readArg(notEnoughData)
                requireNotNull(flow.read(), notEnoughData)
                readArg(notEnoughData)

                flow.skip(requireNotNull(flow.readInt32LE(), notEnoughData).toULong())
            }

            if (hasElseBranch) {
                //Discard the else branch as well
                flow.skip(requireNotNull(flow.readInt32LE(), notEnoughData).toULong())
            }
        } else {
            //Skip the main branch then
            flow.skip(requireNotNull(flow.readInt32LE(), notEnoughData).toULong())

            var success = false
            //Alright, we need to go down the list of else if branches
            repeat(elseIfBranches) {
                checking = readArg(notEnoughData)
                method = requireNotNull(flow.read(), notEnoughData)
                against = readArg(notEnoughData)
                val length = requireNotNull(flow.readInt32LE(), notEnoughData)

                if (!success && checking == against) {
                    OpenSpiralBitcodeParser(WindowedInputFlow(flow, 0uL, length.toULong()), visitor, level + 1)
                        .parse(this)
                    success = true
                } else {
                    flow.skip(length.toULong())
                }
            }

            if (hasElseBranch) {
                val length = requireNotNull(flow.readInt32LE(), notEnoughData)

                //We haven't had a successful branch yet; run the else branch
                if (!success) {
                    OpenSpiralBitcodeParser(WindowedInputFlow(flow, 0uL, length.toULong()), visitor, level + 1)
                        .parse(this)
                } else {
                    flow.skip(length.toULong())
                }
            }
        }
    }

    private suspend fun SpiralContext.addFlagCheck(notEnoughData: () -> String) {
        var conditionCount = requireNotNull(flow.read(), notEnoughData)
        var mainCondition = OpenSpiralBitcodeFlagCondition(
            readArg(notEnoughData),
            requireNotNull(flow.read(), notEnoughData),
            readArg(notEnoughData)
        )
        var otherConditions = Array(conditionCount - 1) {
            Pair(
                requireNotNull(flow.read(), notEnoughData),
                OpenSpiralBitcodeFlagCondition(
                    readArg(notEnoughData),
                    requireNotNull(flow.read(), notEnoughData),
                    readArg(notEnoughData)
                )
            )
        }
        val elifFlag = requireNotNull(flow.read(), notEnoughData)
        val elseIfBranchCount = elifFlag and 0x7F
        val hasElseBranch = elifFlag and 0x80 == 0x80

        //This code has a number of differences from the if check, since we run the check in game, not at compile

        val mainBranch = OpenSpiralBitcodeFlagBranch(
            mainCondition,
            otherConditions,
            requireNotNull(flow.readExact(ByteArray(requireNotNull(flow.readInt32LE(), notEnoughData))))
        )
        val elseIfBranches = Array(elseIfBranchCount) {
            conditionCount = requireNotNull(flow.read(), notEnoughData)
            mainCondition = OpenSpiralBitcodeFlagCondition(
                readArg(notEnoughData),
                requireNotNull(flow.read(), notEnoughData),
                readArg(notEnoughData)
            )
            otherConditions = Array(conditionCount - 1) {
                Pair(
                    requireNotNull(flow.read(), notEnoughData),
                    OpenSpiralBitcodeFlagCondition(
                        readArg(notEnoughData),
                        requireNotNull(flow.read(), notEnoughData),
                        readArg(notEnoughData)
                    )
                )
            }
            OpenSpiralBitcodeFlagBranch(
                mainCondition,
                otherConditions,
                requireNotNull(flow.readExact(ByteArray(requireNotNull(flow.readInt32LE(), notEnoughData))))
            )
        }
        val elseBranch = if (hasElseBranch) requireNotNull(
            flow.readExact(
                ByteArray(
                    requireNotNull(
                        flow.readInt32LE(),
                        notEnoughData
                    )
                )
            )
        ) else null

        visitor.addFlagCheck(this, mainBranch, elseIfBranches, elseBranch, level + 1)
    }

    private suspend fun SpiralContext.addTree(notEnoughData: () -> String) {
        val treeType = requireNotNull(flow.read(), notEnoughData)
        val scope = requireNotNull(flow.readExact(ByteArray(requireNotNull(flow.readInt32LE(), notEnoughData))))

        visitor.addTree(this, treeType, scope, level + 1)
    }

    private suspend fun SpiralContext.addLoadMap(notEnoughData: () -> String) {
        val mapID = readArg(notEnoughData)
        val state = readArg(notEnoughData).orElse(OSLUnion.Int8NumberType(0))
        val arg3 = readArg(notEnoughData).orElse(OSLUnion.Int8NumberType(0xFF))

        val scope = requireNotNull(flow.readExact(ByteArray(requireNotNull(flow.readInt32LE(), notEnoughData))))

        visitor.addLoadMap(this, mapID, state, arg3, scope, level + 1)
    }

    private suspend fun SpiralContext.addPlainOpcode(notEnoughData: () -> String) {
        val opcode = requireNotNull(flow.read(), notEnoughData)
        val argumentCount = requireNotNull(flow.read(), notEnoughData)
        val arguments = IntArray(argumentCount) { requireNotNull(flow.readVariableInt16(), notEnoughData) }

        visitor.addPlainOpcode(this, opcode, arguments)
    }

    private suspend fun SpiralContext.addPlainOpcodeNamed(notEnoughData: () -> String) {
        val name = flow.readNullTerminatedUTF8String()
        val argumentCount = requireNotNull(flow.read(), notEnoughData)
        val arguments = IntArray(argumentCount) { requireNotNull(flow.readVariableInt16(), notEnoughData) }

        visitor.addPlainOpcodeNamed(this, name, arguments)
    }

    private suspend fun SpiralContext.addVariableOpcode(notEnoughData: () -> String) {
        val opcode = requireNotNull(flow.read(), notEnoughData)
        val argumentCount = requireNotNull(flow.read(), notEnoughData)
        val arguments = Array(argumentCount) { readArg(notEnoughData) }

        visitor.addVariableOpcode(this, opcode, arguments)
    }

    private suspend fun SpiralContext.addVariableOpcodeNamed(notEnoughData: () -> String) {
        val name = flow.readNullTerminatedUTF8String()
        val argumentCount = requireNotNull(flow.read(), notEnoughData)
        val arguments = Array(argumentCount) { readArg(notEnoughData) }

        visitor.addVariableOpcodeNamed(this, name, arguments)
    }

    private suspend fun SpiralContext.setVariable(notEnoughData: () -> String) {
        val variableName = flow.readNullTerminatedUTF8String()
        val value = readArg(notEnoughData)

        visitor.setData(this, variableName, value)
    }

    private suspend fun SpiralContext.parseLongReference(): String = buildString {
        while (true) {
            when (flow.read() ?: return@buildString) {
                OpenSpiralBitcode.LONG_REFERENCE_TEXT -> append(flow.readNullTerminatedUTF8String())
                OpenSpiralBitcode.LONG_REFERENCE_VARIABLE -> {
                    visitor.getData(this@parseLongReference, flow.readNullTerminatedUTF8String())
                        ?.let { visitor.stringify(this@parseLongReference, it) }
                        ?.let(this::append)
                }
                OpenSpiralBitcode.LONG_REFERENCE_COLOUR_CODE -> {
                    visitor.colourCodeFor(this@parseLongReference, flow.readNullTerminatedUTF8String())
                        ?.let(this::append)
                }
                OpenSpiralBitcode.LONG_REFERENCE_END -> {
                    visitor.closeLongReference(this@parseLongReference)
                        ?.let(this::append)

                    return@buildString
                }
            }
        }
    }

    private suspend fun SpiralContext.readArg(notEnoughData: () -> String): OSLUnion =
        when (val variable = requireNotNull(flow.read(), notEnoughData)) {
            OpenSpiralBitcode.VARIABLE_LABEL -> OSLUnion.LabelType(flow.readNullTerminatedUTF8String())
            OpenSpiralBitcode.VARIABLE_PARAMETER -> OSLUnion.ParameterType(flow.readNullTerminatedUTF8String())
            OpenSpiralBitcode.VARIABLE_TEXT -> OSLUnion.RawStringType(flow.readNullTerminatedUTF8String())
            OpenSpiralBitcode.VARIABLE_LONG_LABEL -> OSLUnion.LabelType(parseLongReference())
            OpenSpiralBitcode.VARIABLE_LONG_PARAMETER -> OSLUnion.ParameterType(parseLongReference())
            OpenSpiralBitcode.VARIABLE_LONG_REFERENCE -> OSLUnion.RawStringType(parseLongReference())
            OpenSpiralBitcode.VARIABLE_BOOL -> OSLUnion.BooleanType(requireNotNull(flow.read(), notEnoughData) != 0)
            OpenSpiralBitcode.VARIABLE_FUNCTION_CALL -> {
                val funcName = flow.readNullTerminatedUTF8String()
                val paramCount = requireNotNull(flow.read(), notEnoughData)
                val parameters = Array(paramCount) {
                    val paramName = flow.readNullTerminatedUTF8String().takeUnless(String::isBlank)
                    val param = readArg(notEnoughData)
                    OSLUnion.FunctionParameterType(paramName, param)
                }

                OSLUnion.FunctionCallType(funcName, parameters)
            }

            OpenSpiralBitcode.VARIABLE_INT8 -> OSLUnion.Int8NumberType(requireNotNull(flow.read(), notEnoughData))
            OpenSpiralBitcode.VARIABLE_INT16LE -> OSLUnion.Int16LENumberType(
                requireNotNull(
                    flow.readInt16LE(),
                    notEnoughData
                )
            )
            OpenSpiralBitcode.VARIABLE_INT16BE -> OSLUnion.Int16BENumberType(
                requireNotNull(
                    flow.readInt16BE(),
                    notEnoughData
                )
            )
            OpenSpiralBitcode.VARIABLE_INT24LE -> OSLUnion.Int24LENumberType(
                requireNotNull(
                    flow.readInt24LE(),
                    notEnoughData
                )
            )
            OpenSpiralBitcode.VARIABLE_INT24BE -> OSLUnion.Int24BENumberType(
                requireNotNull(
                    flow.readInt24BE(),
                    notEnoughData
                )
            )
            OpenSpiralBitcode.VARIABLE_INT32LE -> OSLUnion.Int32LENumberType(
                requireNotNull(
                    flow.readInt32LE(),
                    notEnoughData
                )
            )
            OpenSpiralBitcode.VARIABLE_INT32BE -> OSLUnion.Int32BENumberType(
                requireNotNull(
                    flow.readInt32BE(),
                    notEnoughData
                )
            )
            OpenSpiralBitcode.VARIABLE_ARBITRARY_INTEGER -> OSLUnion.IntegerNumberType(
                requireNotNull(
                    flow.readInt64LE(),
                    notEnoughData
                )
            )
            OpenSpiralBitcode.VARIABLE_ARBITRARY_DECIMAL -> OSLUnion.DecimalNumberType(
                requireNotNull(
                    flow.readFloatLE(),
                    notEnoughData
                )
            )

            OpenSpiralBitcode.VARIABLE_VAR_REFERENCE -> OSLUnion.VariableReferenceType(flow.readNullTerminatedUTF8String())
            OpenSpiralBitcode.VARIABLE_NULL -> OSLUnion.NullType

            else -> throw IllegalArgumentException("Invalid variable: $variable")
        }
}

public suspend fun <T : OpenSpiralBitcodeVisitor> InputFlow.parseOpenSpiralBitcode(context: SpiralContext, visitor: T): T {
    val magic = requireNotNull(readInt32LE()) { context.localise("${OpenSpiralBitcodeParser.PREFIX}.not_enough_data") }
    require(magic == OpenSpiralBitcode.MAGIC_NUMBER_LE) { context.localise("${OpenSpiralBitcodeParser.PREFIX}.invalid_magic") }

    val parser = OpenSpiralBitcodeParser(this, visitor, 0)
    parser.parse(context)
    return visitor
}