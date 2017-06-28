package org.abimon.util

import org.parboiled.MatcherContext
import org.parboiled.matchers.AnyMatcher

object LineMatcher: AnyMatcher() {
    override fun match(context: MatcherContext<*>): Boolean {
        when(context.currentChar) {
            '\n' -> return false
            else -> return super.match(context)
        }
    }
}

object LineCodeMatcher: AnyMatcher() {
    override fun match(context: MatcherContext<*>): Boolean {
        when(context.currentChar) {
            '\n' -> return false
            '|' -> return false
            else -> return super.match(context)
        }
    }
}

class AllButMatcher(val blacklist: CharArray): AnyMatcher() {
    override fun match(context: MatcherContext<*>): Boolean {
        if(blacklist.contains(context.currentChar))
            return false
        return super.match(context)
    }
}