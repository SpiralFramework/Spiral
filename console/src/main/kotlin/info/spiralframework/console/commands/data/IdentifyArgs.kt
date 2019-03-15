package info.spiralframework.console.commands.data

import info.spiralframework.formats.game.DRGame
import java.io.File

class IdentifyArgs {
    data class Immutable(val identifying: File?, val game: DRGame?)

    var identifying: File? = null
    var game: DRGame? = null

    fun makeImmutable(
            defaultIdentifying: File? = null,
            defaultGame: DRGame? = null
    ): IdentifyArgs.Immutable =
            Immutable(
                    identifying ?: defaultIdentifying,
                    game ?: defaultGame
            )
}