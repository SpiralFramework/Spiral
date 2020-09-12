package info.spiralframework.console.jvm.data

import info.spiralframework.formats.common.games.DrGame
import java.io.File

class IdentifyArgs {
    data class Immutable(val identifying: File?, val game: DrGame?)

    var identifying: File? = null
    var game: DrGame? = null

    fun makeImmutable(
            defaultIdentifying: File? = null,
            defaultGame: DrGame? = null
    ): Immutable =
            Immutable(
                    identifying ?: defaultIdentifying,
                    game ?: defaultGame
            )
}