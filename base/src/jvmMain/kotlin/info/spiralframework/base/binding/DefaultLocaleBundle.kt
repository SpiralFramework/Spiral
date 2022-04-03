package info.spiralframework.base.binding

import info.spiralframework.base.common.locale.CommonLocale
import java.util.*

public fun CommonLocale.jvm(): Locale = Locale(language, country, variant)