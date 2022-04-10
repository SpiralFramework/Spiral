package info.spiralframework.core.common.formats

import info.spiralframework.formats.common.games.DrGame

public interface FormatReadContext {
    public companion object {
        public operator fun invoke(name: String? = null, game: DrGame? = null): FormatReadContext =
            DefaultFormatReadContext(name, game)
    }

    public val name: String?
    public val game: DrGame?
//    val dataContext: DataContext
}

public data class DefaultFormatReadContext(override val name: String? = null, override val game: DrGame? = null) :
    FormatReadContext