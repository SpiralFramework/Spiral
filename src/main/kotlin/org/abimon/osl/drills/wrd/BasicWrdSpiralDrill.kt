package org.abimon.osl.drills.wrd

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.wrd.UnknownEntry
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.abimon.spiral.core.utils.and
import org.parboiled.Rule
import kotlin.reflect.KClass

object BasicWrdSpiralDrill : DrillHead<WrdScript> {
    val cmd = "BASIC-WRD"

    override val klass: KClass<WrdScript> = WrdScript::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "0x",
                    OneOrMore(Digit(16)),
                    pushTmpAction(cmd, this@BasicWrdSpiralDrill),
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

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WrdScript = formScript(rawParams)

    fun formScript(rawParams: Array<Any>): WrdScript {
        val opCode = "${rawParams[0]}".toInt(16)
        val params = rawParams.copyOfRange(1, rawParams.size).map { str -> "$str".toIntOrNull() }.filterNotNull().toIntArray()

        val (_, argumentCount, getEntry) = V3.opCodes[opCode] ?: (null to -1 and ::UnknownEntry)

        if (params.size == argumentCount || argumentCount == -1)
            return getEntry(opCode, params)
        return UnknownEntry(opCode, params)
    }
}