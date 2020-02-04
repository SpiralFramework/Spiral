package info.spiralframework.base.common.locale

import info.spiralframework.base.common.io.SpiralResourceLoader
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.loadProperties
import org.abimon.kornea.io.common.use
import kotlin.reflect.KClass

class CommonLocaleBundle(override val bundleName: String, override val locale: CommonLocale, val backing: Map<String, String>, val context: KClass<*>) : LocaleBundle, Map<String, String> by backing {
    companion object {
        @ExperimentalUnsignedTypes
        suspend inline fun <reified T : Any> load(resourceLoader: SpiralResourceLoader, bundleName: String, locale: CommonLocale): CommonLocaleBundle? =
                load(resourceLoader, bundleName, locale, T::class)

        @ExperimentalUnsignedTypes
        suspend fun load(resourceLoader: SpiralResourceLoader, bundleName: String, locale: CommonLocale, context: KClass<*>): CommonLocaleBundle? {
            with(resourceLoader) {
                val parentSource = loadResource("$bundleName.properties", context)
                val languageSource = loadResource("${bundleName}_${locale.language}.properties", context)
                val countrySource = loadResource("${bundleName}_${locale.language}_${locale.country}.properties", context)
                val variantSource = loadResource("${bundleName}_${locale.language}_${locale.variant}.properties", context)

                val parentBundle = parentSource?.let { ds -> load(bundleName, CommonLocale.ROOT, ds, null, context) }
                val languageBundle = languageSource?.let { ds -> load(bundleName, CommonLocale(locale.language, "", ""), ds, parentBundle, context) }
                val countryBundle = countrySource?.let { ds ->
                    load(bundleName, CommonLocale(locale.language, locale.country, ""), ds, languageBundle
                            ?: parentBundle, context)
                }
                val variantBundle = variantSource?.let { ds ->
                    load(bundleName, locale, ds, countryBundle ?: languageBundle ?: parentBundle, context)
                }
                return variantBundle ?: countryBundle ?: languageBundle ?: parentBundle
            }
        }

        @ExperimentalUnsignedTypes
        suspend fun load(bundleName: String, locale: CommonLocale, dataSource: DataSource<*>, parent: LocaleBundle?, context: KClass<*>): CommonLocaleBundle? {
            val flow = dataSource.openInputFlow() ?: return null
            use(flow) {
                val properties: MutableMap<String, String> = HashMap()
                parent?.let(properties::putAll)
                properties.putAll(flow.loadProperties())
                return CommonLocaleBundle(bundleName, locale, properties, context)
            }
        }
    }

    @ExperimentalUnsignedTypes
    override suspend fun SpiralResourceLoader.loadWithLocale(locale: CommonLocale): LocaleBundle? =
            load(this, bundleName, locale, context)
}