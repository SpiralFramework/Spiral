package info.spiralframework.console.jvm.commands.panels

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.toolkit.common.mapToArray
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.jvm.select
import info.spiralframework.core.common.formats.SpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.images.PreferredImageFormat
import info.spiralframework.core.common.formats.images.SHTXFormat
import info.spiralframework.core.formats.images.JPEGFormat
import info.spiralframework.core.formats.images.PNGFormat
import info.spiralframework.core.formats.images.TGAFormat
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

    object PreferredImageFormatProperty: ISpiralProperty<WritableSpiralFormat> {
        override val name: String = "PreferredImage"
        override val aliases: Array<String> = emptyArray()
        override val key: ISpiralProperty.PropertyKey<WritableSpiralFormat> = PreferredImageFormat
        val imageFormats = arrayOf<WritableSpiralFormat>(PNGFormat, JPEGFormat, TGAFormat, SHTXFormat)

        override suspend fun fillIn(context: SpiralContext, writeContext: SpiralProperties?, data: Any?): KorneaResult<SpiralProperties> {
            println("An image format is required for this operation.")

            return select("Please select a format: ", imageFormats.mapToArray(SpiralFormat::name), imageFormats)
                .map { format -> (writeContext ?: SpiralProperties()).with(PreferredImageFormat, format) }
        }
    }
}