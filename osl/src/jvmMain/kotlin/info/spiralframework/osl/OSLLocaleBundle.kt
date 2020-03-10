package info.spiralframework.osl

import info.spiralframework.antlr.osl.OSLLocaleLexer
import info.spiralframework.antlr.osl.OSLLocaleParser
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.CommonLocaleBundle
import info.spiralframework.base.common.locale.LocaleBundle
import org.abimon.kornea.io.common.flow.readAndClose
import org.abimon.kornea.io.common.useInputFlow
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.util.*
import kotlin.reflect.KClass

open class OSLLocaleBundle(override val bundleName: String, override val locale: CommonLocale, val map: Map<String, String>, private val from: KClass<*>): LocaleBundle, Map<String, String> by map {
    companion object {
        suspend inline fun <reified T: Any> loadBundle(resourceLoader: SpiralResourceLoader, baseName: String, locale: CommonLocale = CommonLocale.defaultLocale) = loadBundle(resourceLoader, baseName, locale, T::class)
        @ExperimentalUnsignedTypes
        suspend fun loadBundle(resourceLoader: SpiralResourceLoader, baseName: String, locale: CommonLocale = CommonLocale.defaultLocale, from: KClass<*>): LocaleBundle? {
            val superBundle = loadBundleByName(resourceLoader, baseName, locale, null, from)
            val langBundle = loadBundleByName(resourceLoader, "${baseName}_${locale.language}", locale, superBundle, from)
            val localeBundle = loadBundleByName(resourceLoader, "${baseName}_${locale.language}_${locale.country}", locale, langBundle ?: superBundle, from)

//            superBundle?.name = baseName
//            langBundle?.name = baseName
//            localeBundle?.name = baseName
//
//            langBundle?.setLocale(locale)
//            localeBundle?.setLocale(locale)
//
//            langBundle?.parent = superBundle
//            localeBundle?.parent = langBundle ?: superBundle

            return localeBundle ?: langBundle ?: superBundle
        }

        @ExperimentalUnsignedTypes
        suspend fun loadBundleByName(resourceLoader: SpiralResourceLoader, fullName: String, locale: CommonLocale, parent: LocaleBundle?, from: KClass<*>): LocaleBundle? {
            resourceLoader.loadResource("$fullName.properties", from)?.useInputFlow { flow ->
                val input = CharStreams.fromString(buildString {
                    appendln(String(flow.readAndClose(), Charsets.UTF_8))
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
                    return@useInputFlow visitor.createLocaleBundle(fullName, locale, parent, from)
                } catch (pce: ParseCancellationException) {}
            }
            try {
                return CommonLocaleBundle.load(resourceLoader, fullName, locale, from)
            } catch (mre: MissingResourceException) {
                return null
            }
        }
    }
    override suspend fun SpiralResourceLoader.loadWithLocale(locale: CommonLocale): LocaleBundle? = loadBundle(this, bundleName, locale, from)
}