package info.spiralframework.base.locale

import java.util.*

/**
 * Declares a bundle as being loaded via a custom method
 */
interface CustomLocaleBundle {
    fun loadWithLocale(locale: Locale): ResourceBundle?
}