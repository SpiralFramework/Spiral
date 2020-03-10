package info.spiralframework.console.commands.data

import info.spiralframework.formats.game.DRGame
import java.io.File

class PatchExecutableArgs {
    data class Immutable(val executablePath: File?, val game: DRGame?)

    var executablePath: File? = null
    var game: DRGame? = null
    var builder: Boolean = false

    fun makeImmutable(
        defaultExecutablePath: File? = null,
        defaultGame: DRGame? = null
    ): Immutable =
            Immutable(
                executablePath ?: defaultExecutablePath,
                game ?: defaultGame
            )
}