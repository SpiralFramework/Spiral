package info.spiralframework.console.imperator

import info.spiralframework.console.Cockpit
import org.abimon.imperator.handle.Order
import org.abimon.imperator.handle.Watchtower
import org.abimon.imperator.impl.InstanceOrder
import org.parboiled.Rule
import org.parboiled.parserunners.ReportingParseRunner

open class ParboiledWatchtower(val rule: Rule, val scope: String? = null, val cockpit: Cockpit<*>) : Watchtower {
    val runner = ReportingParseRunner<Any>(rule)

    override fun allow(order: Order): Boolean {
        if (scope != null && cockpit.with { operationScope }.scopeName != scope)
            return false

        when (order) {
            is InstanceOrder<*> -> {
                val command = order.data as? String ?: return false
                runner.parseErrors.clear()

                val result = runner.run(command)
//                if (result.parseErrors.isNotEmpty() && rule is SequenceMatcher) {
//                    val inputError = result.parseErrors.firstOfInstanceOrNull(InvalidInputError::class) ?: return false
//                    val firstNode = rule.children.firstOrNull() ?: return false
//
//                    return (inputError.failedMatchers.none { path -> path.element.matcher === firstNode }) //Let the command handle bad input
//                }

                return result.matched
            }
            else -> return false
        }
    }

    override fun getName(): String = "Parboiled Watchtower"
}