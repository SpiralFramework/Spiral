package info.spiralframework.base.common.locale

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.getOrNull
import dev.brella.kornea.errors.common.orElse
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.loadProperties
import info.spiralframework.base.common.io.SpiralResourceLoader
import kotlin.reflect.KClass

class CommonLocaleBundle(override val bundleName: String, override val locale: CommonLocale, val backing: Map<String, String>, val context: KClass<*>) : LocaleBundle, Map<String, String> by backing {
    companion object {
        const val ERROR_NO_LANG_FILES_FOUND = 0x0000

        @ExperimentalUnsignedTypes
        suspend inline fun <reified T : Any> load(resourceLoader: SpiralResourceLoader, bundleName: String, locale: CommonLocale): KorneaResult<CommonLocaleBundle> =
                load(resourceLoader, bundleName, locale, T::class)

        @ExperimentalUnsignedTypes
        suspend fun load(resourceLoader: SpiralResourceLoader, bundleName: String, locale: CommonLocale, context: KClass<*>): KorneaResult<CommonLocaleBundle> {
            with(resourceLoader) {
                val parentSource = loadResource("$bundleName.properties", context)
                val languageSource = loadResource("${bundleName}_${locale.language}.properties", context)
                val countrySource = loadResource("${bundleName}_${locale.language}_${locale.country}.properties", context)
                val variantSource = loadResource("${bundleName}_${locale.language}_${locale.variant}.properties", context)

                if (parentSource !is KorneaResult.Success && languageSource !is KorneaResult.Success && countrySource !is KorneaResult.Success && variantSource !is KorneaResult.Success)
                    return KorneaResult.errorAsIllegalArgument(ERROR_NO_LANG_FILES_FOUND, "")

                val parentBundle = parentSource.flatMap { ds -> load(bundleName, CommonLocale.ROOT, ds, null, context) }
                val languageBundle = languageSource.flatMap { ds -> load(bundleName, CommonLocale(locale.language, "", ""), ds, parentBundle.getOrNull(), context) }
                val countryBundle = countrySource.flatMap { ds ->
                    load(bundleName, CommonLocale(locale.language, locale.country, ""), ds, languageBundle.orElse(parentBundle).getOrNull(), context)
                }
                val variantBundle = variantSource.flatMap { ds ->
                    load(bundleName, locale, ds, countryBundle.orElse(languageBundle).orElse(parentBundle).getOrNull(), context)
                }
                return variantBundle.orElse(countryBundle)
                        .orElse(languageBundle)
                        .orElse(parentBundle)
            }
        }

        @ExperimentalUnsignedTypes
        suspend fun load(bundleName: String, locale: CommonLocale, dataSource: DataSource<*>, parent: LocaleBundle?, context: KClass<*>): KorneaResult<CommonLocaleBundle> =
                dataSource.openInputFlow()
                        .flatMap { flow ->
                            closeAfter(flow) {
                                val properties: MutableMap<String, String> = HashMap()
                                parent?.let(properties::putAll)
                                properties.putAll(flow.loadProperties())
                                KorneaResult.success(CommonLocaleBundle(bundleName, locale, properties, context))
                            }
                        }
    }

    override suspend fun SpiralResourceLoader.loadWithLocale(locale: CommonLocale): KorneaResult<LocaleBundle> =
            load(this, bundleName, locale, context).cast()
}