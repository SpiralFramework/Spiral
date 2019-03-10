package info.spiralframework.console.imperator

import info.spiralframework.console.Cockpit
import info.spiralframework.console.data.ParameterParser
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

interface CommandClass {
    val cockpit: Cockpit<*>

    fun <T : Rule> makeRule(op: ParameterParser.() -> T): T {
        return cockpit.parameterParser.op()
    }

    fun <T : Rule, V : Any> makeRuleWith(default: () -> V, op: ParameterParser.(Var<V>) -> T): Rule {
        return makeRule {
            val ruleVar = Var<V>()
            Sequence(
                    Action<Any> { ruleVar.set(default()) },
                    cockpit.parameterParser.op(ruleVar),
                    Action<Any> { push(ruleVar.get()) }
            )
        }
    }

    fun ParboiledSoldier(name: String, rule: Rule, scope: String? = null, command: ParboiledSoldier.(List<Any>) -> Boolean): ParboiledSoldier = ParboiledSoldier(name, rule, scope, cockpit, command)
    fun ParboiledSoldier(name: String, rule: Rule, scope: String? = null, help: String, command: ParboiledSoldier.(List<Any>) -> Boolean): ParboiledSoldier = ParboiledSoldier(name, rule, scope, cockpit, help, command)
}