package info.spiralframework.osl.games.hpa

import info.spiralframework.base.jvm.addAll
import info.spiralframework.base.util.toInt16LE
import info.spiralframework.formats.game.hpa.HopesPeakDRGame
import info.spiralframework.formats.scripting.CustomLin
import info.spiralframework.formats.scripting.lin.UnknownEntry
import info.spiralframework.osl.OSLUnion
import info.spiralframework.osl.games.DRGameVisitor

open class HopesPeakVisitor(val game: HopesPeakDRGame) : DRGameVisitor {
    private val customLin = CustomLin()

    override fun handleScriptLine(line: OSLUnion) {
        when (line) {
            is OSLUnion.NumberType -> {}
            is OSLUnion.StringType -> {}
            is OSLUnion.BooleanType -> {}
            is OSLUnion.LinEntryType -> line(customLin::add)
            is OSLUnion.CustomLinType -> {}
            is OSLUnion.WrdEntryType -> {}
            is OSLUnion.CustomWrdType -> {}
            OSLUnion.UndefinedType -> {}
            OSLUnion.NullType -> {}
            OSLUnion.NoOpType -> {}
        }
    }

    override fun entryForName(name: String, arguments: IntArray): OSLUnion {
        val (opCode, tuple) = game.opCodes.entries.firstOrNull { (_, value) -> value.first.any { opCodeName -> name.equals(opCodeName, true) } }
                ?: return OSLUnion.UndefinedType

        return OSLUnion.LinEntryType(tuple.third(opCode, arguments))
    }

    override fun entryForOpCode(opCode: Int, arguments: IntArray): OSLUnion =
            OSLUnion.LinEntryType(UnknownEntry(opCode, arguments))

    override fun handleArgumentForEntry(arguments: MutableList<Int>, argument: OSLUnion) {
        when (argument) {
            is OSLUnion.NumberType -> arguments.add(argument(Number::toInt))
            is OSLUnion.StringType -> arguments.addAll(argument { customLin.addText(this) }.toInt16LE())
            is OSLUnion.BooleanType -> arguments.add(argument { if (this) 1 else 0 })
            is OSLUnion.LinEntryType -> arguments.addAll(argument { rawArguments.toList() })
            is OSLUnion.CustomLinType -> {}
            is OSLUnion.WrdEntryType -> {}
            is OSLUnion.CustomWrdType -> {}
            OSLUnion.UndefinedType -> {}
            OSLUnion.NullType -> {}
            OSLUnion.NoOpType -> {}
        }
    }

    override fun handleCltCode(builder: StringBuilder, code: String): Boolean {
        builder.append("<CLT ${game.colourCodes[code] ?: code}>")
        return true
    }

    override fun clearCltCode(builder: StringBuilder): Boolean {
        builder.append("<CLT>")
        return true
    }

    override fun closeCltCode(builder: StringBuilder) {
        builder.append("<CLT>")
    }

    override fun scriptResult(): OSLUnion = OSLUnion.CustomLinType(customLin)
}