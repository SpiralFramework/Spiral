package info.spiralframework.osl

import info.spiralframework.antlr.osl.OSLLocaleLexer
import info.spiralframework.antlr.osl.OSLLocaleParser
import info.spiralframework.base.binding.DefaultLocaleBundle
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.LocaleBundle
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.io.InputStream
import java.util.*

open class OSLLocaleBundle(override val bundleName: String, override val locale: CommonLocale, val map: Map<String, String>): LocaleBundle, Map<String, String> by map {
    companion object {
        @JvmOverloads
        fun loadBundle(baseName: String, locale: CommonLocale = CommonLocale.defaultLocale): LocaleBundle? {
            val superBundle = loadBundleByName(baseName, locale)
            val langBundle = loadBundleByName("${baseName}_${locale.language}", locale)
            val localeBundle = loadBundleByName("${baseName}_${locale.language}_${locale.country}", locale)

//            superBundle?.name = baseName
//            langBundle?.name = baseName
//            localeBundle?.name = baseName
//
//            langBundle?.setLocale(locale)
//            localeBundle?.setLocale(locale)
//
            langBundle?.parent = superBundle
            localeBundle?.parent = langBundle ?: superBundle

            return localeBundle ?: langBundle ?: superBundle
        }

        fun loadBundleByName(fullName: String, locale: CommonLocale): LocaleBundle? {
            OSLLocaleBundle::class.java.classLoader.getResourceAsStream("$fullName.properties")?.let { stream ->
                val input = CharStreams.fromString(buildString {
                    appendln(String(stream.use(InputStream::readBytes), Charsets.UTF_8))
                })
                val lexer = OSLLocaleLexer(input)
                val tokens = CommonTokenStream(lexer)
                val parser = OSLLocaleParser(tokens)
                parser.removeErrorListeners()
                parser.errorHandler = BailErrorStrategy()
                try {
                    val tree = parser.locale()
                    val visitor = LocaleVisitor()
                    visitor.visit(tree)
                    return visitor.createLocaleBundle(fullName, locale)
                } catch (pce: ParseCancellationException) {}
            }
            try {
                return DefaultLocaleBundle(fullName, locale)
            } catch (mre: MissingResourceException) {
                return null
            }
        }
    }

    override var parent: LocaleBundle? = null

    override fun loadWithLocale(locale: CommonLocale): LocaleBundle? = loadBundle(bundleName, locale)
}