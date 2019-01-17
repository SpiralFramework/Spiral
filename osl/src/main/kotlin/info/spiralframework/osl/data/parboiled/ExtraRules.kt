package info.spiralframework.osl.data.parboiled

import info.spiralframework.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

data class ExtraRules(
        val header: Array<Rule> = emptyArray(),
        val text: Array<Rule> = emptyArray(),
        val lin: Array<Rule> = emptyArray(),
        val wrd: Array<Rule> = emptyArray(),
        val stx: Array<Rule> = emptyArray(),
        val nonstopRaw: Array<Rule> = emptyArray(),
        val nonstopMinigame: Array<Rule> = emptyArray()
) {
    constructor(parser: OpenSpiralLanguageParser, builders: ExtraRuleBuilders): this(
            builders.header(parser),
            builders.text(parser),
            builders.lin(parser),
            builders.wrd(parser),
            builders.stx(parser),
            builders.nonstopRaw(parser),
            builders.nonstopMinigame(parser)
    )
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExtraRules) return false

        if (!header.contentEquals(other.header)) return false
        if (!text.contentEquals(other.text)) return false
        if (!lin.contentEquals(other.lin)) return false
        if (!wrd.contentEquals(other.wrd)) return false
        if (!stx.contentEquals(other.stx)) return false
        if (!nonstopRaw.contentEquals(other.nonstopRaw)) return false
        if (!nonstopMinigame.contentEquals(other.nonstopMinigame)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.contentHashCode()
        result = 31 * result + text.contentHashCode()
        result = 31 * result + lin.contentHashCode()
        result = 31 * result + wrd.contentHashCode()
        result = 31 * result + stx.contentHashCode()
        result = 31 * result + nonstopRaw.contentHashCode()
        result = 31 * result + nonstopMinigame.contentHashCode()
        return result
    }
}
