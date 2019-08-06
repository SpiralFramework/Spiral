package info.spiralframework.base.common.locale

interface LocaleBundle: Map<String, String> {
    val bundleName: String
    val locale: CommonLocale

    fun loadWithLocale(locale: CommonLocale): LocaleBundle?
}