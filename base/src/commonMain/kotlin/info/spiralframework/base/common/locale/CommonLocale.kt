package info.spiralframework.base.common.locale

public data class CommonLocale(val language: String, val country: String = "", val variant: String = "") {
    public companion object {
        public val ENGLISH: CommonLocale = CommonLocale("en")
        public val FRENCH: CommonLocale = CommonLocale("fr")
        public val GERMAN: CommonLocale = CommonLocale("de")
        public val ITALIAN: CommonLocale = CommonLocale("it")
        public val JAPANESE: CommonLocale = CommonLocale("ja")
        public val KOREAN: CommonLocale = CommonLocale("ko")
        public val CHINESE: CommonLocale = CommonLocale("zh")

        public val SIMPLIFIED_CHINESE: CommonLocale = CommonLocale("zh", "CN")
        public val TRADITIONAL_CHINESE: CommonLocale = CommonLocale("zh", "TW")

        public val FRANCE: CommonLocale = CommonLocale("fr", "FR")
        public val GERMANY: CommonLocale = CommonLocale("de", "DE")
        public val ITALY: CommonLocale = CommonLocale("it", "IT")
        public val JAPAN: CommonLocale = CommonLocale("ja", "JP")
        public val KOREA: CommonLocale = CommonLocale("ko", "KR")
        public val CHINA: CommonLocale = SIMPLIFIED_CHINESE
        public val PRC: CommonLocale = SIMPLIFIED_CHINESE
        public val TAIWAN: CommonLocale = TRADITIONAL_CHINESE
        public val UK: CommonLocale = CommonLocale("en", "GB")
        public val US: CommonLocale = CommonLocale("en", "US")
        public val AUSTRALIA: CommonLocale = CommonLocale("en", "AU")
        public val CANADA: CommonLocale = CommonLocale("en", "CA")
        public val CANADA_FRENCH: CommonLocale = CommonLocale("fr", "CA")

        public val ROOT: CommonLocale = CommonLocale("")

        public val defaultLocale: CommonLocale by lazy { info.spiralframework.base.binding.defaultLocale() }
    }

    public fun languageString(): String = buildString {
        append(language)
        if (country.isNotBlank()) {
            append('_')
            append(country)
        }

        if (variant.isNotBlank()) {
            append('_')
            append(variant)
        }
    }
}