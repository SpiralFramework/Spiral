package org.abimon.spiral.util

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Used for wrapping external media programs, like ffmpeg
 */
object MediaWrapper {
    object ffmpeg {
        fun convert(from: File, to: File, waitFor: Long = 5, units: TimeUnit = TimeUnit.MINUTES) {
            if(to.exists())
                to.delete()
            execute(listOf("ffmpeg", "-i", from.absolutePath, to.absolutePath)).waitFor(waitFor, units)
        }

        val isInstalled: Boolean
            get() {
                val process = execute(listOf("ffmpeg", "-version"))
                process.waitFor(60, TimeUnit.SECONDS)
                return String(process.inputStream.readBytes()).startsWith("ffmpeg version")
            }
    }

    fun execute(command: List<String>): Process = ProcessBuilder().command(command).start()
}