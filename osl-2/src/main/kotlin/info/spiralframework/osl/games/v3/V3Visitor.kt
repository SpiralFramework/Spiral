package info.spiralframework.osl.games.v3

import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.scripting.CustomWordScript
import info.spiralframework.formats.scripting.wrd.UnknownEntry
import info.spiralframework.osl.OSLUnion
import info.spiralframework.osl.games.DRGameVisitor

open class V3Visitor(val game: V3): DRGameVisitor {
    private val customWrd = CustomWordScript()

    override fun handleScriptLine(line: OSLUnion) {
        when (line) {
            is OSLUnion.NumberType -> {}
            is OSLUnion.StringType -> {}
            is OSLUnion.BooleanType -> {}
            is OSLUnion.LinEntryType -> {}
            is OSLUnion.CustomLinType -> {}
            is OSLUnion.WrdEntryType -> line(customWrd::add)
            is OSLUnion.CustomWrdType -> {}
            OSLUnion.UndefinedType -> {}
            OSLUnion.NullType -> {}
            OSLUnion.NoOpType -> {}
        }
    }

    override fun entryForName(name: String, arguments: IntArray): OSLUnion {
        val (opCode, triple) = game.opCodes.entries.firstOrNull { (_, value) -> value.first.any { opCodeName -> name.equals(opCodeName, true) } }
                ?: return OSLUnion.UndefinedType
        return OSLUnion.WrdEntryType(triple.third(opCode, arguments))
    }

    override fun entryForOpCode(opCode: Int, arguments: IntArray): OSLUnion =
            OSLUnion.WrdEntryType(UnknownEntry(opCode, arguments))

    override fun handleArgumentForEntry(arguments: MutableList<Int>, argument: OSLUnion) {
        when (argument) {
            is OSLUnion.NumberType -> arguments.add(argument { toInt() and 0xFFFF })
            is OSLUnion.RawStringType -> arguments.add(argument { customWrd.string(this) })
            is OSLUnion.LabelType -> arguments.add(argument { customWrd.label(this) })
            is OSLUnion.ParameterType -> arguments.add(argument { customWrd.parameter(this) })
            is OSLUnion.BooleanType -> TODO("Little more research on this")
            is OSLUnion.LinEntryType -> {}
            is OSLUnion.CustomLinType -> {}
            is OSLUnion.WrdEntryType -> arguments.addAll(argument { rawArguments.toList() })
            is OSLUnion.CustomWrdType -> {}
            OSLUnion.UndefinedType -> {}
            OSLUnion.NullType -> {}
            OSLUnion.NoOpType -> {}
        }
    }

    override fun clearCltCode(builder: StringBuilder): Boolean {
        builder.append("<CLT=cltNORMAL>")
        return true
    }
    override fun handleCltCode(builder: StringBuilder, code: String): Boolean {
        builder.append("<CLT=$code>")
        return true
    }
    //CaptainSwag101#0482: V3 does not terminate CLT commands
    override fun closeCltCode(builder: StringBuilder) {}

    override fun scriptResult(): OSLUnion = OSLUnion.CustomWrdType(customWrd)
}