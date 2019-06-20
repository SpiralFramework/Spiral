package info.spiralframework.osl

import info.spiralframework.osl.data.OSLEnvironment
import info.spiralframework.osl.data.parboiled.ExtraRuleBuilders
import org.parboiled.Parboiled

open class OpenSpiralLanguageParser(open val extraRuleBuilders: ExtraRuleBuilders, open val environment: OSLEnvironment) : SpiralParser(true) {
    companion object {
        operator fun invoke(extraRuleBuilders: ExtraRuleBuilders, environment: OSLEnvironment): OpenSpiralLanguageParser = Parboiled.createParser(OpenSpiralLanguageParser::class.java, extraRuleBuilders, environment)
    }
}
