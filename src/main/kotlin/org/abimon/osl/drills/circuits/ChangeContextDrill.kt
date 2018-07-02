package org.abimon.osl.drills.circuits

import org.abimon.osl.GameContext
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.UDG
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.game.v3.V3
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object ChangeContextDrill : DrillHead<GameContext> {
    val cmd = "CHANGE-CONTEXT"
    val games = HashMap<String, GameContext>().apply {
        DR1.names.forEach { name -> put(name.toUpperCase(), GameContext.DR1GameContext) }
        DR2.names.forEach { name -> put(name.toUpperCase(), GameContext.DR2GameContext) }
        UDG.names.forEach { name -> put(name.toUpperCase(), GameContext.UDGGameContext) }

        V3.names.forEach { name -> put(name.toUpperCase(), GameContext.V3GameContextObject) }

        UnknownHopesPeakGame.names.forEach { name -> put(name.toUpperCase(), GameContext.UnknownHopesPeakGameContext) }

        this["STX"] = GameContext.STXGameContext.INSTANCE
        this["STXT"] = GameContext.STXGameContext.INSTANCE

        DR1.names.forEach { name -> put("Nonstop Debate ($name)".toUpperCase(), GameContext.DR1NonstopDebateContext) }
        DR2.names.forEach { name -> put("Nonstop Debate ($name)".toUpperCase(), GameContext.DR2NonstopDebateContext) }
    }

    override val klass: KClass<GameContext> = GameContext::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            FirstOf("Game Context:", "Context:", "Game Context Is ", "Context Is ", "Set Context To ", "Set Game Context To "),
                            pushDrillHead(cmd, this@ChangeContextDrill),
                            OptionalInlineWhitespace(),
                            Parameter(cmd),
                            Action<Any> { tmpStack[cmd]?.peek()?.toString()?.toUpperCase().let { key -> key == "NULL" || key in games } },
                            operateOnTmpActions(cmd) { stack -> operate(this, stack.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) }
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): GameContext? {
        if (parser.silence)
            return null

        val context = games[rawParams[0].toString().toUpperCase()]
        parser.gameContext = context

        return context
    }
}