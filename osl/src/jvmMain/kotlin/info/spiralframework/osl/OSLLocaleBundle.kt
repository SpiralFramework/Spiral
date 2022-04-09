package info.spiralframework.osl

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.orDefault
import dev.brella.kornea.io.common.flow.readAndClose
import info.spiralframework.antlr.osl.LocaleVisitor
import info.spiralframework.antlr.osl.OSLLocaleLexer
import info.spiralframework.antlr.osl.OSLLocaleParser
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.CommonLocaleBundle
import info.spiralframework.base.common.locale.LocaleBundle
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.util.*
import kotlin.reflect.KClass

public open class OSLLocaleBundle(
    override val bundleName: String,
    override val locale: CommonLocale,
    private val map: Map<String, String>,
    private val from: KClass<*>
) : LocaleBundle, Map<String, String> by map {
    public companion object {
        public suspend inline fun <reified T : Any> loadBundle(
            resourceLoader: SpiralResourceLoader,
            baseName: String,
            locale: CommonLocale = CommonLocale.defaultLocale
        ): KorneaResult<LocaleBundle> = loadBundle(resourceLoader, baseName, locale, T::class)

        public suspend fun loadBundle(
            resourceLoader: SpiralResourceLoader,
            baseName: String,
            locale: CommonLocale = CommonLocale.defaultLocale,
            from: KClass<*>
        ): KorneaResult<LocaleBundle> {
            val superBundle = loadBundleByName(resourceLoader, baseName, locale, null, from)
            val langBundle = loadBundleByName(
                resourceLoader,
                "${baseName}_${locale.language}",
                locale,
                superBundle.getOrNull(),
                from
            )

            val localeBundle = loadBundleByName(
                resourceLoader,
                "${baseName}_${locale.language}_${locale.country}",
                locale,
                langBundle
                    .orDefault(superBundle)
                    .getOrNull(),
                from
            )

//            superBundle?.name = baseName
//            langBundle?.name = baseName
//            localeBundle?.name = baseName
//
//            langBundle?.setLocale(locale)
//            localeBundle?.setLocale(locale)
//
//            langBundle?.parent = superBundle
//            localeBundle?.parent = langBundle ?: superBundle

            return localeBundle
                .orDefault(langBundle)
                .orDefault(superBundle)
        }

        public suspend fun loadBundleByName(
            resourceLoader: SpiralResourceLoader,
            fullName: String,
            locale: CommonLocale,
            parent: LocaleBundle?,
            from: KClass<*>
        ): KorneaResult<LocaleBundle> {
            val oslBundle = resourceLoader
                .loadResource("$fullName.properties", from)
                .flatMap { ds ->
                    closeAfter(ds) {
                        ds.openInputFlow().flatMap inner@{ flow ->
                            val input = CharStreams.fromString(String(flow.readAndClose(), Charsets.UTF_8))
                            val lexer = OSLLocaleLexer(input)
                            val tokens = CommonTokenStream(lexer)
                            val parser = OSLLocaleParser(tokens)
                            parser.removeErrorListeners()
                            parser.errorHandler = BailErrorStrategy()
                            try {
                                val tree = parser.locale()
                                val visitor = LocaleVisitor()
                                visitor.visit(tree)
                                return@inner KorneaResult.success(
                                    visitor.createLocaleBundle(
                                        fullName,
                                        locale,
                                        parent,
                                        from
                                    )
                                )
                            } catch (pce: ParseCancellationException) {
                                return@inner KorneaResult.thrown(pce)
                            }
                        }
                    }
                }

            if (oslBundle.isSuccess) return oslBundle

            return try {
                CommonLocaleBundle.load(resourceLoader, fullName, locale, from)
            } catch (mre: MissingResourceException) {
                KorneaResult.thrown(mre)
            }
        }
    }

    override suspend fun SpiralResourceLoader.loadWithLocale(locale: CommonLocale): KorneaResult<LocaleBundle> =
        loadBundle(this, bundleName, locale, from)
}