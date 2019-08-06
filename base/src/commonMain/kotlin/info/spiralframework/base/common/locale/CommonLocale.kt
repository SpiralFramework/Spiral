package info.spiralframework.base.common.locale

data class CommonLocale(val language: String, val country: String = "", val variant: String = "") {
    companion object {
        val ENGLISH = CommonLocale("en")
        val FRENCH = CommonLocale("fr")
        val GERMAN = CommonLocale("de")
        val ITALIAN = CommonLocale("it")
        val JAPANESE = CommonLocale("ja")
        val KOREAN = CommonLocale("ko")
        val CHINESE = CommonLocale("zh")

        val SIMPLIFIED_CHINESE = CommonLocale("zh", "CN")
        val TRADITIONAL_CHINESE = CommonLocale("zh", "TW")

        val FRANCE = CommonLocale("fr", "FR")
        val GERMANY = CommonLocale("de", "DE")
        val ITALY = CommonLocale("it", "IT")
        val JAPAN = CommonLocale("ja", "JP")
        val KOREA = CommonLocale("ko", "KR")
        val CHINA = SIMPLIFIED_CHINESE
        val PRC = SIMPLIFIED_CHINESE
        val TAIWAN = TRADITIONAL_CHINESE
        val UK = CommonLocale("en", "GB")
        val US = CommonLocale("en", "US")
        val AUSTRALIA = CommonLocale("en", "AU")
        val CANADA = CommonLocale("en", "CA")
        val CANADA_FRENCH = CommonLocale("fr", "CA")

        val ROOT = CommonLocale("")

        val defaultLocale: CommonLocale by lazy(::defaultLocale)
    }
}