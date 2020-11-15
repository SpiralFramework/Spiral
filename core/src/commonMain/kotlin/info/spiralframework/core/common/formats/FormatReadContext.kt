package info.spiralframework.core.common.formats

import info.spiralframework.formats.common.games.DrGame

interface FormatReadContext {
    companion object {
        operator fun invoke(name: String? = null, game: DrGame? = null): FormatReadContext =
                DefaultFormatReadContext(name, game)
    }

    val name: String?
    val game: DrGame?
//    val dataContext: DataContext
}

data class DefaultFormatReadContext(override val name: String? = null, override val game: DrGame? = null): FormatReadContext