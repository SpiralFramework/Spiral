package info.spiralframework.console.jvm.commands.data

import info.spiralframework.formats.common.games.DrGame
import java.io.File

class ExtractWorkspaceArgs {
    data class Immutable(val workplacePath: File?, val game: DrGame?)

    var workplacePath: File? = null
    var game: DrGame? = null
    var builder: Boolean = false

    fun makeImmutable(
        defaultWorkplacePath: File? = null,
        defaultGame: DrGame? = null
    ): Immutable =
            Immutable(
                workplacePath ?: defaultWorkplacePath,
                game ?: defaultGame
            )
}