package info.spiralframework.console.commands.data

import java.io.File

class ConvertArgs {
    data class Immutable(val converting: File?, val from: String?, val to: String?, val filter: Regex?, val game: String?)

    var converting: File? = null
    var from: String? = null
    var to: String? = null
    var filter: Regex? = null
    var builder: Boolean = false
    var game: String? = null

    fun makeImmutable(
            defaultConverting: File? = null,
            defaultFrom: String? = null,
            defaultTo: String? = null,
            defaultFilter: Regex? = null,
            defaultGame: String? = null
    ): ConvertArgs.Immutable =
            Immutable(
                    converting ?: defaultConverting,
                    from ?: defaultFrom,
                    to ?: defaultTo,
                    filter ?: defaultFilter,
                    game ?: defaultGame
            )
}