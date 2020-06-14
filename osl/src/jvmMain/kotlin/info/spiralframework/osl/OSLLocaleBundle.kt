package info.spiralframework.osl

import info.spiralframework.antlr.osl.OSLLocaleLexer
import info.spiralframework.antlr.osl.OSLLocaleParser
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.CommonLocaleBundle
import info.spiralframework.base.common.locale.LocaleBundle
import org.abimon.kornea.errors.common.*
import org.abimon.kornea.io.common.closeAfter
import org.abimon.kornea.io.common.flow.readAndClose
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
        suspend fun loadBundle(resourceLoader: SpiralResourceLoader, baseName: String, locale: CommonLocale = CommonLocale.defaultLocale, from: KClass<*>): KorneaResult<LocaleBundle> {
            val superBundle = loadBundleByName(resourceLoader, baseName, locale, null, from)
            val langBundle = loadBundleByName(resourceLoader, "${baseName}_${locale.language}", locale, superBundle.getOrNull(), from)
            val localeBundle = loadBundleByName(resourceLoader, "${baseName}_${locale.language}_${locale.country}", locale, langBundle.orElse(superBundle).getOrNull(), from)

//            superBundle?.name = baseName
//            langBundle?.name = baseName
//            localeBundle?.name = baseName
//
//            langBundle?.setLocale(locale)
//            localeBundle?.setLocale(locale)
//
//            langBundle?.parent = superBundle
//            localeBundle?.parent = langBundle ?: superBundle

            return localeBundle.orElse(langBundle).orElse(superBundle)
        }

        @ExperimentalUnsignedTypes
        suspend fun loadBundleByName(resourceLoader: SpiralResourceLoader, fullName: String, locale: CommonLocale, parent: LocaleBundle?, from: KClass<*>): KorneaResult<LocaleBundle> {
            val oslBundle = resourceLoader.loadResource("$fullName.properties", from).flatMap { ds ->
                closeAfter(ds) {
                    ds.openInputFlow().flatMap inner@{ flow ->
                        val input = CharStreams.fromString(buildString {
                            appendLine(String(flow.readAndClose(), Charsets.UTF_8))
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
                            return@inner KorneaResult.success(visitor.createLocaleBundle(fullName, locale, parent, from))
                        } catch (pce: ParseCancellationException) {
                            return@inner KorneaResult.thrown(pce)
                        }
                    }
                }
            }

            if (oslBundle is KorneaResult.Success) return oslBundle

            try {
                return CommonLocaleBundle.load(resourceLoader, fullName, locale, from).cast()
            } catch (mre: MissingResourceException) {
                return KorneaResult.thrown(mre)
            }
        }
    }
    override suspend fun SpiralResourceLoader.loadWithLocale(locale: CommonLocale): KorneaResult<out LocaleBundle> = loadBundle(this, bundleName, locale, from)
}