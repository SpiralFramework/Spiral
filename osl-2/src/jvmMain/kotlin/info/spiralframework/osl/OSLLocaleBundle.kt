package info.spiralframework.osl

import info.spiralframework.antlr.osl.OSLLocaleLexer
import info.spiralframework.antlr.osl.OSLLocaleParser
import info.spiralframework.base.locale.CustomLocaleBundle
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.io.InputStream
import java.io.Reader
import java.util.*

open class OSLLocaleBundle(reader: Reader) : PropertyResourceBundle(reader), CustomLocaleBundle {
    companion object {
        val RESOURCE_BUNDLE_SET_PARENT = ResourceBundle::class.java.getDeclaredMethod("setParent", ResourceBundle::class.java)
                .apply { isAccessible = true }
        val RESOURCE_BUNDLE_NAME = ResourceBundle::class.java.getDeclaredField("name")
                .apply { isAccessible = true }
        val RESOURCE_BUNDLE_LOCALE = ResourceBundle::class.java.getDeclaredField("locale")
                .apply { isAccessible = true }

        fun ResourceBundle.setParent(bundle: ResourceBundle?) {
            RESOURCE_BUNDLE_SET_PARENT.invoke(this, bundle)
        }

        fun ResourceBundle.setLocale(locale: Locale) {
            RESOURCE_BUNDLE_LOCALE[this] = locale
        }

        var ResourceBundle.name: String
            get() = RESOURCE_BUNDLE_NAME[this] as String
            set(value) { RESOURCE_BUNDLE_NAME[this] = value }

        @JvmOverloads
        fun loadBundle(baseName: String, locale: Locale = Locale.getDefault()): ResourceBundle? {
            val superBundle = loadBundleByName(baseName)
            val langBundle = loadBundleByName("${baseName}_${locale.language}")
            val localeBundle = loadBundleByName("${baseName}_${locale.language}_${locale.country}")

            superBundle?.name = baseName
            langBundle?.name = baseName
            localeBundle?.name = baseName

            langBundle?.setLocale(locale)
            localeBundle?.setLocale(locale)

            langBundle?.setParent(superBundle)
            localeBundle?.setParent(langBundle ?: superBundle)

            return localeBundle ?: langBundle ?: superBundle
        }

        fun loadBundleByName(fullName: String): ResourceBundle? {
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
                    return visitor.createResourceBundle()
                } catch (pce: ParseCancellationException) {}
            }
            try {
                return ResourceBundle.getBundle(fullName)
            } catch (mre: MissingResourceException) {
                return null
            }
        }
    }

    override fun loadWithLocale(locale: Locale): ResourceBundle? = loadBundle(name, locale)
}