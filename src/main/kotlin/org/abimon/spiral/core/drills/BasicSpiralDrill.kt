package org.abimon.spiral.core.drills

import org.abimon.spiral.core.SpiralConfig
import org.abimon.spiral.core.lin.*
import org.abimon.spiral.core.lin.dr1.WaitForInputEntry
import org.abimon.spiral.core.lin.dr1.*
import org.abimon.spiral.util.*
import org.abimon.visi.io.errPrintln
import org.parboiled.BaseParser
import org.parboiled.Rule

//TODO: Support DR2 op codes too
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
                        '|'
                ),
                Optional(
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

    override fun formScripts(rawParams: Array<Any>, config: SpiralConfig): Array<LinScript> = arrayOf(formScript(rawParams))

    fun formScript(rawParams: Array<Any>): LinScript {
        val opCode = "${rawParams[0]}".toInt(16)
        val params = rawParams.copyOfRange(1, rawParams.size).map { "$it".toIntOrNull() }.filterNotNull().toIntArray()
        when(opCode) {
            0x00 -> {
                if(params.size == 1) return TextCountEntry(params[0])
                else if (params.size == 2) return TextCountEntry(params[0], params[1])
                else throw notEnoughParams(opCode, intArrayOf(1, 2), params.size)
            }
            0x01 -> { ensure(0x01, 3, params); return unknown(0x01, params.copyOf(3)) }
            0x02 -> {
                errPrintln("[Basic Spiral Drill] LIN script with op code 0x02 was called, this is not the correct way to add text!")
                return TextEntry("Lorem Ipsum")
            }
            0x03 -> { ensure(0x03, 1, params); return FormatEntry(params[0])}
            0x04 -> {
                if(params.size == 1) return FilterEntry(params[0])
                else if (params.size >= 4) return FilterEntry(params[0], params[1], params[2], params[3])
                else throw IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires either 1 or 4 parameters, got ${params.size}")
            }
            0x05 -> if(params.size == 1) return MovieEntry(params[0]) else if (params.size >= 2) return MovieEntry(params[0], params[1]) else throw IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires either 1 or 2 parameters, got ${params.size}")
            0x06 -> {
                if(params.size == 2) return AnimationEntry(params[0], params[1])
                else if(params.size == 3) return AnimationEntry(params[0], params[1], params[2])
                else if(params.size == 7) return AnimationEntry(params[0], params[1], params[2], params[3], params[4], params[5], params[6])
                else if(params.size >= 8) return AnimationEntry(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7])
                else throw IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires either 2, 3, 7, or 8 parameters, got ${params.size}")
            }
            0x08 -> {
                if(params.size == 3) return VoiceLineEntry(params[0], params[1], params[2])
                else if(params.size == 4) return VoiceLineEntry(params[0], params[1], params[2], params[3])
                else if(params.size >= 5) return VoiceLineEntry(params[0], params[1], params[2], params[3], params[4])
                else throw notEnoughParams(opCode, intArrayOf(3, 4, 5), params.size)
            }
            0x09 -> {
                if(params.size == 2) return MusicEntry(params[0], params[1])
                else if(params.size >= 3) return MusicEntry(params[0], params[1], params[2])
                else throw notEnoughParams(opCode, intArrayOf(2, 3), params.size)
            }
            0x0A -> {
                if(params.size == 2) return SoundEffectEntryA(params[0], params[1])
                else if(params.size >= 3) return SoundEffectEntryA(params[0], params[1], params[2])
                else throw notEnoughParams(opCode, intArrayOf(2, 3), params.size)
            }
            0x0B -> { if(params.size == 1) return SoundEffectEntryB(params[0]) else if (params.size >= 2) return SoundEffectEntryB(params[0], params[1]) else throw notEnoughParams(opCode, intArrayOf(1, 2), params.size) }
            0x0C -> { ensure(0x0C, 2, params); return TruthBulletEntry(params[0], params[1]) }
            0x0D -> { ensure(0x0D, 3, params); return unknown(0x0D, params.copyOf(3)) }
            0x0E -> { ensure(0x0E, 2, params); return unknown(0x0E, params.copyOf(2)) }
            0x0F -> {
                if(params.size == 2) return SetStudentTitleEntry(params[0], params[1])
                else if(params.size >= 3) return SetStudentTitleEntry(params[0], params[1], params[2])
                else throw notEnoughParams(opCode, intArrayOf(2, 3), params.size)
            }
            0x10 -> {
                if(params.size == 2) return SetStudentReportEntry(params[0], params[1])
                else if(params.size >= 3) return SetStudentReportEntry(params[0], params[1], params[2])
                else throw notEnoughParams(opCode, intArrayOf(2, 3), params.size)
            }
            0x11 -> { ensure(0x11, 4, params); return unknown(0x11, params.copyOf(4)) }
            0x14 -> {
                if(params.size == 2) return TrialCameraEntry(params[0], params[1])
                else if(params.size >= 3) return TrialCameraEntry(params[0], params[1], params[2])
                else throw notEnoughParams(opCode, intArrayOf(2, 3), params.size)
            }
            0x15 -> {
                if(params.size == 2) return LoadMapEntry(params[0], params[1])
                else if(params.size >= 3) return LoadMapEntry(params[0], params[1], params[2])
                else throw notEnoughParams(opCode, intArrayOf(2, 3), params.size)
            }
            0x19 -> { ensure(0x19, 3, params); return LoadScriptEntry(params[0], params[1], params[2])
            }
            0x1A -> return StopScriptEntry()
            0x1B -> { ensure(0x1B, 3, params); return RunScriptEntry(params[0], params[1], params[2])
            }
            0x1C -> return unknown(0x1C, IntArray(0))
            0x1E -> { ensure(0x1E, 5, params); return SpriteEntry(params[0], params[1], params[2], params[3], params[4]) }
            0x1F -> { ensure(0x1F, 7, params); return unknown(0x1F, params.copyOf(7)) }
            0x20 -> { ensure(0x20, 5, params); return unknown(0x20, params.copyOf(5)) }
            0x21 -> { ensure(0x21, 1, params); return SpeakerEntry(params[0]) }
            0x22 -> { ensure(0x22, 3, params); return unknown(0x22, params.copyOf(3)) }
            0x23 -> { ensure(0x23, 5, params); return unknown(0x23, params.copyOf(5)) }
            0x25 -> { ensure(0x25, 2, params); return ChangeUIEntry(params[0], params[1]) }
            0x26 -> { ensure(0x26, 3, params); return SetFlagEntry(params[0], params[1], params[2]) }
            0x27 -> { ensure(0x27, 1, params); return CheckCharacterEntry(params[0]) }
            0x29 -> { ensure(0x29, 1, params); return CheckObjectEntry(params[0]) }
            0x2A -> { ensure(0x2A, 2, params); return SetLabelEntry(params[0], params[1]) }
            0x2B -> { ensure(0x2B, 1, params); return ChoiceEntry(params[0]) }
            0x2C -> { ensure(0x2C, 2, params); return unknown(0x2C, params.copyOf(2)) }
            0x2E -> { ensure(0x2E, 2, params); return unknown(0x2E, params.copyOf(2)) }
            0x2F -> { ensure(0x2F, 10, params); return unknown(0x2F, params.copyOf(10)) } //Does it even appear?
            0x30 -> {
                if(params.size == 2) return ShowBackgroundEntry(params[0], params[1])
                else if(params.size >= 3) return ShowBackgroundEntry(params[0], params[1], params[2])
                else throw notEnoughParams(opCode, intArrayOf(2, 3), params.size)
            }
            0x32 -> { ensure(0x32, 1, params); return unknown(0x32, params.copyOf(1)) }
            0x33 -> { ensure(0x33, 4, params); return unknown(0x33, params.copyOf(4)) }
            0x34 -> { ensure(0x34, 2, params); return GoToLabelEntry(params[0], params[1]) }
            0x35 -> return CheckFlagEntryA(params)
            0x36 -> return CheckFlagEntryB(params)
            0x39 -> { ensure(0x39, 5, params); return unknown(0x39, params.copyOf(5)) }
            0x3A -> return WaitForInputEntry()
            0x3B -> return WaitFrameEntry()
            0x3C -> return EndFlagCheckEntry()

            else -> return unknown(opCode, params)
        }
    }

    fun unknown(opCode: Int, params: IntArray) = UnknownEntry(opCode, params)
    fun ensure(opCode: Int, required: Int, params: IntArray): Unit = if(params.size < required) throw notEnoughParams(opCode, required, params.size) else Unit
    fun notEnoughParams(opCode: Int, required: Int, got: Int) = IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires $required, got $got")
    /** If you call this with an array of less than two you are exactly what's wrong with this world */
    fun notEnoughParams(opCode: Int, required: IntArray, got: Int) = IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires either ${required.copyOfRange(0, required.size - 1).joinToString() + " or " + required.last()}; got $got")
}