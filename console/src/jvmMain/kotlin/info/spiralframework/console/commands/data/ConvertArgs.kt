package info.spiralframework.console.commands.data

import info.spiralframework.formats.game.DRGame
import java.io.File

class ConvertArgs {
    data class Immutable(val converting: File?, var from: String?, var to: String?, var filter: Regex?, var game: DRGame?)

    var converting: File? = null
    var from: String? = null
    var to: String? = null
    var filter: Regex? = null
    var game: DRGame? = null
    var builder: Boolean = false

    fun makeImmutable(
            defaultConverting: File? = null,
            defaultFrom: String? = null,
            defaultTo: String? = null,
            defaultFilter: Regex? = null,
            defaultGame: DRGame? = null
    ): ConvertArgs.Immutable =
            Immutable(
                    converting ?: defaultConverting,
                    from ?: defaultFrom,
                    to ?: defaultTo,
                    filter ?: defaultFilter,
                    game ?: defaultGame
            )
}