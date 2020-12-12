package info.spiralframework.console.jvm.commands.panels

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.map
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.jvm.select
import info.spiralframework.formats.common.games.DrGame

object GurrenSpiralProperty {
    object Game : ISpiralProperty<DrGame> {
        override val name: String = "DrGame"
        override val aliases: Array<String> = arrayOf("Game")
        override val key: ISpiralProperty.PropertyKey<DrGame> = DrGame

        override suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any?): KorneaResult<SpiralProperties> {
            println("A game object is required for this operation.")

            return select("Please select a game: ", DrGame.NAMES, DrGame.VALUES)
                .flatMap { constructor -> constructor(context) }
                .map { game -> (writeContext ?: SpiralProperties()).with(DrGame, game) }
        }
    }

    object LinScriptable : ISpiralProperty<DrGame.LinScriptable> {
        override val name: String = "DrGame.LinScriptable"
        override val aliases: Array<String> = arrayOf("Game.LinScriptable", "LinScriptable")
        override val key: ISpiralProperty.PropertyKey<DrGame.LinScriptable> = DrGame.LinScriptable

        @Suppress("UNREACHABLE_CODE")
        override suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any?): KorneaResult<SpiralProperties> {
            println("A game object is required for this operation.")

            return select("Please select a game: ", DrGame.LinScriptable.NAMES, DrGame.LinScriptable.VALUES)
                .flatMap { constructor -> constructor(context) }
                .map { game -> (writeContext ?: SpiralProperties()).with(DrGame.LinScriptable, game) }
        }
    }
}