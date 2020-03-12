package info.spiralframework.console.jvm.commands.data

import info.spiralframework.formats.common.games.DrGame
import java.io.File

class PatchExecutableArgs {
    data class Immutable(val executablePath: File?, val game: DrGame?)

    var executablePath: File? = null
    var game: DrGame? = null
    var builder: Boolean = false

    fun makeImmutable(
        defaultExecutablePath: File? = null,
        defaultGame: DrGame? = null
    ): Immutable =
            Immutable(
                executablePath ?: defaultExecutablePath,
                game ?: defaultGame
            )
}