package info.spiralframework.console.imperator

import info.spiralframework.console.Cockpit
import org.abimon.imperator.handle.Order
import org.abimon.imperator.handle.Watchtower
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.osl.firstOfInstanceOrNull
import org.parboiled.Rule
import org.parboiled.errors.InvalidInputError
import org.parboiled.parserunners.ReportingParseRunner

open class ParboiledWatchtower(val rule: Rule, val scope: String? = null, val cockpit: Cockpit) : Watchtower {
    val runner = ReportingParseRunner<Any>(rule)

    override fun allow(order: Order): Boolean {
        if (scope != null && cockpit.operationScope.scopeName != scope)
            return false

        when (order) {
            is InstanceOrder<*> -> {
                val command = order.data as? String ?: return false
                runner.parseErrors.clear()

                val result = runner.run(command)
                if (result.parseErrors.isNotEmpty()) {
                    val inputError = result.parseErrors.firstOfInstanceOrNull(InvalidInputError::class)

                    if (inputError?.startIndex ?: 0 > 0)
                        return true //Let the command handle bad input
                }

                return !result.hasErrors()
            }
            else -> return false
        }
    }

    override fun getName(): String = "Parboiled Watchtower"
}