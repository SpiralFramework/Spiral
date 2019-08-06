package info.spiralframework.osl.data.parboiled

import info.spiralframework.osl.EMPTY_RULE_BUILDER
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.RuleBuilder

data class ExtraRuleBuilders(
        val header: RuleBuilder = EMPTY_RULE_BUILDER,
        val text: RuleBuilder = EMPTY_RULE_BUILDER,
        val lin: RuleBuilder = EMPTY_RULE_BUILDER,
        val wrd: RuleBuilder = EMPTY_RULE_BUILDER,
        val stx: RuleBuilder = EMPTY_RULE_BUILDER,
        val nonstopRaw: RuleBuilder = EMPTY_RULE_BUILDER,
        val nonstopMinigame: RuleBuilder = EMPTY_RULE_BUILDER
) {
    operator fun invoke(parser: OpenSpiralLanguageParser): ExtraRules = ExtraRules(parser, this)
}
