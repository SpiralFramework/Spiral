package org.abimon.osl

import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.osl.results.*
import org.abimon.spiral.core.utils.DataHandler
import org.parboiled.Action
import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.errors.InvalidInputError
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.support.Var
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import kotlin.reflect.full.cast

object OSL {
    val SCRIPT_REGEX = "e\\d{2}_\\d{3}_\\d{3}(\\.lin)?".toRegex()

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        val jsonParser = JsonParser()

        DataHandler.stringToMap = { string -> jsonParser.parse(string) }
        DataHandler.streamToMap = { stream -> jsonParser.parse(String(stream.readBytes())) }

        val script = File(args.firstOrNull { str -> str.startsWith("--script=") }?.substringAfter('=')
                ?: run { print("Script: "); readLine() ?: error("No script provided!") })
        val parent = File(args.firstOrNull { str -> str.startsWith("--parent=") }?.substringAfter('=')
                ?: script.absolutePath.substringBeforeLast(File.separator))

        val parser = OpenSpiralLanguageParser { name ->
            val file = File(parent, name)
            if (file.exists())
                return@OpenSpiralLanguageParser file.readBytes()
            return@OpenSpiralLanguageParser null
        }

        parser.localisationFile = File(args.firstOrNull { str -> str.startsWith("--lang=") }?.substringAfter('=')
                ?: "en_US.lang")

        val (result, finalScript) = parser.parseWithOutput(script.readText())

        var compiling: OSLCompilation<*>? = null

        val compiled: MutableMap<String, OSLCompilation<*>> = HashMap()

        if (result.hasErrors()) {
            val inputError = result.parseErrors.filterIsInstance(InvalidInputError::class.java).firstOrNull()

            if (inputError != null) {
                println("Error found from ${inputError.startIndex} to ${inputError.endIndex}: [${inputError.inputBuffer.extract(inputError.startIndex, inputError.endIndex)}]")
            } else {
                println("Other errors found: ${result.parseErrors.joinToString("\n")}")
            }
        } else {
            for (value in result.valueStack.reversed()) {
                if (value is List<*>) {
                    val drillBit = (value[0] as? SpiralDrillBit) ?: continue
                    val head = drillBit.head
                    try {
                        val params = value.subList(1, value.size).filterNotNull().toTypedArray()

                        val products = head.operate(parser, params)

                        compiling?.handle(drillBit, head.klass.cast(products), head.klass)

                        when (head.klass) {
                            GameContext::class -> {
                                val context = products as GameContext
                                when (context) {
                                    is GameContext.NonstopDebateDataContext -> compiling = CustomNonstopDataOSL(context.game)
                                    is GameContext.NonstopDebateMinigameContext -> compiling = CustomNonstopMinigameOSL(context.game)
                                    is GameContext.HopesPeakGameContext -> compiling = CustomLinOSL()
                                    is GameContext.V3GameContext -> compiling = CustomWordScriptOSL()
                                    is GameContext.STXGameContext -> compiling = CustomSTXOSL()
                                }
                            }
                            OSLVariable::class -> {
                                val (key, keyVal) = products as OSLVariable<*>

                                when (key) {
                                    OSLVariable.KEYS.COMPILE_AS -> {
                                        if (compiling != null)
                                            compiled[keyVal.toString()] = compiling

                                        compiling = null
                                    }
                                }
                            }
                        }
                    } catch (th: Throwable) {
                        throw IllegalArgumentException("Script line [${drillBit.script}] threw an error", th)
                    }
                }
            }
        }

        if (compiling != null) {
            compiled[script.name.substringBeforeLast('.')] = compiling
        }

        println()
        println(compiled)
        println()
        finalScript.split('\n').maxBy(String::length)?.let { str -> (0 until str.length).forEach { print('*') } }
        println()
        println(finalScript)
        finalScript.split('\n').maxBy(String::length)?.let { str -> (0 until str.length).forEach { print('*') } }
        println("\n")

        if (!result.hasErrors()) {

            val saveTo = File(args.firstOrNull { str -> str.startsWith("--save_to=") }?.substringAfter('=')
                    ?: run { print("Save To: "); readLine() ?: return }).let { file ->
                if (file.isFile)
                    return@let File(file.absolutePath.trimEnd(File.separatorChar).substringBeforeLast(File.separator))
                return@let file
            }
            if (!saveTo.exists())
                saveTo.mkdirs()

            compiled.forEach { name, blueprint ->
                when (blueprint) {
//                is CustomLin -> {
//                    output = File(saveTo, "$name.lin")
//                    FileOutputStream(output).use(product::compile)
//                }
//                is CustomWordScript -> {
//                    output = File(saveTo, "$name.wrd")
//                    FileOutputStream(output).use(product::compile)
//                }
//                is CustomNonstopDebate -> {
//                    output = File(saveTo, "$name.dat")
//                    FileOutputStream(output).use(product::compile)
//                }
//                is CustomSTXT -> {
//                    output = File(saveTo, "$name.stx")
//                    FileOutputStream(output).use(product::compile)
//                }

                    is CustomLinOSL -> {
                        val output = File(saveTo, "$name.lin")
                        FileOutputStream(output).use(blueprint.produce()::compile)
                    }

                    is CustomNonstopMinigameOSL -> {
                        val scriptFolder: File
                        val binFolder: File
                        val savePath = saveTo.absolutePath.toLowerCase().trimEnd(File.separatorChar)

                        if (savePath.endsWith("${File.separator}script") || savePath == "script") {
                            scriptFolder = saveTo
                            binFolder = File("${saveTo.absolutePath.substringBeforeLast(File.separator)}${File.separator}bin")
                        } else if (savePath.endsWith("${File.separator}bin") || savePath == "bin") {
                            binFolder = saveTo
                            scriptFolder = File("${saveTo.absolutePath.substringBeforeLast(File.separator)}${File.separator}script")
                        } else if (savePath.endsWith("${File.separator}dr1") || savePath.endsWith("${File.separator}dr2") || savePath == "dr1" || savePath == "dr2") {
                            scriptFolder = File(saveTo, "data${File.separator}us${File.separator}script")
                            binFolder = File(saveTo, "data${File.separator}us${File.separator}bin")
                        } else if (saveTo.listFiles().any { file -> (file.name.toLowerCase() == "dr1" || file.name.toLowerCase() == "dr2") && file.isDirectory }) {
                            val drDir = saveTo.listFiles().first { file -> (file.name.toLowerCase() == "dr1" || file.name.toLowerCase() == "dr2") && file.isDirectory }

                            scriptFolder = File(drDir, "data${File.separator}us${File.separator}script")
                            binFolder = File(drDir, "data${File.separator}us${File.separator}bin")
                        } else {
                            scriptFolder = saveTo
                            binFolder = saveTo
                        }

                        if (!scriptFolder.exists())
                            scriptFolder.mkdirs()
                        if (!binFolder.exists())
                            binFolder.mkdirs()

                        val chapter: Int?
                        val room: Int?
                        val scene: Int?

                        if (name.matches(SCRIPT_REGEX)) {
                            chapter = name.substring(1, 3).toInt()
                            room = name.substring(4, 7).toInt()
                            scene = name.substring(8, 11).toInt() + 1
                        } else {
                            chapter = null
                            room = null
                            scene = null
                        }

                        val mainScriptFile = File(scriptFolder, "$name.lin")
                        val debateScriptFile = File(scriptFolder, buildString {
                            if (chapter == null || room == null || scene == null) {
                                append(name)
                                append("-debate.lin")
                            } else {
                                append('e')
                                append(chapter.toString().padStart(2, '0'))
                                append('_')
                                append(room.toString().padStart(3, '0'))
                                append('_')
                                append(scene.toString().padStart(3, '0'))
                                append(".lin")
                            }
                        })
                        val debateFile = File(binFolder, buildString {
                            if (chapter == null) {
                                append(name)
                                append("-debate.dat")
                            } else {
                                append("nonstop_")
                                append(chapter.toString().padStart(2, '0'))
                                append('_')
                                append(blueprint.minigame.debateNumber.toString().padStart(3, '0'))
                                append(".dat")
                            }
                        })

                        val (mainScript, debateScript, debate) = blueprint.produce(chapter, room, scene)

                        FileOutputStream(mainScriptFile).use(mainScript::compile)
                        FileOutputStream(debateScriptFile).use(debateScript::compile)
                        FileOutputStream(debateFile).use(debate::compile)

                        println("Compiled $name (type: ${mainScript::class} to $mainScriptFile")
                        println("Compiled $name (type: ${debateScript::class} to $debateScriptFile")
                        println("Compiled $name (type: ${debate::class} to $debateFile")

//                    println("Compiled $name (type: ${blueprint::class}) to $output")
                    }
                }
            }
        }
    }

    /**
     * We use this here as an alternative to a JSON library because we need it for literally two files
     */
    @Suppress("UNUSED_PARAMETER")
    @BuildParseTree
    open class JsonParser(parboiledCreated: Boolean) : BaseParser<Any>() {
        companion object {
            operator fun invoke(): JsonParser = Parboiled.createParser(JsonParser::class.java, true)
        }

        open val digitsLower = charArrayOf(
                '0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b',
                'c', 'd', 'e', 'f', 'g', 'h',
                'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z'
        )

        open val digitsUpper = charArrayOf(
                '0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'A', 'B',
                'C', 'D', 'E', 'F', 'G', 'H',
                'I', 'J', 'K', 'L', 'M', 'N',
                'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z'
        )

        open val whitespace = (Character.MIN_VALUE until Character.MAX_VALUE).filter { Character.isWhitespace(it) }.toCharArray()

        open fun Digit(): Rule = Digit(10)
        open fun Digit(base: Int): Rule = FirstOf(AnyOf(digitsLower.sliceArray(0 until base)), AnyOf(digitsUpper.sliceArray(0 until base)))
        open fun WhitespaceCharacter(): Rule = AnyOf(whitespace)
        open fun OptionalWhitespace(): Rule = ZeroOrMore(WhitespaceCharacter())
        open fun InlineWhitespaceCharacter(): Rule = AnyOf(charArrayOf('\t', ' '))
        open fun InlineWhitespace(): Rule = OneOrMore(InlineWhitespaceCharacter())
        open fun OptionalInlineWhitespace(): Rule = ZeroOrMore(InlineWhitespaceCharacter())

        @Suppress("UNCHECKED_CAST")
        fun parse(string: String): Map<String, Any>? {
            val runner = ReportingParseRunner<Any>(JsonObject())
            val result = runner.run(string)

            return result.resultValue as? Map<String, Any>
        }

        open fun JsonObject(): Rule {
            val mapVar = Var<MutableMap<String, Any>>(HashMap())

            val pushToMap = Action<Any> {
                val kv = pop()

                if (kv is Pair<*, *>) {
                    val (key, value) = kv

                    if (key !is String)
                        return@Action false

                    mapVar.get()[key] = value ?: return@Action true

                    return@Action true
                }

                return@Action false
            }

            return Sequence(
                    '{',
                    Action<Any> { mapVar.get().clear(); true },
                    Optional(
                            JsonKeyValuePair(),
                            pushToMap,
                            ZeroOrMore(
                                    ',',
                                    JsonKeyValuePair(),
                                    pushToMap
                            )
                    ),
                    '}',
                    Action<Any> { push(mapVar.get()) }
            )
        }

        open fun JsonArray(): Rule {
            val arrayVar = Var<MutableList<Any>>(ArrayList())

            return Sequence(
                    '[',
                    Action<Any> { arrayVar.get().clear(); true },
                    Optional(
                            OptionalWhitespace(),
                            JsonValue(),
                            OptionalWhitespace(),
                            Action<Any> { arrayVar.get().add(pop()) },
                            ZeroOrMore(',', OptionalWhitespace(), JsonValue(), OptionalWhitespace(), Action<Any> { arrayVar.get().add(pop()) })
                    ),
                    Action<Any> { push(arrayVar.get()) },
                    ']'
            )
        }

        open fun JsonKeyValuePair(): Rule {
            val key = Var<String>()
            val value = Var<Any>()

            return Sequence(
                    OptionalWhitespace(),
                    JsonString(),
                    Action<Any> { key.set(pop() as String) },
                    OptionalWhitespace(),
                    ':',
                    OptionalWhitespace(),
                    JsonValue(),
                    Action<Any> { value.set(pop()) },
                    OptionalWhitespace(),
                    Action<Any> { push(key.get() to value.get()) }
            )
        }

        open fun JsonString(): Rule {
            val str = Var<String>()

            return Sequence(
                    "\"",
                    Action<Any> { str.set("") },
                    Optional(
                            OneOrMore(
                                    FirstOf(
                                            Sequence(
                                                    "\\",
                                                    FirstOf(
                                                            Sequence(
                                                                    FirstOf(
                                                                            "\"",
                                                                            "\\",
                                                                            "/",
                                                                            "b",
                                                                            "f",
                                                                            "n",
                                                                            "r",
                                                                            "t"
                                                                    ),
                                                                    Action<Any> {
                                                                        when (match()) {
                                                                            "\"" -> str.set(str.get() + "\"")
                                                                            "\\" -> str.set(str.get() + "\\")
                                                                            "/" -> str.set(str.get() + "/")
                                                                            "b" -> str.set(str.get() + "\b")
                                                                            "f" -> str.set(str.get() + 0xC.toChar())
                                                                            "n" -> str.set(str.get() + "\n")
                                                                            "r" -> str.set(str.get() + "\r")
                                                                            "t" -> str.set(str.get() + "\t")
                                                                        }

                                                                        return@Action true
                                                                    }
                                                            ),
                                                            Sequence(
                                                                    "u",
                                                                    NTimes(4, Digit(16)),
                                                                    Action<Any> { str.set(str.get() + match().toInt(16).toChar()) }
                                                            )
                                                    )
                                            ),
                                            Sequence(
                                                    AllButMatcher(charArrayOf('\\', '"')),
                                                    Action<Any> { str.set(str.get() + match()) }
                                            )
                                    )
                            )
                    ),
                    Action<Any> { push(str.get()) },
                    "\""
            )
        }

        open fun JsonNumber(): Rule {
            val floating = Var<Boolean>()

            return Sequence(
                    Sequence(
                            Action<Any> { floating.set(false) },
                            Optional("-"),
                            FirstOf(
                                    "0",
                                    Sequence(
                                            FirstOf("1", "2", "3", "4", "5", "6", "7", "8", "9"),
                                            OneOrMore(Digit())
                                    )
                            ),
                            Optional(
                                    '.',
                                    OneOrMore(Digit()),
                                    Action<Any> { floating.set(true) }
                            ),

                            Optional(
                                    FirstOf('e', 'E'),
                                    Optional(FirstOf('+', '-')),
                                    OneOrMore(Digit()),
                                    Action<Any> { floating.set(true) }
                            )
                    ),

                    Action<Any> {
                        val num = BigDecimal(match())

                        return@Action push(if (floating.get()) num.toDouble() else num.longValueExact())
                    }
            )
        }

        open fun JsonValue(): Rule = FirstOf(
                JsonString(),
                JsonNumber(),

                JsonObject(),
                JsonArray(),

                Sequence(
                        "true",
                        push(true)
                ),
                Sequence(
                        "false",
                        push(false)
                ),
                Sequence(
                        "null",
                        push(null)
                )
        )
    }
}