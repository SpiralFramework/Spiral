package info.spiralframework.console

import info.spiralframework.base.util.measureResultNanoTime
import info.spiralframework.base.util.printlnLocale
import info.spiralframework.base.util.times
import info.spiralframework.console.commands.GurrenMechanic
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.console.imperator.ImperatorParser
import info.spiralframework.console.imperator.ParboiledSoldier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.abimon.imperator.impl.InstanceOrder

class CockpitMechanic internal constructor(args: GurrenArgs): Cockpit<CockpitMechanic>(args) {
    companion object {
        const val SUCCESS = 0
        const val UNKNOWN_COMMAND = 3
        const val BAD_COMMAND = 4
    }

    override fun startAsync(scope: CoroutineScope): Job {
        return scope.launch {
            val (matchingSoldiers, ns) = measureResultNanoTime { imperator.dispatch(InstanceOrder("STDIN", scout = null, data = args.filteredArgs.joinToString(ImperatorParser.MECHANIC_SEPARATOR.toString()) { str -> str.trimStart('-') })) }

            if (args.timeCommands) {
                printlnLocale("gurren.timing.command_runtime", matchingSoldiers.size, ns)
            } else {
                LOGGER.trace("gurren.timing.command_runtime", matchingSoldiers.size, ns)
            }

            if (matchingSoldiers.isEmpty()) {
                printlnLocale("commands.mechanic.usage", relativeRunningJar, " " * (18 + relativeRunningJar.length))
                this@CockpitMechanic { currentExitCode = UNKNOWN_COMMAND }
            } else if (matchingSoldiers.any { soldier -> (soldier as? ParboiledSoldier)?.failed == true }) {
                this@CockpitMechanic { currentExitCode = BAD_COMMAND }
            } else {
                this@CockpitMechanic { currentExitCode = SUCCESS }
            }
        }
    }

    init {
        imperator.hireSoldiers(GurrenMechanic(this))
    }
}