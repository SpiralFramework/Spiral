package info.spiralframework.core.formats

import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.BLANK_DATA_CONTEXT
import info.spiralframework.formats.utils.DataContext

interface FormatReadContext {
    val name: String?
    val game: DRGame?
    val dataContext: DataContext
}

data class DefaultFormatReadContext(override val name: String? = null, override val game: DRGame? = null, override val dataContext: DataContext = BLANK_DATA_CONTEXT): FormatReadContext