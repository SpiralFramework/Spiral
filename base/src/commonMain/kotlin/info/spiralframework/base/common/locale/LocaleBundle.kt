package info.spiralframework.base.common.locale

interface LocaleBundle: Map<String, String> {
    val bundleName: String
    val locale: CommonLocale
    var parent: LocaleBundle?

    fun loadWithLocale(locale: CommonLocale): LocaleBundle?
}