package info.spiralframework.console.jvm.commands.data

import info.spiralframework.formats.common.games.DrGame
import java.io.File

class ConvertArgs {
    data class Immutable(val converting: File?, val from: String?, val to: String?, val filter: Regex?, val game: DrGame?)

    var converting: File? = null
    var from: String? = null
    var to: String? = null
    var filter: Regex? = null
    var builder: Boolean = false
    var game: DrGame? = null

    fun makeImmutable(
            defaultConverting: File? = null,
            defaultFrom: String? = null,
            defaultTo: String? = null,
            defaultFilter: Regex? = null,
            defaultGame: DrGame? = null
    ): Immutable =
            Immutable(
                    converting ?: defaultConverting,
                    from ?: defaultFrom,
                    to ?: defaultTo,
                    filter ?: defaultFilter,
                    game ?: defaultGame
            )
}