package info.spiralframework.console

import info.spiralframework.base.util.measureResultNanoTime
import info.spiralframework.base.util.printlnLocale
import info.spiralframework.base.util.times
import info.spiralframework.console.commands.mechanic.GurrenMechanic
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.console.data.ParameterParser
import info.spiralframework.console.eventbus.CommandRequest
import info.spiralframework.console.eventbus.ParboiledCommand
import info.spiralframework.core.postback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CockpitMechanic internal constructor(args: GurrenArgs) : Cockpit<CockpitMechanic>(args) {
    companion object {
        const val SUCCESS = 0
        const val UNKNOWN_COMMAND = 3
        const val BAD_COMMAND = 4
    }

    override fun startAsync(scope: CoroutineScope): Job {
        return scope.launch {
            val (foundCommands, ns) = measureResultNanoTime { bus.postback(CommandRequest(args.filteredArgs.joinToString(ParameterParser.MECHANIC_SEPARATOR.toString()) { str -> str.trimStart('-') })).foundCommands }

            if (args.timeCommands) {
                printlnLocale("gurren.timing.command_runtime", foundCommands.size, ns)
            } else {
                LOGGER.trace("gurren.timing.command_runtime", foundCommands.size, ns)
            }

            if (foundCommands.isEmpty()) {
                printlnLocale("commands.mechanic.usage", relativeRunningJar, " " * (17 + relativeRunningJar.length))
                this@CockpitMechanic { currentExitCode = UNKNOWN_COMMAND }
            } else if (foundCommands.any(ParboiledCommand::failed)) {
                this@CockpitMechanic { currentExitCode = BAD_COMMAND }
            } else {
                this@CockpitMechanic { currentExitCode = SUCCESS }
            }
        }
    }

    init {
        registerCommandClass(GurrenMechanic(this))
    }
}