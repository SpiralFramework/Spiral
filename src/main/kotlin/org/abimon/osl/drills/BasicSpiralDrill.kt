package org.abimon.osl.drills

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
import org.abimon.spiral.core.utils.and
import org.parboiled.Rule
import kotlin.reflect.KClass

object BasicSpiralDrill : DrillHead<LinScript> {
    val cmd = "BASIC"

    override val klass: KClass<LinScript> = LinScript::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "0x",
                    OneOrMore(Digit(16)),
                    pushTmpAction(cmd, this@BasicSpiralDrill),
                    pushTmpAction(cmd),
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
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript = formScript(rawParams, parser.game)

    fun formScript(rawParams: Array<Any>, game: HopesPeakDRGame): LinScript {
        val opCode = "${rawParams[0]}".toInt(16)
        val params = rawParams.copyOfRange(1, rawParams.size).map { str -> "$str".toIntOrNull() }.filterNotNull().toIntArray()

        val (_, argumentCount, getEntry) = game.opCodes[opCode] ?: (null to -1 and ::UnknownEntry)

        if (params.size == argumentCount || argumentCount == -1)
            return getEntry(opCode, params)
        return UnknownEntry(opCode, params)
    }

    fun unknown(opCode: Int, params: IntArray) = UnknownEntry(opCode, params)
    fun ensure(opCode: Int, required: Int, params: IntArray): Unit = if (params.size < required) throw notEnoughParams(opCode, required, params.size) else Unit
    fun notEnoughParams(opCode: Int, required: Int, got: Int) = IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires $required, got $got")
    /** If you call this with an array of less than two you are exactly what's wrong with this world */
    fun notEnoughParams(opCode: Int, required: IntArray, got: Int) = IllegalArgumentException("[Basic Spiral Drill] LIN script with op code $opCode called; requires either ${required.copyOfRange(0, required.size - 1).joinToString() + " or " + required.last()}; got $got")
}