package org.abimon.osl

import org.parboiled.MatcherContext
import org.parboiled.matchers.AnyMatcher

class AllButMatcher(val blacklist: CharArray): AnyMatcher() {
    override fun match(context: MatcherContext<*>): Boolean {
        if(blacklist.contains(context.currentChar) && context.inputBuffer.charAt(context.currentIndex - 1) != '\\')
            return false
        return super.match(context)
    }
}