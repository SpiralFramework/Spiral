package info.spiralframework.console

import info.spiralframework.base.SpiralLocale
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.console.data.SpiralScope
import info.spiralframework.console.imperator.ImperatorParser
import info.spiralframework.core.SpiralCoreData
import info.spiralframework.core.formats.images.PNGFormat
import info.spiralframework.core.formats.images.SHTXFormat
import info.spiralframework.formats.utils.DataHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.abimon.imperator.handle.Imperator
import org.abimon.imperator.impl.BasicImperator
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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

            val img = { FileInputStream("/Users/undermybrella/Workspace/KSPIRAL/shinkiro/a2/data/_cg/bustup_00_00.btx") }
            val result = SHTXFormat.read(source = img)
            if (result.didSucceed) {
                println("Success!")

                println("Writing: " + FileOutputStream(File("tmp.png")).use { out -> PNGFormat.write(data = result.obj, stream = out) })
            }

            instance.start()
        }

        init {
            SpiralLocale.addBundle("SpiralCommands")

            DataHandler.byteArrayToMap = { byteArray -> SpiralCoreData.JSON_MAPPER.readValue(byteArray, Map::class.java).mapKeys { (key) -> key.toString() } }
            DataHandler.stringToMap = { string -> SpiralCoreData.JSON_MAPPER.readValue(string, Map::class.java).mapKeys { (key) -> key.toString() } }
            DataHandler.fileToMap = { file -> SpiralCoreData.JSON_MAPPER.readValue(file, Map::class.java).mapKeys { (key) -> key.toString() } }
            DataHandler.streamToMap = { stream -> SpiralCoreData.JSON_MAPPER.readValue(stream, Map::class.java).mapKeys { (key) -> key.toString() } }
        }
    }

    /** The logger for Spiral */
    val LOGGER = LoggerFactory.getLogger(SpiralLocale.localise("logger.commands.name", SpiralCoreData.version ?: SpiralLocale.localise("gurren.default_version")))

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