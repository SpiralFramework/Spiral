package info.spiralframework.base.common.locale

import dev.brella.kornea.errors.common.KorneaResult
import info.spiralframework.base.common.io.SpiralResourceLoader

public interface LocaleBundle: Map<String, String> {
    public val bundleName: String
    public val locale: CommonLocale
//    val parent: LocaleBundle? We don't really need a parent do we?

    public suspend fun SpiralResourceLoader.loadWithLocale(locale: CommonLocale): KorneaResult<LocaleBundle>
}

public suspend fun LocaleBundle.loadWithLocale(resourceLoader: SpiralResourceLoader, locale: CommonLocale): KorneaResult<LocaleBundle> = resourceLoader.loadWithLocale(locale)