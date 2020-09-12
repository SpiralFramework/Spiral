package info.spiralframework.console.jvm

import dev.brella.kornea.toolkit.coroutines.ascii.arbitraryProgressBar
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.console.jvm.data.GurrenArgs
import info.spiralframework.console.jvm.data.SpiralCockpitContext
import info.spiralframework.spiral.updater.jarLocation
import info.spiralframework.spiral.updater.moveUpdate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalUnsignedTypes
class CockpitUpdate internal constructor(val updateFile: File, startingContext: SpiralCockpitContext, vararg rawArgs: String) : Cockpit(startingContext) {
    companion object {
        val needsMove = AtomicBoolean(true)
    }

    val process: Process = ProcessBuilder("java", "-jar", updateFile.absolutePath, "--${GurrenArgs.DISABLE_UPDATE_CHECK}", *rawArgs)
            .inheritIO()
            .start()

    override suspend fun start() {
        with(context) {
            try {
                val exitCode = suspendCoroutine<Int> { continuation -> continuation.resume(process.waitFor()) }
                this@CockpitUpdate with { currentExitCode = exitCode }
            } catch (e: CancellationException) {
                val exitCode = suspendCoroutine<Int> { continuation -> continuation.resume(process.destroyForcibly().waitFor()) }
                this@CockpitUpdate with { currentExitCode = exitCode }
            } finally {
                //Move the update over
                move()
            }
        }
    }

    suspend fun SpiralCockpitContext.move() {
        printlnLocale("gurren.update.moving")
        arbitraryProgressBar {
            moveUpdate(updateFile.toURI(), Cockpit::class.java.jarLocation.toURI(), "")
            needsMove.set(false)
        }
        printlnLocale("gurren.update.finished_moving")
    }

    init {
        with(startingContext) {
            printlnLocale("gurren.update.init")

            Runtime.getRuntime().addShutdownHook(thread(false) {
                runBlocking {
                    move()
                }
            })
        }
    }
}