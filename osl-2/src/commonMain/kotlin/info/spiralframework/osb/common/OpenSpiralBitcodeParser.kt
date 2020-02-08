package info.spiralframework.osb.common

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedUTF8String
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.readVariableInt16

object OpenSpiralBitcode {
    const val MAGIC_NUMBER_LE = 0x494C534F

    /** Operations */

    const val OPERATION_SET_VERSION = 0x00
    const val OPERATION_ADD_DIALOGUE = 0x01
    const val OPERATION_ADD_DIALOGUE_VARIABLE = 0x02

    const val OPERATION_ADD_PLAIN_OPCODE = 0x70
    const val OPERATION_ADD_VARIABLE_OPCODE = 0x71
    const val OPERATION_ADD_PLAIN_OPCODE_NAMED = 0x72
    const val OPERATION_ADD_VARIABLE_OPCODE_NAMED = 0x73

    const val OPERATION_ADD_LABEL = 0x80
    const val OPERATION_ADD_PARAMETER = 0x81
    const val OPERATION_ADD_TEXT = 0x82
    const val OPERATION_SET_VARIABLE = 0x8F

    /** Other magic values */

    const val VARIABLE_LABEL = 0x60
    const val VARIABLE_PARAMETER = 0x61
    const val VARIABLE_TEXT = 0x62
    const val VARIABLE_LONG_LABEL = 0x63
    const val VARIABLE_LONG_PARAMETER = 0x64
    const val VARIABLE_LONG_REFERENCE = 0x65
    const val VARIABLE_BOOL = 0x6E
    const val VARIABLE_RAW = 0x6F

    const val LONG_REFERENCE_TEXT = 0xA0
    const val LONG_REFERENCE_VARIABLE = 0xA1
    const val LONG_REFERENCE_COLOUR_CODE = 0xA2
    const val LONG_REFERENCE_END = 0xAF

    const val ACTION_TEXT = 0xB0
    const val ACTION_VARIABLE = 0xB1
    const val ACTION_COLOUR_CODE = 0xB2
    const val ACTION_END = 0xBF
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class OpenSpiralBitcodeParser(val flow: InputFlow, val visitor: OpenSpiralBitcodeVisitor) {
    companion object {
        const val PREFIX = "osl.bitcode.parser"
    }

    @ExperimentalUnsignedTypes
    suspend fun parse(context: SpiralContext) {
        try {
            with(context) {
                val notEnoughData: () -> String = { localise("$PREFIX.not_enough_data") }

                while (true) {
                    when (flow.read() ?: return) {
                        OpenSpiralBitcode.OPERATION_SET_VERSION -> setVersion(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_DIALOGUE -> addDialogue(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_DIALOGUE_VARIABLE -> addDialogue(notEnoughData)

                        OpenSpiralBitcode.OPERATION_ADD_PLAIN_OPCODE -> addPlainOpcode(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_PLAIN_OPCODE_NAMED -> addPlainOpcodeNamed(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_VARIABLE_OPCODE -> addVariableOpcode(notEnoughData)
                        OpenSpiralBitcode.OPERATION_ADD_VARIABLE_OPCODE_NAMED -> addVariableOpcodeNamed(notEnoughData)

                        OpenSpiralBitcode.OPERATION_SET_VARIABLE -> setVariable(notEnoughData)
                    }
                }
            }
        } finally {
            visitor.end()
        }
    }

    private suspend fun SpiralContext.setVersion(notEnoughData: () -> String) {
        val major = requireNotNull(flow.read(), notEnoughData)
        val minor = requireNotNull(flow.read(), notEnoughData)
        val patch = requireNotNull(flow.read(), notEnoughData)

        visitor.setVersion(SemanticVersion(major, minor, patch))
    }

    private suspend fun SpiralContext.addDialogue(notEnoughData: () -> String) {
        val speakerName = flow.readNullTerminatedUTF8String()
        val dialogue = readArg(notEnoughData)

        visitor.addDialogue(speakerName, dialogue)
    }

    private suspend fun SpiralContext.addDialogueVariable(notEnoughData: () -> String) {
        val variableName = flow.readNullTerminatedUTF8String()
        val dialogue = readArg(notEnoughData)

        val data = visitor.getData(variableName) ?: return
        when (data) {
            is OSLUnion.NumberType -> visitor.addDialogue(data.number.toInt(), dialogue)
            else -> visitor.addDialogue(visitor.stringify(data), dialogue)
        }
    }

    private suspend fun SpiralContext.addPlainOpcode(notEnoughData: () -> String) {
        val opcode = requireNotNull(flow.read(), notEnoughData)
        val argumentCount = requireNotNull(flow.read(), notEnoughData)
        val arguments = IntArray(argumentCount) { requireNotNull(flow.readVariableInt16(), notEnoughData) }

        visitor.addPlainOpcode(opcode, arguments)
    }

    private suspend fun SpiralContext.addPlainOpcodeNamed(notEnoughData: () -> String) {
        val name = flow.readNullTerminatedUTF8String()
        val argumentCount = requireNotNull(flow.read(), notEnoughData)
        val arguments = IntArray(argumentCount) { requireNotNull(flow.readVariableInt16(), notEnoughData) }

        visitor.addPlainOpcodeNamed(name, arguments)
    }

    private suspend fun SpiralContext.addVariableOpcode(notEnoughData: () -> String) {
        val opcode = requireNotNull(flow.read(), notEnoughData)
        val argumentCount = requireNotNull(flow.read(), notEnoughData)
        val arguments = Array(argumentCount) { readArg(notEnoughData) }

        visitor.addVariableOpcode(opcode, arguments)
    }

    private suspend fun SpiralContext.addVariableOpcodeNamed(notEnoughData: () -> String) {
        val name = flow.readNullTerminatedUTF8String()
        val argumentCount = requireNotNull(flow.read(), notEnoughData)
        val arguments = Array(argumentCount) { readArg(notEnoughData) }

        visitor.addVariableOpcodeNamed(name, arguments)
    }

    private suspend fun SpiralContext.setVariable(notEnoughData: () -> String) {
        val variableName = flow.readNullTerminatedUTF8String()
        val value = readArg(notEnoughData)

        visitor.setData(variableName, value)
    }

    private suspend fun SpiralContext.parseLongReference(): String = buildString {
        while (true) {
            when (flow.read() ?: return@buildString) {
                OpenSpiralBitcode.LONG_REFERENCE_TEXT -> append(flow.readNullTerminatedUTF8String())
                OpenSpiralBitcode.LONG_REFERENCE_VARIABLE -> {
                    visitor.getData(flow.readNullTerminatedUTF8String())
                            ?.let { visitor.stringify(it) }
                            ?.let(this::append)
                }
                OpenSpiralBitcode.LONG_REFERENCE_COLOUR_CODE -> {
                    visitor.colourCodeFor(flow.readNullTerminatedUTF8String())
                            ?.let(this::append)
                }
                OpenSpiralBitcode.LONG_REFERENCE_END -> {
                    visitor.closeLongReference()
                            ?.let(this::append)

                    return@buildString
                }
            }
        }
    }

    private suspend fun SpiralContext.readArg(notEnoughData: () -> String) =
            when (val variable = requireNotNull(flow.read(), notEnoughData)) {
                OpenSpiralBitcode.VARIABLE_RAW -> OSLUnion.NumberType(requireNotNull(flow.readVariableInt16(), notEnoughData))
                OpenSpiralBitcode.VARIABLE_TEXT -> OSLUnion.RawStringType(flow.readNullTerminatedUTF8String())
                OpenSpiralBitcode.VARIABLE_LABEL -> OSLUnion.LabelType(flow.readNullTerminatedUTF8String())
                OpenSpiralBitcode.VARIABLE_LONG_LABEL -> OSLUnion.LabelType(parseLongReference())
                OpenSpiralBitcode.VARIABLE_PARAMETER -> OSLUnion.ParameterType(flow.readNullTerminatedUTF8String())
                OpenSpiralBitcode.VARIABLE_LONG_PARAMETER -> OSLUnion.ParameterType(parseLongReference())
                OpenSpiralBitcode.VARIABLE_LONG_REFERENCE -> OSLUnion.RawStringType(parseLongReference())
                OpenSpiralBitcode.VARIABLE_BOOL -> OSLUnion.BooleanType(requireNotNull(flow.read(), notEnoughData) != 0)
                else -> throw IllegalArgumentException("Invalid variable: $variable")
            }
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun <T : OpenSpiralBitcodeVisitor> InputFlow.parseOpenSpiralBitcode(context: SpiralContext, visitor: T): T {
    val magic = requireNotNull(readInt32LE()) { context.localise("${OpenSpiralBitcodeParser.PREFIX}.not_enough_data") }
    require(magic == OpenSpiralBitcode.MAGIC_NUMBER_LE) { context.localise("${OpenSpiralBitcodeParser.PREFIX}.invalid_magic") }
    val parser = OpenSpiralBitcodeParser(this, visitor)
    parser.parse(context)
    return visitor
}