package info.spiralframework.console.imperator

import info.spiralframework.base.SpiralLocale
import info.spiralframework.console.Cockpit
import org.abimon.imperator.handle.Order
import org.abimon.imperator.handle.Soldier
import org.abimon.imperator.handle.Watchtower
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.visi.io.errPrintln
import org.parboiled.Rule
import org.parboiled.errors.ParseError
import org.parboiled.parserunners.ReportingParseRunner

open class ParboiledSoldier(val rule: Rule, val scope: String? = null, private val watchtowers: Collection<Watchtower>, val failedCommand: (List<ParseError>) -> Unit, val command: (List<Any>) -> Unit) : Soldier {
    companion object {
        fun invalidCommand(failed: List<ParseError>) {
            errPrintln(SpiralLocale.localise("commands.invalid"))
        }
    }

    constructor(rule: Rule, scope: String? = null, cockpit: Cockpit, command: (List<Any>) -> Unit) : this(rule, scope, java.util.Collections.singletonList(ParboiledWatchtower(rule, scope, cockpit)), Companion::invalidCommand, command)
    constructor(rule: Rule, scope: String? = null, cockpit: Cockpit, help: String, command: (List<Any>) -> Unit) : this(rule, scope, java.util.Collections.singletonList(ParboiledWatchtower(rule, scope, cockpit)), { errPrintln(help) }, command)

    override fun command(order: Order) {
        when (order) {
            is InstanceOrder<*> -> {
                val command = order.data as? String ?: return

                val runner = ReportingParseRunner<Any>(rule)
                val result = runner.run(command)

                if (result.hasErrors()) {
                    failedCommand(result.parseErrors)
                } else {
                    command(result.valueStack.reversed())
                }
            }
        }
    }

    override fun getName(): String = "Parboiled Rule $rule"

    override fun getWatchtowers(): Collection<Watchtower> = watchtowers
}