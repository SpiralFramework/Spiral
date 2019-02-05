package info.spiralframework.console

import info.spiralframework.base.util.locale
import info.spiralframework.base.util.printlnLocale
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.spiral.updater.jarLocation
import info.spiralframework.spiral.updater.moveUpdate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class CockpitUpdate internal constructor(val updateFile: File, args: GurrenArgs, vararg rawArgs: String): Cockpit<CockpitUpdate>(args) {
    companion object {
        val needsMove = AtomicBoolean(false)
    }
    val process: Process = ProcessBuilder("java", "-jar", updateFile.absolutePath, "--${GurrenArgs.DISABLE_UPDATE_CHECK}", *rawArgs)
            .inheritIO()
            .start()

    override fun startAsync(scope: CoroutineScope): Job {
        return scope.launch {
            try {
                this@CockpitUpdate { currentExitCode = process.waitFor() }
            } catch (e: CancellationException) {
                this@CockpitUpdate { currentExitCode = process.destroyForcibly().waitFor() }
            } finally {
                //Move the update over
                printlnLocale("gurren.update.moving")
                moveUpdate(updateFile.toURI(), Cockpit::class.java.jarLocation.toURI(), locale("gurren.update.finished_moving"))
                needsMove.set(false)
            }
        }
    }

    init {
        printlnLocale("gurren.update.init")

        Runtime.getRuntime().addShutdownHook(thread(false) {
            if (needsMove.get()) {
                printlnLocale("gurren.update.moving")
                moveUpdate(updateFile.toURI(), Cockpit::class.java.jarLocation.toURI(), locale("gurren.update.finished_moving"))
                needsMove.set(false)
            }
        })
    }
}