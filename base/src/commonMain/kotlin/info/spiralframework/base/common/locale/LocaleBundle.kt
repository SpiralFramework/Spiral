package info.spiralframework.base.common.locale

import info.spiralframework.base.common.io.SpiralResourceLoader
import org.abimon.kornea.errors.common.KorneaResult

interface LocaleBundle: Map<String, String> {
    val bundleName: String
    val locale: CommonLocale
//    val parent: LocaleBundle? We don't really need a parent do we?

    suspend fun SpiralResourceLoader.loadWithLocale(locale: CommonLocale): KorneaResult<out LocaleBundle>
}

suspend fun LocaleBundle.loadWithLocale(resourceLoader: SpiralResourceLoader, locale: CommonLocale) = resourceLoader.loadWithLocale(locale)