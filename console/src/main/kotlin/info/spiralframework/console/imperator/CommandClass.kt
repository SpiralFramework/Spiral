package info.spiralframework.console.imperator

import info.spiralframework.console.Cockpit
import org.parboiled.Rule

interface CommandClass {
    val cockpit: Cockpit

    fun <T : Rule> makeRule(op: ImperatorParser.() -> T): T {
        return cockpit.imperatorParser.op()
    }

    fun ParboiledSoldier(rule: Rule, scope: String? = null, command: (List<Any>) -> Unit): ParboiledSoldier = ParboiledSoldier(rule, scope, cockpit, command)
    fun ParboiledSoldier(rule: Rule, scope: String? = null, help: String, command: (List<Any>) -> Unit): ParboiledSoldier = ParboiledSoldier(rule, scope, cockpit, help, command)
}