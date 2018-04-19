package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
import org.abimon.spiral.core.utils.and
import org.parboiled.Rule
import kotlin.reflect.KClass

object BasicLinSpiralDrill : DrillHead<LinScript> {
    val cmd = "BASIC-LIN"

    override val klass: KClass<LinScript> = LinScript::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "0x",
                    OneOrMore(Digit(16)),
                    pushTmpAction(cmd, this@BasicLinSpiralDrill),
                    pushTmpAction(cmd),
                    Optional(
                            '|'
                    ),
                    Optional(
                            ParamList(
                                    cmd,
                                    Sequence(
                                            OneOrMore(Digit()),
                                            pushToStack()
                                    ),
                                    Sequence(
                                            ',',
                                            OptionalWhitespace()
                                    )
                            )
                    ),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript = formScript(rawParams, parser.game as? HopesPeakDRGame
            ?: UnknownHopesPeakGame)

    fun formScript(rawParams: Array<Any>, game: HopesPeakDRGame): LinScript {
        val opCode = "${rawParams[0]}".toInt(16)
        val params = rawParams.copyOfRange(1, rawParams.size).map { str -> "$str".toIntOrNull() }.filterNotNull().toIntArray()

        val (_, argumentCount, getEntry) = game.opCodes[opCode] ?: (null to -1 and ::UnknownEntry)

        if (params.size == argumentCount || argumentCount == -1)
            return getEntry(opCode, params)
        return UnknownEntry(opCode, params)
    }
}