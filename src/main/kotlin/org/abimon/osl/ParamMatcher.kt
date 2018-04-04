package org.abimon.osl

import org.parboiled.MatcherContext
import org.parboiled.matchers.AnyMatcher
import org.parboiled.support.Chars

object ParamMatcher: AnyMatcher() {
    override fun match(context: MatcherContext<*>): Boolean {
        when(context.currentChar) {
            '\n' -> return false
            '|' -> return false
            ',' -> return false
            '"' -> return !(context.currentIndex == 0 || context.inputBuffer.charAt(context.currentIndex - 1) != '\\')
            Chars.EOI -> return false
            else -> return super.match(context)
        }
    }
}