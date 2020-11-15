package info.spiralframework.core.common.formats

import info.spiralframework.formats.common.games.DrGame

interface FormatWriteContext {
    companion object {
        operator fun invoke(name: String? = null, game: DrGame? = null): FormatWriteContext =
                DefaultFormatWriteContext(name, game)
    }

    val name: String?
    val game: DrGame?
//    val dataContext: DataContext
}

data class DefaultFormatWriteContext(override val name: String? = null, override val game: DrGame? = null): FormatWriteContext