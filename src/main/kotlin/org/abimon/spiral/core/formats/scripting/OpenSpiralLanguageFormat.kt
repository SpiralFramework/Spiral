package org.abimon.spiral.core.formats.scripting

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.WordScriptCommand
import org.abimon.osl.WordScriptString
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.text.STXFormat
import org.abimon.spiral.core.objects.customLin
import org.abimon.spiral.core.objects.customSTXT
import org.abimon.spiral.core.objects.customWordScript
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.abimon.spiral.core.objects.text.STXT
import org.abimon.spiral.core.utils.removeEscapes
import org.abimon.visi.lang.EnumOS
import java.io.InputStream
import java.io.OutputStream

object OpenSpiralLanguageFormat: SpiralFormat {
    override val name: String = "Open Spiral Language"
    override val extension: String = "osl"
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat, WRDFormat, STXFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        val text = String(dataSource().use { stream -> stream.readBytes() }, Charsets.UTF_8)

        val parser = OpenSpiralLanguageParser { fileName -> context(fileName)?.invoke()?.use { stream -> stream.readBytes() }}

        parser.drGame = game ?: UnknownHopesPeakGame
        parser["FILENAME"] = name
        parser["OS"] = EnumOS.determineOS().name

        val result = parser.parse(text)
        return !result.hasErrors() && !result.valueStack.isEmpty
    }

    @Suppress("UNCHECKED_CAST")
    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, context, dataSource, output, params)) return true

        val compileText = params["wrd:compile_text"]?.toString()?.toBoolean() ?: false

        val text = String(dataSource().use { stream -> stream.readBytes() }, Charsets.UTF_8)
        val parser = OpenSpiralLanguageParser { fileName -> context(fileName)?.invoke()?.use { stream -> stream.readBytes() }}
        val lang = context("en_US.lang")

        if (lang != null)
            parser.localiser = { unlocalised ->
                lang().use { stream -> String(stream.readBytes()) }.split('\n').firstOrNull { localised -> localised.startsWith("$unlocalised=") }?.substringAfter("$unlocalised=")?.replace("\\n", "\n")
                        ?: unlocalised
            }

        parser.drGame = game ?: UnknownHopesPeakGame
        parser["FILENAME"] = name
        parser["OS"] = EnumOS.determineOS().name

        val result = parser.parse(text)
        val stack = result.valueStack?.toList()?.asReversed() ?: return false

        if (result.hasErrors() || result.valueStack.isEmpty)
            return false

        when (format) {
            LINFormat -> {
                val customLin = customLin {
                    stack.forEach { value ->
                        SpiralData.LOGGER.trace("Stack Value: {}", value)

                        if (value is List<*>) {
                            val drillBit = (value[0] as? SpiralDrillBit) ?: return@forEach
                            val head = drillBit.head
                            try {
                                val valueParams = value.subList(1, value.size).filterNotNull().toTypedArray()

                                val products = head.operate(parser, valueParams)

                                when (head.klass) {
                                    LinScript::class -> add(products as LinScript)
                                    Array<LinScript>::class -> addAll(products as Array<LinScript>)
                                    Unit::class -> { }
                                    else -> System.err.println("${head.klass} not a recognised product type!")
                                }
                            } catch (th: Throwable) {
                                throw IllegalArgumentException("Script line [${drillBit.script}] threw an error", th)
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
                        SpiralData.LOGGER.trace("Stack Value: {}", value)
                        if (value is List<*>) {
                            val drillBit = (value[0] as? SpiralDrillBit) ?: return@forEach
                            val head = drillBit.head
                            try {
                                val valueParams = value.subList(1, value.size).filterNotNull().toTypedArray()

                                val products = head.operate(parser, valueParams)

                                when (head.klass) {
                                    WrdScript::class -> add(products as WrdScript)
                                    Array<WrdScript>::class -> addAll(products as Array<WrdScript>)
                                    WordScriptCommand::class -> {
                                        val command = products as WordScriptCommand

                                        command.command.let { cmdStr ->
                                            when (command.type) {
                                                EnumWordScriptCommand.LABEL -> if (cmdStr !in labels) label(cmdStr)
                                                EnumWordScriptCommand.PARAMETER -> if (cmdStr !in parameters) parameter(cmdStr)
                                                EnumWordScriptCommand.STRING -> if (cmdStr !in strings && compileText) strings.add(cmdStr)
                                                EnumWordScriptCommand.RAW -> {
                                                }
                                            }
                                        }
                                    }
                                    Unit::class -> { }
                                    else -> System.err.println("${head.klass} not a recognised product type!")
                                }
                            } catch (th: Throwable) {
                                throw IllegalArgumentException("Script line [${drillBit.script}] threw an error", th)
                            }
                        }
                    }
                }

                if (customWordScript.entries.isEmpty())
                    return false

                customWordScript.compile(output)
            }
            STXFormat -> {
                val customSTX = customSTXT {
                    val unmapped: MutableList<String> = ArrayList()
                    result.valueStack.reversed().forEach { value ->
                        if (value is List<*>) {
                            val drillBit = (value[0] as? SpiralDrillBit) ?: return@forEach
                            val head = drillBit.head
                            try {
                                val oslParams = value.subList(1, value.size).filterNotNull().toTypedArray()

                                val products = head.operate(parser, oslParams)

                                when (head.klass) {
                                    STXT.Language::class -> language = products as STXT.Language
                                    WordScriptString::class -> {
                                        val str = products as WordScriptString

                                        if (str.index == -1)
                                            unmapped.add(str.str.removeEscapes())
                                        else
                                            this[str.index] = str.str.removeEscapes()
                                    }
                                    Unit::class -> {
                                    }
                                }
                            } catch (th: Throwable) {
                                throw IllegalArgumentException("Script line [${drillBit.script}] threw an error", th)
                            }
                        }
                    }

                    val localKeys = strings.keys.sorted().toMutableList()

                    unmapped.forEach { str ->
                        val index = localKeys.let { strs -> (0..localKeys.size + 1).first { i -> i !in strs } }
                        localKeys.add(index)
                        localKeys.sort()

                        this[index] = str
                    }
                }

                customSTX.compile(output)
            }
        }

        return true
    }
}