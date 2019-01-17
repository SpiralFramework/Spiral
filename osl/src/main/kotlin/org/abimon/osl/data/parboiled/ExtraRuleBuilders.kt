package org.abimon.osl.data.parboiled

import org.abimon.osl.EMPTY_RULE_BUILDER
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.RuleBuilder

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