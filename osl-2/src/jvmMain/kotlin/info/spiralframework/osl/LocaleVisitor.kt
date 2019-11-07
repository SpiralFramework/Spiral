package info.spiralframework.osl

import info.spiralframework.antlr.osl.OSLLocaleParser
import info.spiralframework.antlr.osl.OSLLocaleParserBaseVisitor
import info.spiralframework.base.common.locale.CommonLocale
import org.antlr.v4.runtime.tree.TerminalNode

class LocaleVisitor : OSLLocaleParserBaseVisitor<Unit>() {
    private val localeMap: MutableMap<String, String> = HashMap()

    override fun visitLocaleLine(ctx: OSLLocaleParser.LocaleLineContext) {
        val key = ctx.LOCALE_PROPERTY_NAME().text

        val value = buildString {
            ctx.children.forEach { node ->
                if (node !is TerminalNode)
                    return@forEach

                when (node.symbol.type) {
                    OSLLocaleParser.LOCALE_ESCAPES -> {
                        when (node.text[1]) {
                            'b' -> append('\b')
                            'f' -> append(0x0C.toChar())
                            'n' -> append('\n')
                            'r' -> append('\r')
                            't' -> append('\t')
                            'u' -> append(node.text.substring(2).toInt(16).toChar())
                        }
                    }
                    OSLLocaleParser.LOCALE_STRING_CHARACTERS -> append(node.text)
                }
            }
        }

        val processed = value
                .toCharArray()
                .map { c -> "\\u${c.toInt().toString(16).padStart(4, '0')}" }
                .joinToString("")

        localeMap[key] = processed
    }

    fun createLocaleBundle(bundleName: String, locale: CommonLocale) = OSLLocaleBundle(bundleName, locale, localeMap)
}