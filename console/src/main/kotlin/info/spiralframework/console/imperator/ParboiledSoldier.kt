package info.spiralframework.console.imperator

import info.spiralframework.base.util.printlnErr
import info.spiralframework.base.util.printlnErrLocale
import info.spiralframework.console.Cockpit
import org.abimon.imperator.handle.Order
import org.abimon.imperator.handle.Soldier
import org.abimon.imperator.handle.Watchtower
import org.abimon.imperator.impl.InstanceOrder
import org.parboiled.Rule
import org.parboiled.errors.ParseError
import org.parboiled.parserunners.ReportingParseRunner

open class ParboiledSoldier(val rule: Rule, val scope: String? = null, private val watchtowers: Collection<Watchtower>, val failedCommand: ParboiledSoldier.(List<ParseError>) -> Unit, val command: ParboiledSoldier.(List<Any>) -> Boolean) : Soldier {
    companion object {
        val FAILURE = true
        val SUCCESS = false

        val invalidCommand: ParboiledSoldier.(List<ParseError>) -> Unit = { failed ->
            if (failed.isNotEmpty()) {
                printlnErrLocale("commands.invalid")
                failed.mapNotNull(ParseError::getErrorMessage).distinct().forEach { error -> printlnErr("\t$error") }
            } else {
                printlnErrLocale("commands.unk_error")
            }
        }
    }

    constructor(rule: Rule, scope: String? = null, cockpit: Cockpit<*>, command: ParboiledSoldier.(List<Any>) -> Boolean) : this(rule, scope, java.util.Collections.singletonList(ParboiledWatchtower(rule, scope, cockpit)), invalidCommand, command)
    constructor(rule: Rule, scope: String? = null, cockpit: Cockpit<*>, help: String, command: ParboiledSoldier.(List<Any>) -> Boolean) : this(rule, scope, java.util.Collections.singletonList(ParboiledWatchtower(rule, scope, cockpit)), { printlnErr(help) }, command)
    val runner = ReportingParseRunner<Any>(rule)
    var failed: Boolean = false

    override fun command(order: Order) {
        when (order) {
            is InstanceOrder<*> -> {
                val command = order.data as? String ?: return

                runner.parseErrors.clear()
                val result = runner.run(command)
                failed = result.hasErrors()

                if (result.hasErrors()) {
                    failedCommand(result.parseErrors)
                } else {
                    failed = (command(result.valueStack.reversed()) == FAILURE)
                }
            }
        }
    }

    override fun getName(): String = "Parboiled Rule $rule"

    override fun getWatchtowers(): Collection<Watchtower> = watchtowers
}