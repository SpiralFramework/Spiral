package info.spiralframework.console.commands.data

import info.spiralframework.formats.game.DRGame
import java.io.File

class ExtractWorkspaceArgs {
    data class Immutable(val workplacePath: File?, val game: DRGame?)

    var workplacePath: File? = null
    var game: DRGame? = null
    var builder: Boolean = false

    fun makeImmutable(
        defaultWorkplacePath: File? = null,
        defaultGame: DRGame? = null
    ): Immutable =
            Immutable(
                workplacePath ?: defaultWorkplacePath,
                game ?: defaultGame
            )
}