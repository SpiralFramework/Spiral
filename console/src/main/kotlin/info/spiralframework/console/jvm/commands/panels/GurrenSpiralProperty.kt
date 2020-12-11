package info.spiralframework.console.jvm.commands.panels

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.korneaNotEnoughData
import dev.brella.kornea.errors.common.map
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.base.common.text.toIntOrNullBaseN
import info.spiralframework.formats.common.games.DrGame
import kotlinx.coroutines.yield

object GurrenSpiralProperty {
    object LinScriptable : ISpiralProperty<DrGame.LinScriptable> {
        override val name: String = "DrGame.LinScriptable"
        override val key: ISpiralProperty.PropertyKey<DrGame.LinScriptable> = DrGame.LinScriptable

        @Suppress("UNREACHABLE_CODE")
        override suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any): KorneaResult<SpiralProperties> {
            if (writeContext[DrGame.LinScriptable] != null) return KorneaResult.success(writeContext!!)

//            if (context is SpiralCockpitContext) {}

            val keys = DrGame.LinScriptable.NAMES

            println("A game object is required for this operation.")
            val string = "Please select a game:\n${keys.mapIndexed { index, name -> "\t${index + 1}) $name" }.joinToString("\n", postfix = "\n\t${keys.size + 1}) Exit")}"

            while (true) {
                yield()

                println(string)
                print("> ")

                val input = readLine() ?: return korneaNotEnoughData("stdin broken")

                if (input.equals("exit", true) || input.equals("break", true) || input.equals("quit", true)) return KorneaResult.empty()

                val inputAsNum = input.trim().toIntOrNullBaseN()?.minus(1)

                if (inputAsNum != null && inputAsNum in keys.indices) {
                    println("Selected: '${keys[inputAsNum]}'")

                    return DrGame.LinScriptable.VALUES[keys[inputAsNum]]?.invoke(context)?.map { game ->
                        (writeContext ?: SpiralProperties()).with(DrGame.LinScriptable, game)
                    } ?: KorneaResult.empty()
                }

                val gameConstructor = DrGame.LinScriptable.VALUES.entries.groupBy({ (k) -> k.commonPrefixWith(input, true).length }, { (_, v) -> v })
                    .entries
                    .maxByOrNull(Map.Entry<Int, *>::key)
                    ?.let { (k, v) -> if (v.size == 1) v.first() else null }

                if (gameConstructor == null) {
                    println("Sorry, '$input' is a little ambiguous, try again maybe?")
                } else {
                    return gameConstructor(context).map { game -> (writeContext ?: SpiralProperties()).with(DrGame.LinScriptable, game) }
                }
            }

            return KorneaResult.empty()
        }
    }
}