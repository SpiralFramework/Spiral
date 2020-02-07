package info.spiralframework.osl.games.v3

import info.spiralframework.formats.common.games.DRv3
import info.spiralframework.formats.common.scripting.wrd.CustomWordScript
import info.spiralframework.formats.common.scripting.wrd.UnknownWrdEntry
import info.spiralframework.formats.common.scripting.wrd.WordScriptValue
import info.spiralframework.osb.common.OSLUnion
import info.spiralframework.osl.games.DRGameVisitor

@ExperimentalUnsignedTypes
open class V3Visitor(val game: DRv3) {
    //TODO: Make private again
    val customWrd = CustomWordScript()

    fun handleScriptLine(line: OSLUnion) {
        when (line) {
            is OSLUnion.NumberType -> {
            }
            is OSLUnion.StringType -> {
            }
            is OSLUnion.BooleanType -> {
            }
            OSLUnion.UndefinedType -> {
            }
            OSLUnion.NullType -> {
            }
            OSLUnion.NoOpType -> {
            }
        }
    }

//    fun entryForName(name: String, arguments: IntArray): OSLUnion {
//        val (opcode, triple) = game.wrdOpcodeMap
//                .entries.firstOrNull { (_, value) -> value.names?.any { opCodeName -> name.equals(opCodeName, true) } == true }
//                ?: return OSLUnion.UndefinedType
//        return OSLUnion.WrdEntryType(triple.entryConstructor(opcode, WordScriptValue.parse(arguments, customWrd.labels, customWrd.parameters, customWrd.strings, game.wrdOpcodeCommandType[opcode])))
//    }
//
//    fun entryForOpCode(opcode: Int, arguments: IntArray): OSLUnion {
//        val scriptOpcode = game.wrdOpcodeMap[opcode]
//        val parsedArguments = WordScriptValue.parse(arguments, customWrd.labels, customWrd.parameters, customWrd.strings, game.wrdOpcodeCommandType[opcode])
//    }

    fun handleArgumentForEntry(arguments: MutableList<Int>, argument: OSLUnion) {
        when (argument) {
            is OSLUnion.NumberType -> arguments.add(argument { toInt() and 0xFFFF })
            is OSLUnion.RawStringType -> arguments.add(argument { customWrd.addText(this) })
            is OSLUnion.LabelType -> arguments.add(argument { customWrd.addLabel(this) })
            is OSLUnion.ParameterType -> arguments.add(argument { customWrd.addParameter(this) })
            is OSLUnion.BooleanType -> TODO("Little more research on this")
            OSLUnion.UndefinedType -> {
            }
            OSLUnion.NullType -> {
            }
            OSLUnion.NoOpType -> {
            }
        }
    }

    fun clearCltCode(builder: StringBuilder): Boolean {
        builder.append("<CLT=cltNORMAL>")
        return true
    }

    fun handleCltCode(builder: StringBuilder, code: String): Boolean {
        builder.append("<CLT=$code>")
        return true
    }

    //CaptainSwag101#0482: V3 does not terminate CLT commands
    fun closeCltCode(builder: StringBuilder) {}

//    fun scriptResult(): OSLUnion = OSLUnion.CustomWrdType(customWrd)
}