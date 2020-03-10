package info.spiralframework.console.commands.data

import info.spiralframework.formats.game.DRGame
import java.io.File

class LinkContentArgs {
    data class Immutable(val workspacePath: File?, val baseGamePath: File?, val game: DRGame?, val linkingContentName: String?, val linkingContentPath: File?, val createLink: Boolean, val filter: Regex?)

    var workspacePath: File? = null
    var baseGamePath: File? = null
    var game: DRGame? = null
    var linkingContentName: String? = null
    var linkingContentPath: File? = null
    var createLink: Boolean = true
    var filter: Regex? = null
    var builder: Boolean = false

    fun makeImmutable(
        defaultWorkspacePath: File? = null,
        defaultBaseGamePath: File? = null,
        defaultGame: DRGame? = null,
        defaultLinkingContentName: String? = null,
        defaultLinkingContentPath: File? = null,
        defaultFilter: Regex? = null
    ): Immutable =
        Immutable(
            workspacePath ?: defaultWorkspacePath,
            baseGamePath ?: defaultBaseGamePath,
            game ?: defaultGame,
            linkingContentName ?: defaultLinkingContentName,
            linkingContentPath ?: defaultLinkingContentPath,
            createLink,
            filter ?: defaultFilter
        )
}