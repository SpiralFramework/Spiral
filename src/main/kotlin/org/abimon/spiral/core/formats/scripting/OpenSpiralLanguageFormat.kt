package org.abimon.spiral.core.formats.scripting

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.WordScriptCommand
import org.abimon.osl.WordScriptString
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.customLin
import org.abimon.spiral.core.objects.customWordScript
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.abimon.spiral.util.debug
import java.io.InputStream
import java.io.OutputStream

object OpenSpiralLanguageFormat: SpiralFormat {
    override val name: String = "Open Spiral Language"
    override val extension: String = "osl"
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat, WRDFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        val text = String(dataSource().use { stream -> stream.readBytes() }, Charsets.UTF_8)

        val parser = OpenSpiralLanguageParser { fileName -> context(fileName)?.invoke()?.use { stream -> stream.readBytes() }}
        val result = parser.parse(text)
        return !result.hasErrors() && !result.valueStack.isEmpty
    }

    @Suppress("UNCHECKED_CAST")
    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, context, dataSource, output, params)) return true

        val text = String(dataSource().use { stream -> stream.readBytes() }, Charsets.UTF_8)
        val parser = OpenSpiralLanguageParser { fileName -> context(fileName)?.invoke()?.use { stream -> stream.readBytes() }}
        val result = parser.parse(text)
        val stack = result.valueStack?.toList()?.asReversed() ?: return false

        if (result.hasErrors() || result.valueStack.isEmpty)
            return false

        when (format) {
            LINFormat -> {
                val customLin = customLin {
                    stack.forEach { value ->
                        debug("Stack Value: $value")

                        if (value is List<*>) {
                            val head = (value[0] as? DrillHead<*>) ?: return@forEach
                            val valueParams = value.subList(1, value.size).filterNotNull().toTypedArray()

                            val products = head.operate(parser, valueParams)

                            when (head.klass) {
                                LinScript::class -> add(products as LinScript)
                                Array<LinScript>::class -> addAll(products as Array<LinScript>)
                            }
                        }
                    }
                }

                if (customLin.entries.isEmpty())
                    return false

                customLin.compile(output)
            }
            WRDFormat -> {
                val customWordScript = customWordScript {

                    stack.forEach { value ->
                        debug("Stack Value: $value")
                        if (value is List<*>) {
                            val head = (value[0] as? DrillHead<*>) ?: return@forEach
                            val valueParams = value.subList(1, value.size).filterNotNull().toTypedArray()

                            val products = head.operate(parser, valueParams)

                            when (head.klass) {
                                WrdScript::class -> add(products as WrdScript)
                                Array<WrdScript>::class -> addAll(products as Array<WrdScript>)
                                WordScriptString::class -> string((products as WordScriptString).string)
                                WordScriptCommand::class -> {
                                    val command = (products as WordScriptCommand)
                                    command(command.number, command.command)
                                }
                            }
                        }
                    }
                }

                customWordScript.compile(output)
            }
        }

        return false
    }
}