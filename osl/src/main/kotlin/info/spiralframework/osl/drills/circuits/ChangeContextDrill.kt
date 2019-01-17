package info.spiralframework.osl.drills.circuits

import info.spiralframework.osl.GameContext
import info.spiralframework.osl.LineMatcher
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.game.hpa.UDG
import info.spiralframework.formats.game.hpa.UnknownHopesPeakGame
import info.spiralframework.formats.game.v3.V3
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object ChangeContextDrill : DrillHead<info.spiralframework.osl.GameContext> {
    val cmd = "CHANGE-CONTEXT"
    val games = HashMap<String, info.spiralframework.osl.GameContext>().apply {
        DR1.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.DR1GameContext) }
        DR2.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.DR2GameContext) }
        UDG.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.UDGGameContext) }

        V3.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.V3GameContextObject) }

        UnknownHopesPeakGame.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.UnknownHopesPeakGameContext) }

        this["STX"] = info.spiralframework.osl.GameContext.STXGameContext.INSTANCE
        this["STXT"] = info.spiralframework.osl.GameContext.STXGameContext.INSTANCE

        DR1.names.forEach { name -> put("Nonstop Debate ($name)".toUpperCase(), info.spiralframework.osl.GameContext.DR1NonstopDebateMinigameContext) }
        DR2.names.forEach { name -> put("Nonstop Debate ($name)".toUpperCase(), info.spiralframework.osl.GameContext.DR2NonstopDebateMinigameContext) }

        DR1.names.forEach { name -> put("Nonstop Debate Data ($name)".toUpperCase(), info.spiralframework.osl.GameContext.DR1NonstopDebateDataContext) }
        DR2.names.forEach { name -> put("Nonstop Debate Data ($name)".toUpperCase(), info.spiralframework.osl.GameContext.DR2NonstopDebateDataContext) }

        DR1.names.forEach { name -> put("$name Trial".toUpperCase(), info.spiralframework.osl.GameContext.DR1TrialContext) }
        DR2.names.forEach { name -> put("$name Trial".toUpperCase(), info.spiralframework.osl.GameContext.DR2TrialContext) }
    }

    override val klass: KClass<info.spiralframework.osl.GameContext> = info.spiralframework.osl.GameContext::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            FirstOf("Game Context:", "Context:", "Game Context Is ", "Context Is ", "Set Context To ", "Set Game Context To "),
                            pushDrillHead(cmd, this@ChangeContextDrill),
                            OptionalInlineWhitespace(),
                            OneOrMore(LineMatcher),
                            pushTmpAction(cmd),
                            Action<Any> { tmpStack[cmd]?.peek()?.toString()?.toUpperCase().let { key -> key == "NULL" || key in games } },
                            operateOnTmpActions(cmd) { stack -> operate(this, stack.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) }
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): info.spiralframework.osl.GameContext? {
        if (parser.silence)
            return null

        val context = games[rawParams[0].toString().toUpperCase()]
        parser.gameContext = context

        return context
    }
}
