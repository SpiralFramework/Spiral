package info.spiralframework.base.binding

import info.spiralframework.base.common.EntryPair
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.LocaleBundle
import java.util.*

actual class DefaultLocaleBundle(val bundle: ResourceBundle, override val locale: CommonLocale, override val bundleName: String = bundle.baseBundleName) : LocaleBundle {
    actual constructor(bundleName: String, locale: CommonLocale) : this(ResourceBundle.getBundle(bundleName, locale.jvm()), locale, bundleName)
    constructor(bundleName: String, locale: CommonLocale, classLoader: ClassLoader) : this(ResourceBundle.getBundle(bundleName, locale.jvm(), classLoader), locale, bundleName)

    override val entries: Set<Map.Entry<String, String>> by lazy {
        bundle.keySet()
                .map { key -> EntryPair(key, bundle.getString(key)) }
                .toSet()
    }

    override val keys: Set<String> get() = bundle.keySet()
    override val size: Int get() = keys.size
    override val values: Collection<String> get() = bundle.keySet().map(bundle::getString)

    override fun containsKey(key: String): Boolean = bundle.containsKey(key)
    override fun containsValue(value: String): Boolean = bundle.keys.asSequence().any { key -> bundle.getString(key) == value }
    override fun get(key: String): String? = if (bundle.containsKey(key)) bundle.getString(key) else null

    override fun isEmpty(): Boolean = !bundle.keys.hasMoreElements()

    override fun loadWithLocale(locale: CommonLocale): LocaleBundle? = DefaultLocaleBundle(bundleName, locale)
}

fun CommonLocale.jvm(): Locale = Locale(language, country, variant)