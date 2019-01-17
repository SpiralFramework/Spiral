package org.abimon.osl.drills

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule
import kotlin.reflect.KClass

interface DrillHead<T : Any> {
    val klass: KClass<T>
    fun OpenSpiralLanguageParser.syntax(): Rule
    fun Syntax(parser: OpenSpiralLanguageParser): Rule = parser.syntax()
    fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): T?
}