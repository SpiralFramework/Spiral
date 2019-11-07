package info.spiralframework.osl

import org.parboiled.MatcherContext
import org.parboiled.matchers.AnyMatcher
import org.parboiled.support.Chars

object ParamMatcher : AnyMatcher() {
    override fun match(context: MatcherContext<*>): Boolean {
        when (context.currentChar) {
            '"' -> return context.inputBuffer.charAt(context.currentIndex - 1) == '\\'
            Chars.EOI -> return false
            else -> return super.match(context)
        }
    }
}
