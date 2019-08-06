package info.spiralframework.osl.drills.circuits

import info.spiralframework.osl.GameContext
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

object ChangeGameDrill : DrillHead<info.spiralframework.osl.GameContext> {
    val cmd = "CHANGE-GAME"
    val games = HashMap<String, info.spiralframework.osl.GameContext>().apply {
        DR1.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.DR1GameContext) }
        DR2.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.DR2GameContext) }
        UDG.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.UDGGameContext) }

        V3.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.V3GameContextObject) }

        UnknownHopesPeakGame.names.forEach { name -> put(name.toUpperCase(), info.spiralframework.osl.GameContext.UnknownHopesPeakGameContext) }
    }

    override val klass: KClass<info.spiralframework.osl.GameContext> = info.spiralframework.osl.GameContext::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            FirstOf("Game:", "Game Is ", "Set Game To "),
                            pushDrillHead(cmd, this@ChangeGameDrill),
                            OptionalInlineWhitespace(),
                            Parameter(cmd),
                            Action<Any> { tmpStack[cmd]?.peek()?.toString()?.toUpperCase() in games },
                            operateOnTmpActions(cmd) { stack -> operate(this, stack.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) }
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): info.spiralframework.osl.GameContext? {
        if (parser.silence)
            return null

        val context = games[rawParams[0].toString().toUpperCase()]
        parser.gameContext =  context

        return context
    }
}
