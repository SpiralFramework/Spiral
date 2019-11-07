package info.spiralframework.base.binding

import info.spiralframework.base.common.EntryPair
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.LocaleBundle
import java.util.*

fun CommonLocale.jvm(): Locale = Locale(language, country, variant)