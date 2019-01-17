package org.abimon.osl

import org.parboiled.MatcherContext
import org.parboiled.matchers.AnyMatcher
import org.parboiled.support.Chars

object LineCodeMatcher: AnyMatcher() {
    override fun match(context: MatcherContext<*>): Boolean {
        when(context.currentChar) {
            '\n' -> return false
            '|' -> return false
            Chars.EOI -> return false
            else -> return super.match(context)
        }
    }
}