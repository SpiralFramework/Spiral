package org.abimon.osl

import org.abimon.osl.drills.DrillHead
import org.abimon.osl.drills.circuits.*
import org.abimon.osl.drills.lin.*
import org.abimon.osl.drills.wrd.BasicWrdSpiralDrill
import org.abimon.osl.drills.wrd.NamedWrdSpiralDrill
import org.abimon.osl.drills.wrd.WordCommandDrill
import org.abimon.osl.drills.wrd.WordStringDrill
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.game.v3.V3
import org.parboiled.Action
import org.parboiled.Context
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.support.ParsingResult
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

open class OpenSpiralLanguageParser(private val oslContext: (String) -> ByteArray?, isParboiledCreated: Boolean) : SpiralParser(isParboiledCreated) {
    companion object {
        val FRAMES_PER_SECOND = 60

        operator fun invoke(oslContext: (String) -> ByteArray?): OpenSpiralLanguageParser = Parboiled.createParser(OpenSpiralLanguageParser::class.java, oslContext, true)
    }

    var game: DRGame = UnknownHopesPeakGame
    var strictParsing: Boolean = true

    val customIdentifiers = HashMap<String, Int>()
    val customFlagNames = HashMap<String, Int>()
    val customLabelNames = HashMap<String, Int>()

    val flags = HashMap<String, Boolean>()
    val data = HashMap<String, Any>()

    val states = HashMap<Int, ParserState>()

    var startingGame: DRGame = UnknownHopesPeakGame
    val startingCustomIdentifiers = HashMap<String, Int>()
    val startingCustomFlagNames = HashMap<String, Int>()
    val startingCustomLabelNames = HashMap<String, Int>()
    val startingFlags = HashMap<String, Boolean>()
    val startingData = HashMap<String, Any>()

    val uuid = UUID.randomUUID().toString()
    var labels = 0
    var flagCheckIndentation = 0

    operator fun get(key: String): Any? = data[key]
    operator fun <T : Any> get(key: String, clazz: KClass<T>): T? = clazz.safeCast(data[key])

    operator fun set(key: String, value: Any?): Any? {
        if (silence)
            return null

        if (value == null)
            return data.remove(key)
        return data.put(key, value)
    }

    fun loadState(context: Context<Any>) {
        val (stateSilence, stateGame, stateStrictParsing, stateCustomIdentifiers, stateCustomFlagNames, stateCustomLabelNames, stateFlags, stateData, valueStackSnapshot) = (states.remove(context.level) ?: return)

        this.silence = stateSilence
        this.game = stateGame
        this.strictParsing = stateStrictParsing

        this.customIdentifiers.clear()
        this.customIdentifiers.putAll(stateCustomIdentifiers)

        this.customFlagNames.clear()
        this.customFlagNames.putAll(stateCustomFlagNames)

        this.customLabelNames.clear()
        this.customLabelNames.putAll(stateCustomLabelNames)

        this.flags.clear()
        this.flags.putAll(stateFlags)

        this.data.clear()
        this.data.putAll(stateData)

        context.valueStack.restoreSnapshot(valueStackSnapshot)
    }

    fun saveState(context: Context<Any>) {
        states[context.level] = ParserState(
                this.silence,
                this.game,
                this.strictParsing,

                this.customIdentifiers.entries.toTypedArray(),
                this.customFlagNames.entries.toTypedArray(),
                this.customLabelNames.entries.toTypedArray(),
                this.flags.entries.toTypedArray(),
                this.data.entries.toTypedArray(),

                context.valueStack.takeSnapshot()
        )
    }

    fun loadState(): Action<Any> = Action { context -> loadState(context); true }
    fun saveState(): Action<Any> = Action { context -> saveState(context); true }

    fun <K, V> MutableMap<K, V>.putAll(entries: Array<Map.Entry<K, V>>) {
        entries.forEach { (key, value) -> this[key] = value }
    }

    open fun OpenSpiralLanguage(): Rule = Sequence(
            clearState(),
            Action<Any> {
                startingGame = game
                startingCustomIdentifiers.clear()
                startingCustomFlagNames.clear()
                startingCustomLabelNames.clear()
                startingFlags.clear()
                startingData.clear()

                startingCustomIdentifiers.putAll(customIdentifiers)
                startingCustomFlagNames.putAll(customFlagNames)
                startingCustomLabelNames.putAll(customLabelNames)
                startingFlags.putAll(flags)
                startingData.putAll(data)

                return@Action true
            },
            Sequence(
                    "OSL Script\n",
                    OpenSpiralLines(),
                    FirstOf(
                            Sequence(
                                    Action<Any> { strictParsing },
                                    EOI
                            ),
                            Action<Any> { !strictParsing }
                    )
            ),
            Action<Any> {
                game = startingGame
                customIdentifiers.clear()
                customFlagNames.clear()
                customLabelNames.clear()
                flags.clear()
                data.clear()

                customIdentifiers.putAll(startingCustomIdentifiers)
                customFlagNames.putAll(startingCustomFlagNames)
                customLabelNames.putAll(startingCustomLabelNames)
                flags.putAll(startingFlags)
                data.putAll(startingData)

                return@Action true
            }
    )

    open fun OpenSpiralLines(): Rule = Sequence(
            ZeroOrMore(Sequence(OptionalWhitespace(), SpiralTextLine(), Ch('\n'))),
            OptionalWhitespace(),
            SpiralTextLine()
    )

    open fun SpiralTextLine(): Rule = FirstOf(
            Comment(),

            SpiralLinLine(),
            SpiralWrdLine(),

            ChangeGameDrill,
            EchoDrill,
            AddNameAliasDrill,
            AddFlagAliasDrill,
            AddLabelAliasDrill,
            HeaderOSLDrill,
            StrictParsingDrill,
            MetaIfDrill,
            ErrorDrill,
            ForLoopDrill,

//          Comment(),
//          Whitespace()
            EMPTY
    )

    open fun SpiralLinLine(): Rule =
            Sequence(
                    Action<Any> { game is HopesPeakDRGame },
                    FirstOf(
                            BasicLinTextDrill,
                            LinDialogueDrill,
                            BasicLinSpiralDrill,
                            NamedLinSpiralDrill,
                            LinBustSpriteDrill,
                            LinHideSpriteDrill,
                            LinUIDrill,
                            LinIfDrill,
                            LinChoicesDrill,
                            LinMarkLabelDrill,
                            LinGoToDrill,
                            LinSetFlagDrill,

                            LinScreenFadeDrill,
                            LinScreenFlashDrill
                    )
            )

    open fun SpiralWrdLine(): Rule =
            Sequence(
                    Action<Any> { game === V3 },
                    FirstOf(
                            BasicWrdSpiralDrill,
                            NamedWrdSpiralDrill,
                            WordCommandDrill,
                            WordStringDrill
                    )
            )

    override fun toRule(obj: Any?): Rule {
        when (obj) {
            is DrillHead<*> -> return obj.Syntax(this)
            is String -> return IgnoreCase(obj)
            else -> return super.toRule(obj)
        }
    }

    fun parse(lines: String): ParsingResult<Any> {
        val runner = ReportingParseRunner<Any>(OpenSpiralLanguage())
        return runner.run(lines.replace("\r\n", "\n"))
    }

    fun load(name: String): ByteArray? = oslContext(name)

    fun copy(): OpenSpiralLanguageParser {
        val copy = OpenSpiralLanguageParser(oslContext)

        copy.game = game
        copy.customIdentifiers.putAll(customIdentifiers)
        copy.flags.putAll(flags)

        return copy
    }

    val COLOUR_CODES = mapOf(
            "white_big" to 9,
            "white" to 0,
            "pink" to 1,
            "purple" to 2,
            "yellow" to 3,
            "blue_glow" to 23,
            "blue" to 4,
            "grey" to 5,
            "gray" to 5,
            "green" to 6,
            "red_big" to 11,
            "red" to 10,
            "cyan" to 24,
            "salmon" to 33,
            "slightly_darker_blue" to 34,

            "break" to 17,
            "noise" to 20,
            "consent" to 69
    )

    val HEX_CODES = mapOf(
            "FFFFFF" to 0,
            "B766F4" to 1,
            "5C1598" to 2,
            "DEAB00" to 3,
            "54E1FF" to 4,
            "383838" to 5,
            "52FF13" to 6,
            "FE0008" to 10,
            "1A51E8" to 11,
            "FF6A6E" to 33,
            "6DCAFF" to 34,
            "252525" to 45,
            "3F3F3F" to 47,
            "585858" to 48,
            "FF9900" to 61
    )

    open fun LinText(cmd: String, vararg allBut: Char): Rule =
            Sequence(
                    clearTmpStack("LIN-TEXT-$cmd"),
                    OneOrMore(FirstOf(
                            Sequence(
                                    RuleWithVariables(OneOrMore(AllButMatcher(charArrayOf('\\', '\n').plus(allBut)))),
                                    '\\',
                                    FirstOf('&', '#'),
                                    Action<Any> { context ->
                                        pushTmpAction("LIN-TEXT-$cmd", "${pop()}&").run(context)
                                        return@Action true
                                    }
                            ),
                            Sequence(
                                    RuleWithVariables(ZeroOrMore(AllButMatcher(charArrayOf('&', '\n').plus(allBut)))),
                                    "&clear",
                                    Action<Any> { context ->
                                        val text = pop().toString()

                                        pushTmpAction("LIN-TEXT-$cmd", text).run(context)
                                        pushTmpAction("LIN-TEXT-$cmd", "<CLT>").run(context)
                                        return@Action true
                                    }
                            ),
                            Sequence(
                                    RuleWithVariables(ZeroOrMore(AllButMatcher(charArrayOf('&', '#', '\n').plus(allBut)))),
                                    FirstOf(
                                            Sequence(
                                                    '&',
                                                    FirstOf(COLOUR_CODES.keys.toTypedArray()),
                                                    Action<Any> { push(match()) },
                                                    Action<Any> { context ->
                                                        val colour = pop().toString()
                                                        val text = pop().toString()

                                                        pushTmpAction("LIN-TEXT-$cmd", text).run(context)
                                                        pushTmpAction("LIN-TEXT-$cmd", "<CLT ${COLOUR_CODES[colour]
                                                                ?: 0}>").run(context)
                                                        return@Action true
                                                    }
                                            ),
                                            Sequence(
                                                    '#',
                                                    FirstOf(HEX_CODES.keys.toTypedArray()),
                                                    Action<Any> { push(match()) },
                                                    Action<Any> { context ->
                                                        val colour = pop().toString()
                                                        val text = pop().toString()

                                                        pushTmpAction("LIN-TEXT-$cmd", text).run(context)
                                                        pushTmpAction("LIN-TEXT-$cmd", "<CLT ${HEX_CODES[colour]
                                                                ?: 0}>").run(context)
                                                        return@Action true
                                                    }
                                            )
                                    )
                            ),
                            Sequence(
                                    RuleWithVariables(ZeroOrMore(AllButMatcher(charArrayOf('&', '#', '\n').plus(allBut)))),
                                    FirstOf(
                                            Sequence(
                                                    '&',
                                                    '{',
                                                    FirstOf(COLOUR_CODES.keys.toTypedArray()),
                                                    Action<Any> { push(match()) },
                                                    '}',
                                                    Whitespace(),
                                                    Action<Any> { context ->
                                                        val colour = pop().toString()
                                                        val text = pop().toString()

                                                        pushTmpAction("LIN-TEXT-$cmd", text).run(context)
                                                        pushTmpAction("LIN-TEXT-$cmd", "<CLT ${COLOUR_CODES[colour]
                                                                ?: 0}>").run(context)
                                                        return@Action true
                                                    }
                                            ),
                                            Sequence(
                                                    '#',
                                                    '{',
                                                    FirstOf(HEX_CODES.keys.toTypedArray()),
                                                    Action<Any> { push(match()) },
                                                    '}',
                                                    Whitespace(),
                                                    Action<Any> { context ->
                                                        val colour = pop().toString()
                                                        val text = pop().toString()

                                                        pushTmpAction("LIN-TEXT-$cmd", text).run(context)
                                                        pushTmpAction("LIN-TEXT-$cmd", "<CLT ${HEX_CODES[colour]
                                                                ?: 0}>").run(context)
                                                        return@Action true
                                                    }
                                            )
                                    )
                            ),
                            Sequence(
                                    RuleWithVariables(OneOrMore(AllButMatcher(charArrayOf('\n').plus(allBut)))),
                                    pushTmpFromStack("LIN-TEXT-$cmd")
                            )
                    )),
                    operateOnTmpActions("LIN-TEXT-$cmd") { stack ->
                        if (!tmpStack.containsKey(cmd))
                            tmpStack[cmd] = LinkedList()
                        tmpStack[cmd]!!.push(stack.joinToString(""))
                    }
            )

    override fun Parameter(cmd: String): Rule = Sequence(
            ParameterToStack(),
            pushTmpFromStack(cmd)
    )

    open fun ParameterToStack(): Rule = FirstOf(
            Sequence(
                    '"',
                    RuleWithVariables(OneOrMore(ParamMatcher)),
                    '"'
            ),
            RuleWithVariables(OneOrMore(AllButMatcher(whitespace)))
    )

    override fun ParameterBut(cmd: String, vararg allBut: Char): Rule = FirstOf(
            Sequence(
                    '"',
                    RuleWithVariables(OneOrMore(ParamMatcher)),
                    pushTmpFromStack(cmd),
                    '"'
            ),
            Sequence(
                    RuleWithVariables(OneOrMore(AllButMatcher(whitespace.plus(allBut)))),
                    pushTmpFromStack(cmd)
            )
    )

    open fun RuleWithVariables(matching: Rule): Rule =
            Sequence(
                    FirstOf(
                            matching,
                            Sequence(
                                    '"',
                                    '%',
                                    OneOrMore(ParamMatcher),
                                    Action<Any> { match() in data || match() == "GAME" },
                                    '"'
                            ),
                            Sequence(
                                    '%',
                                    OneOrMore(ParamMatcher),
                                    Action<Any> { match() in data || match() == "GAME" }
                            )
                    ),
                    Action<Any> {
                        var str = match()
                        str = str.replace("%GAME", game.names.first())

                        for ((key, value) in data)
                            str = str.replace("%$key", value.toString())

                        push(str)
                        return@Action true
                    }
            )

    open fun Flag(): Rule =
            FirstOf(
                    Sequence(
                            ParameterToStack(),
                            Action<Any> {
                                val flagName = pop()

                                val id = customFlagNames[flagName] ?: return@Action false

                                val group = id shr 8
                                val flagID = id % 256

                                push(flagID)
                                push(group)
                            }
                    ),
                    Sequence(
                            OneOrMore(Digit()),
                            Action<Any> { push(match()) },

                            OptionalWhitespace(),
                            ',',
                            OptionalWhitespace(),

                            OneOrMore(Digit()),
                            Action<Any> {
                                val flagID = match()
                                val groupID = pop()

                                push(flagID)
                                push(groupID)
                            }
                    ),
                    Sequence(
                            OneOrMore(Digit()),
                            Action<Any> {
                                val id = match().toIntOrNull() ?: return@Action false

                                val group = id shr 8
                                val flagID = id % 256

                                push(flagID)
                                push(group)
                            }
                    )
            )

    open fun FlagValue(): Rule =
            FirstOf(
                    Sequence(
                            OneOrMore(Digit()),
                            pushToStack()
                    ),
                    Sequence(
                            "true",
                            pushToStack(1)
                    ),
                    Sequence(
                            "false",
                            pushToStack(0)
                    )
            )

    open fun Label(): Rule =
            FirstOf(
                    Sequence(
                            ParameterToStack(),
                            Action<Any> {
                                val labelName = pop()

                                val id = customLabelNames[labelName] ?: return@Action false

                                push(id % 256)
                                push(id shr 8)
                            }
                    ),
                    Sequence(
                            OneOrMore(Digit()),
                            Action<Any> { push(match()) },

                            OptionalWhitespace(),
                            ',',
                            OptionalWhitespace(),

                            OneOrMore(Digit()),
                            Action<Any> {
                                val first = match()
                                val second = pop()

                                push(first)
                                push(second)
                            }
                    ),
                    Sequence(
                            OneOrMore(Digit()),
                            Action<Any> {
                                val id = match().toIntOrNull() ?: return@Action false

                                push(id % 256)
                                push(id shr 8)
                            }
                    )
            )

    open fun FrameCount(): Rule =
            FirstOf(
                    Sequence(
                            Decimal(),
                            pushToStack(),
                            FirstOf('s', Sequence(Whitespace(), "seconds")),
                            Action<Any> {
                                val seconds = pop().toString().toFloatOrNull() ?: 1.0f
                                push((seconds * FRAMES_PER_SECOND).toInt())
                            }
                    ),
                    Sequence(
                            OneOrMore(Digit()),
                            pushToStack(),
                            Whitespace(),
                            Optional("frames")
                    )
            )
}