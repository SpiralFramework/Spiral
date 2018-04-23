package org.abimon.spiral.core.formats.text

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.scripting.LINFormat
import org.abimon.spiral.core.objects.game.DRGame
import java.io.InputStream
import java.io.OutputStream

object SpiralTextFormat : SpiralFormat {
    override val name = "SPIRAL Text"
    //TODO: Rename extension to something more unique
    override val extension = "stxt"
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
//        val data = dataSource().use { stream -> stream.readBytes() }
//
//        return !SpiralDrill.stxtRunner.run(String(data, Charsets.UTF_8)).hasErrors()
        return false
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, context, dataSource, output, params)) return true
        TODO("This doesn't work at the moment, but it will soon; stay tuned")

//        val data = dataSource().use { stream -> stream.readBytes() }
//
//        when (format) {
//            LINFormat -> {
////                val lin = make<CustomLin> {
////                    SpiralDrill.stxtRunner.run(String(data, Charsets.UTF_8)).valueStack.forEach { value ->
////                        if (value is List<*>) (value[0] as DrillHead).formScripts(value.subList(1, value.size).filterNotNull().toTypedArray()).forEach { scriptEntry -> entry(scriptEntry) }
////                    }
////                }
////
////                lin.compile(output)
//            }
//        }
//
//        return true
    }
}