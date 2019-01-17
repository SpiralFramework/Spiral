package org.abimon.osl.drills

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.BaseParser
import org.parboiled.Rule
import kotlin.reflect.KClass

class StaticDrill<T: Any>(val value: T, override val klass: KClass<T>): DrillHead<T> {
    companion object {
        inline operator fun <reified T: Any> invoke(value: T): StaticDrill<T> = StaticDrill(value, T::class)
    }

    override fun OpenSpiralLanguageParser.syntax(): Rule = BaseParser.NOTHING
    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): T = value
}