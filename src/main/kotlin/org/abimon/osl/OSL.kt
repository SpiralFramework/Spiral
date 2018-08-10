package org.abimon.osl

import org.abimon.osl.data.nonstopDebate.NonstopDebateNewObject
import org.abimon.osl.data.nonstopDebate.NonstopDebateVariable
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.spiral.core.objects.customNonstopDebate
import org.abimon.spiral.core.objects.scripting.CustomLin
import org.abimon.spiral.core.objects.scripting.CustomNonstopDebate
import org.abimon.spiral.core.objects.scripting.CustomWordScript
import org.abimon.spiral.core.objects.scripting.NonstopDebateSection
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.text.CustomSTXT
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

object OSL {
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        val jsonParser = JsonParser()

        DataHandler.stringToMap = { string -> jsonParser.parse(string) }
        DataHandler.streamToMap = { stream -> jsonParser.parse(String(stream.readBytes())) }

        val script = File(args.firstOrNull { str -> str.startsWith("--script=") }?.substringAfter('=') ?: run { print("Script: "); readLine() ?: error("No script provided!") })
        val parent = File(args.firstOrNull { str -> str.startsWith("--parent=") }?.substringAfter('=') ?:script.absolutePath.substringBeforeLast(File.separator))

        val parser = OpenSpiralLanguageParser { name ->
            val file = File(parent, name)
            if (file.exists())
                return@OpenSpiralLanguageParser file.readBytes()
            return@OpenSpiralLanguageParser null
        }

        parser.localisationFile = File(args.firstOrNull { str -> str.startsWith("--lang=") }?.substringAfter('=') ?: "en_US.lang")

        val (result, finalScript) = parser.parseWithOutput(script.readText())

        var compiling: Any? = null
        var section: NonstopDebateSection? = null

        val compiled: MutableMap<String, Any> = HashMap()

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

                        when (head.klass) {
                            GameContext::class -> {
                                val context = products as GameContext
                                when (context) {
                                    is GameContext.HopesPeakGameContext -> compiling = CustomLin()
                                    is GameContext.V3GameContext -> compiling = CustomWordScript()
                                    is GameContext.NonstopDebateContext -> compiling = customNonstopDebate { game = context.game }
                                    is GameContext.STXGameContext -> compiling = CustomSTXT()
                                }
                            }
                            LinScript::class -> (compiling as? CustomLin)?.add(products as LinScript)
                            Array<LinScript>::class -> (compiling as? CustomLin)?.addAll(products as Array<LinScript>)
                            NonstopDebateVariable::class -> {
                                val variable = products as NonstopDebateVariable

                                section?.let { nonstopSection ->
                                    if (variable.index < nonstopSection.data.size)
                                        nonstopSection[variable.index] = variable.data
                                }
                            }
                            NonstopDebateNewObject::class -> {
                                if (compiling is CustomNonstopDebate)
                                    section?.let(compiling::section)

                                section = NonstopDebateSection((products as NonstopDebateNewObject).size)
                            }

                            OSLVariable::class -> {
                                val (key, keyVal) = products as OSLVariable<*>

                                when (key) {
                                    OSLVariable.KEYS.NONSTOP_TIMELIMIT -> (compiling as? CustomNonstopDebate)?.timeLimit = keyVal as? Int ?: 300
                                    OSLVariable.KEYS.COMPILE_AS -> {
                                        if (compiling != null) {
                                            if (compiling is CustomNonstopDebate)
                                                section?.let(compiling::section)

                                            compiled[keyVal.toString()] = compiling

                                            compiling = null
                                        }
                                    }
                                }
                            }
                            Unit::class -> {}
                            else -> System.err.println("${head.klass} not a recognised product type!")
                        }
                    } catch (th: Throwable) {
                        throw IllegalArgumentException("Script line [${drillBit.script}] threw an error", th)
                    }
                }
            }
        }

        if (compiling != null) {
            if (compiling is CustomNonstopDebate)
                section?.let(compiling::section)

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

        val saveTo = File(args.firstOrNull { str -> str.startsWith("--save_to=") }?.substringAfter('=') ?: run { print("Save To: "); readLine() ?: return })
        if (!saveTo.exists())
            saveTo.mkdirs()

        compiled.forEach { name, product ->
            val output: File?

            when (product) {
                is CustomLin -> {
                    output = File(saveTo, "$name.lin")
                    FileOutputStream(output).use(product::compile)
                }
                is CustomWordScript -> {
                    output = File(saveTo, "$name.wrd")
                    FileOutputStream(output).use(product::compile)
                }
                is CustomNonstopDebate -> {
                    output = File(saveTo, "$name.dat")
                    FileOutputStream(output).use(product::compile)
                }
                is CustomSTXT -> {
                    output = File(saveTo, "$name.stx")
                    FileOutputStream(output).use(product::compile)
                }
                else -> {
                    output = null
                    return@forEach
                }
            }

            println("Compiled $name (type: ${product::class}) to $output")
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