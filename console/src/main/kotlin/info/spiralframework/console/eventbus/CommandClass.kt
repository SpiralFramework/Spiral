package info.spiralframework.console.eventbus

import info.spiralframework.console.data.ParameterParser
import info.spiralframework.console.data.ParboiledMarker
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

interface CommandClass {
    val parameterParser: ParameterParser

    fun <T : Rule> makeRule(op: ParameterParser.() -> T): T {
        return parameterParser.op()
    }

    fun <T : Rule, V : Any> makeRuleWith(default: () -> V, op: ParameterParser.(Var<V>) -> T): Rule {
        return makeRule {
            val ruleVar = Var<V>()
            Sequence(
                    Action<Any> { ruleVar.set(default()) },
                    parameterParser.op(ruleVar),
                    Action<Any> { push(ruleVar.get()) }
            )
        }
    }

    fun ParboiledCommand(rule: Rule, scope: String? = null, command: ParboiledCommand.(List<Any>) -> Boolean): ParboiledCommand = ParboiledCommand(rule, scope, command)
    fun ParboiledCommand(rule: Rule, scope: String? = null, help: String, command: ParboiledCommand.(List<Any>) -> Boolean): ParboiledCommand = ParboiledCommand(rule, scope, help, command)

    fun ParameterParser.pushMarkerSuccessBase() = push(ParboiledMarker.SUCCESS_BASE)
    fun ParameterParser.pushMarkerSuccessCommand() = push(ParboiledMarker.SUCCESS_COMMAND)
    fun ParameterParser.pushMarkerFailedLocale(locale: String) = push(ParboiledMarker.FAILED_LOCALE(locale))
}