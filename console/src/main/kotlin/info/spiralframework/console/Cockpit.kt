package info.spiralframework.console

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.LocaleLogger
import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.util.locale
import info.spiralframework.base.util.relativePathFrom
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.console.data.SpiralScope
import info.spiralframework.console.imperator.ImperatorParser
import info.spiralframework.core.SpiralCoreData
import info.spiralframework.core.userAgent
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.spiral.updater.DeleteUpdate
import info.spiralframework.spiral.updater.installUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.abimon.imperator.handle.Imperator
import org.abimon.imperator.impl.BasicImperator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

/** The driving force behind the console interface for Spiral */
abstract class Cockpit<SELF: Cockpit<SELF>> internal constructor(val args: GurrenArgs) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            DeleteUpdate.mainMethod = Cockpit.Companion::main
            val gurrenArgs = GurrenArgs(args)

            val instance: Cockpit<*>

            if (gurrenArgs.isTool) {
                instance = CockpitMechanic(gurrenArgs)
            } else {
                instance = CockpitPilot(gurrenArgs)
            }

            instance.start()
            System.exit(instance with { currentExitCode })
        }

        init {
            SpiralLocale.addBundle("SpiralCommands")

            DataHandler.byteArrayToMap = { byteArray -> SpiralCoreData.JSON_MAPPER.readValue(byteArray, Map::class.java).mapKeys { (key) -> key.toString() } }
            DataHandler.stringToMap = { string -> SpiralCoreData.JSON_MAPPER.readValue(string, Map::class.java).mapKeys { (key) -> key.toString() } }
            DataHandler.fileToMap = { file -> SpiralCoreData.JSON_MAPPER.readValue(file, Map::class.java).mapKeys { (key) -> key.toString() } }
            DataHandler.streamToMap = { stream -> SpiralCoreData.JSON_MAPPER.readValue(stream, Map::class.java).mapKeys { (key) -> key.toString() } }
        }
    }

    val runningJar = File(Cockpit::class.java.protectionDomain.codeSource.location.toURI())
    val runningDirectory = File(System.getProperty("user.dir")).absoluteFile!!
    val relativeRunningJar = runningJar relativePathFrom runningDirectory

    /** The mutex to use to access this classes properties */
    val mutex = Mutex()

    /** The logger for Spiral */
    var LOGGER: Logger = LocaleLogger(LoggerFactory.getLogger(locale<String>("logger.commands.name")))
    var NORMAL_LOGGER: Logger
        get() = DataHandler.LOGGER.let { logger -> if (logger is LocaleLogger) logger.logger else logger }
        set(value) {
            if (DataHandler.LOGGER is LocaleLogger)
                (DataHandler.LOGGER as LocaleLogger).logger = value
            else
                DataHandler.LOGGER = NORMAL_LOGGER
        }

    /**
     * The scope of operation that Spiral is currently operating in
     */
    var operationScope: SpiralScope = SpiralScope("default", "> ")

    val imperatorParser: ImperatorParser = ImperatorParser()
    val imperator: Imperator = BasicImperator()

    var currentExitCode: Int = 0

    /**
     * Signal to the cockpit that Spiral has finished initialising and to start up
     * This method will block the current thread. Use [startAsync] if you want finer control over the job that's launched
     */
    fun start() = runBlocking { startAsync().join() }

    abstract fun startAsync(scope: CoroutineScope = GlobalScope): Job

    @Suppress("UNCHECKED_CAST")
    suspend operator fun <T> invoke(op: suspend SELF.() -> T): T = mutex.withLock { (this@Cockpit as SELF).op() }
    @Suppress("UNCHECKED_CAST")
    infix fun <T> with(op: suspend SELF.() -> T): T = runBlocking { mutex.withLock { (this@Cockpit as SELF).op() } }
    @Suppress("UNCHECKED_CAST")
    suspend infix fun <T> withAsync(op: suspend SELF.() -> T): T = mutex.withLock { (this@Cockpit as SELF).op() }
}