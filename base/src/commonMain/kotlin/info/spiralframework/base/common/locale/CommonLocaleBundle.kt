package info.spiralframework.base.common.locale

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.orDefault
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.loadProperties
import info.spiralframework.base.common.io.SpiralResourceLoader
import kotlin.reflect.KClass

public class CommonLocaleBundle(override val bundleName: String, override val locale: CommonLocale, private val backing: Map<String, String>, private val context: KClass<*>) : LocaleBundle, Map<String, String> by backing {
    public companion object {
        public const val ERROR_NO_LANG_FILES_FOUND: Int = 0x0000

        public suspend inline fun <reified T : Any> load(resourceLoader: SpiralResourceLoader, bundleName: String, locale: CommonLocale): KorneaResult<CommonLocaleBundle> =
                load(resourceLoader, bundleName, locale, T::class)

        public suspend fun load(resourceLoader: SpiralResourceLoader, bundleName: String, locale: CommonLocale, context: KClass<*>): KorneaResult<CommonLocaleBundle> {
            with(resourceLoader) {
                val parentSource = loadResource("$bundleName.properties", context)
                val languageSource = loadResource("${bundleName}_${locale.language}.properties", context)
                val countrySource = loadResource("${bundleName}_${locale.language}_${locale.country}.properties", context)
                val variantSource = loadResource("${bundleName}_${locale.language}_${locale.variant}.properties", context)

                if (parentSource.isFailure && languageSource.isFailure && countrySource.isFailure && variantSource.isFailure)
                    return KorneaResult.errorAsIllegalArgument(ERROR_NO_LANG_FILES_FOUND, "")

                val parentBundle = parentSource.flatMap { ds -> load(bundleName, CommonLocale.ROOT, ds, null, context) }
                val languageBundle = languageSource.flatMap { ds -> load(bundleName, CommonLocale(locale.language, "", ""), ds, parentBundle.getOrNull(), context) }
                val countryBundle = countrySource.flatMap { ds ->
                    load(bundleName, CommonLocale(locale.language, locale.country, ""), ds, languageBundle.orDefault(parentBundle).getOrNull(), context)
                }
                val variantBundle = variantSource.flatMap { ds ->
                    load(bundleName, locale, ds, countryBundle.orDefault(languageBundle).orDefault(parentBundle).getOrNull(), context)
                }
                return variantBundle.orDefault(countryBundle)
                        .orDefault(languageBundle)
                        .orDefault(parentBundle)
            }
        }

        public suspend fun load(bundleName: String, locale: CommonLocale, dataSource: DataSource<*>, parent: LocaleBundle?, context: KClass<*>): KorneaResult<CommonLocaleBundle> =
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