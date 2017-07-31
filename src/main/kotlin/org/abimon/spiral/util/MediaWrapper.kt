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

        fun join(audio: File, video: File, output: File, waitFor: Long = 5, units: TimeUnit = TimeUnit.MINUTES) {
            if(output.exists())
                output.delete()
            execute(listOf("ffmpeg", "-i", audio.absolutePath, "-i", video.absolutePath, "-map", "0:a:0", "-map", "1:v:0", output.absolutePath)).waitFor(waitFor, units)
        }

        val isInstalled: Boolean
            get() {
                val process = executeSilent(listOf("ffmpeg", "-version"))
                process.waitFor(60, TimeUnit.SECONDS)
                val version = String(process.inputStream.readBytes())
                if(version.startsWith("ffmpeg version"))
                    return true
                println(version)
                return false
            }
    }

    fun execute(command: List<String>): Process = ProcessBuilder().command(command).apply {}.start()

    fun executeSilent(command: List<String>): Process = ProcessBuilder().command(command).start()
}