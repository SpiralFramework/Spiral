package info.spiralframework.console

import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.console.data.SpiralScope
import info.spiralframework.console.imperator.ImperatorParser
import info.spiralframework.core.SpiralCoreData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.abimon.imperator.handle.Imperator
import org.abimon.imperator.impl.BasicImperator
import org.slf4j.LoggerFactory

/** The driving force behind the console interface for Spiral */
abstract class Cockpit internal constructor(val args: GurrenArgs) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val gurrenArgs = GurrenArgs(args)
            val instance: Cockpit

            if (gurrenArgs.isTool) {
                println("UHO")
                instance = CockpitPilot(gurrenArgs)
            } else {
                instance = CockpitPilot(gurrenArgs)
            }

            instance.start()
        }

        init {
            SpiralCoreData.addBundle("SpiralCommands", Cockpit::class.java)
        }
    }

    /** The logger for Spiral */
    val LOGGER = LoggerFactory.getLogger(SpiralCoreData.localise("logger.name", SpiralCoreData.version ?: "Devloper"))

    /**
     * The scope of operation that Spiral is currently operating in
     */
    var operationScope: SpiralScope = SpiralScope("default", "> ")

    val imperatorParser: ImperatorParser = ImperatorParser()
    val imperator: Imperator = BasicImperator()

    /**
     * Signal to the cockpit that Spiral has finished initialising and to start up
     * This method will block the current thread. Use [startAsync] if you want finer control over the job that's launched
     */
    fun start() = runBlocking { startAsync().join() }

    abstract fun startAsync(scope: CoroutineScope = GlobalScope): Job
}