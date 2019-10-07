package info.spiralframework.base.common.locale

import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.loadProperties
import info.spiralframework.base.common.io.loadResource
import info.spiralframework.base.common.io.use
import kotlin.reflect.KClass

class CommonLocaleBundle(override val bundleName: String, override val locale: CommonLocale, override var parent: LocaleBundle?, val backing: Map<String, String>, val context: KClass<*>) : LocaleBundle, Map<String, String> by backing {
    companion object {
        @ExperimentalUnsignedTypes
        suspend inline fun <reified T : Any> load(bundleName: String, locale: CommonLocale): CommonLocaleBundle? =
                load(bundleName, locale, T::class)

        @ExperimentalUnsignedTypes
        suspend fun load(bundleName: String, locale: CommonLocale, context: KClass<*>): CommonLocaleBundle? {
            val parentSource = loadResource("$bundleName.properties", context)
            val languageSource = loadResource("${bundleName}_${locale.language}.properties", context)
            val countrySource = loadResource("${bundleName}_${locale.language}_${locale.country}.properties", context)
            val variantSource = loadResource("${bundleName}_${locale.language}_${locale.variant}.properties", context)

            val parentBundle = parentSource?.let { ds -> load(bundleName, CommonLocale.ROOT, ds, null, context) }
            val languageBundle = languageSource?.let { ds -> load(bundleName, CommonLocale.ROOT, ds, parentBundle, context) }
            val countryBundle = countrySource?.let { ds ->
                load(bundleName, CommonLocale.ROOT, ds, languageBundle ?: parentBundle, context)
            }
            val variantBundle = variantSource?.let { ds ->
                load(bundleName, CommonLocale.ROOT, ds, countryBundle ?: languageBundle ?: parentBundle, context)
            }
            return variantBundle ?: countryBundle ?: languageBundle ?: parentBundle
        }

        @ExperimentalUnsignedTypes
        suspend fun load(bundleName: String, locale: CommonLocale, dataSource: DataSource<*>, parent: LocaleBundle?, context: KClass<*>): CommonLocaleBundle? {
            val flow = dataSource.openInputFlow() ?: return null
            use(flow) {
                val properties = flow.loadProperties()
                return CommonLocaleBundle(bundleName, locale, parent, properties, context)
            }
        }
    }

    @ExperimentalUnsignedTypes
    override suspend fun loadWithLocale(locale: CommonLocale): LocaleBundle? =
            load(bundleName, locale, context)
}