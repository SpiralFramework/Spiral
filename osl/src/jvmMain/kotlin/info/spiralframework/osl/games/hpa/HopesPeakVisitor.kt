package info.spiralframework.osl.games.hpa

import dev.brella.kornea.io.common.flow.extensions.toInt16LE
import info.spiralframework.base.jvm.addAll
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.CustomLinScript
import info.spiralframework.osb.common.OSLUnion

public open class HopesPeakVisitor(public val game: DrGame.LinScriptable) {
    private val customLin = CustomLinScript()

    public fun handleScriptLine(line: OSLUnion) {
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
            else -> {}
        }
    }

    public fun handleArgumentForEntry(arguments: MutableList<Int>, argument: OSLUnion) {
        when (argument) {
            is OSLUnion.NumberType -> arguments.add(argument(Number::toInt))
            is OSLUnion.StringType -> arguments.addAll(argument { customLin.addText(this) }.toInt16LE())
            is OSLUnion.BooleanType -> arguments.add(argument { if (this) 1 else 0 })
            OSLUnion.UndefinedType -> {
            }
            OSLUnion.NullType -> {
            }
            OSLUnion.NoOpType -> {
            }
            else -> {}
        }
    }

    public fun handleCltCode(builder: StringBuilder, code: String): Boolean {
        builder.append("<CLT ${game.linColourCodes[code] ?: code}>")
        return true
    }

    public fun clearCltCode(builder: StringBuilder): Boolean {
        builder.append("<CLT>")
        return true
    }

    public fun closeCltCode(builder: StringBuilder) {
        builder.append("<CLT>")
    }
}