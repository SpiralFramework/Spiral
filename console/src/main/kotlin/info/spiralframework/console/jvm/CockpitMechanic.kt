package info.spiralframework.console.jvm

import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.jvm.times
import info.spiralframework.console.jvm.data.SpiralCockpitContext
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@ExperimentalUnsignedTypes
@ExperimentalTime
class CockpitMechanic internal constructor(startingContext: SpiralCockpitContext) : Cockpit(startingContext) {
    companion object {
        const val SUCCESS = 0
        const val UNKNOWN_COMMAND = 3
        const val BAD_COMMAND = 4
    }

    override suspend fun start() {
        with(context) {
            val (foundCommands, ns) = measureTimedValue {
//                post(
//                        CommandRequest(
//                                context.args.filteredArgs.joinToString(ParameterParser.MECHANIC_SEPARATOR.toString()) { str -> str.trimStart('-') },
//                                with { operationScope }
//                        )
//                ).foundCommands

                emptyList<String>()
            }

            if (args.timeCommands) {
                printlnLocale("gurren.timing.command_runtime", foundCommands.size, ns.toLongNanoseconds())
            } else {
                trace("gurren.timing.command_runtime", foundCommands.size, ns.toLongNanoseconds())
            }

            if (foundCommands.isEmpty()) {
                printlnLocale("commands.mechanic.usage", relativeRunningJar, " " * (17 + relativeRunningJar.length))
                this@CockpitMechanic with { currentExitCode = UNKNOWN_COMMAND }
//            } else if (foundCommands.any(ParboiledCommand::failed)) {
            } else if (foundCommands.contains("fail")) {
                this@CockpitMechanic with { currentExitCode = BAD_COMMAND }
            } else {
                this@CockpitMechanic with { currentExitCode = SUCCESS }
            }
        }
    }
}