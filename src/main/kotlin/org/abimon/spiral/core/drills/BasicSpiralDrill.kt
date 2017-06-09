package org.abimon.spiral.core.drills

import org.abimon.spiral.core.lin.*
import org.abimon.util.*
import org.abimon.visi.io.errPrintln
import org.parboiled.BaseParser
import org.parboiled.Rule

object BasicSpiralDrill : DrillHead {
    val cmd = "BASIC"

    override fun Syntax(parser: BaseParser<Any>): Rule = parser.makeCommand {
        Sequence(
                clearTmpStack(cmd),
                "0x",
                OneOrMore(Digit(16)),
                pushTmpAction(this, cmd, this@BasicSpiralDrill),
                pushTmpAction(this, cmd),
                Optional(
                        '|',
                        ParamList(
                                cmd,
                                Sequence(
                                        OneOrMore(Digit()),
                                        pushToStack(this)
                                ),
                                Sequence(
                                        ',',
                                        Optional(Whitespace())
                                )
                        )
                ),
                pushTmpStack(this, cmd)
        )
    }

    override fun formScript(rawParams: Array<Any>): LinScript {
        val opCode = "${rawParams[0]}".toInt(16)
        val params = rawParams.copyOfRange(1, rawParams.size).map { "$it".toIntOrNull() }.filterNotNull().toIntArray()
        when(opCode) {
            0x00 -> if(params.size == 1) return TextCountEntry(params[0]) else if (params.size == 2) return TextCountEntry(params[0], params[1]) else throw throw IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires either 1 or 2 parameters, got ${params.size}")
            0x01 -> { ensure(0x01, 3, params); return unknown(0x01, params) }
            0x02 -> {
                errPrintln("[Basic Spiral Drill] LIN script with op code 0x02 was called, this is not the correct way to add text!")
                return TextEntry("Lorem Ipsum")
            }
            0x03 -> { ensure(0x03, 1, params); return FormatEntry(params[0])}
            0x04 -> if(params.size == 1) return FilterEntry(params[0]) else if (params.size == 4) return FilterEntry(params[0], params[1], params[2], params[3]) else throw IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires either 1 or 4 parameters, got ${params.size}")
            0x05 -> if(params.size == 1) return MovieEntry(params[0]) else if (params.size == 2) return MovieEntry(params[0], params[1]) else throw throw IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires either 1 or 2 parameters, got ${params.size}")
            0x06 -> {
                if(params.size == 2) return AnimationEntry(params[0], params[1])
                else if(params.size == 3) return AnimationEntry(params[0], params[1], params[2])
                else if(params.size == 7) return AnimationEntry(params[0], params[1], params[2], params[3], params[4], params[5], params[6])
                else if(params.size == 8) return AnimationEntry(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7])
                else throw IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires either 2, 3, 7, or 8 parameters, got ${params.size}")
            }
            else -> return unknown(opCode, params)
        }
    }

    fun unknown(opCode: Int, params: IntArray) = UnknownEntry(opCode, params)
    fun ensure(opCode: Int, required: Int, params: IntArray): Unit = if(params.size < required) throw IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires $required, got ${params.size}") else Unit
}