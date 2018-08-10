package org.abimon.osl

import org.abimon.osl.drills.DrillHead
import org.abimon.osl.drills.circuits.*
import org.abimon.osl.drills.headerCircuits.*
import org.abimon.osl.drills.lin.*
import org.abimon.osl.drills.nonstopDebate.NonstopDebateBasicDrill
import org.abimon.osl.drills.nonstopDebate.NonstopDebateNamedDrill
import org.abimon.osl.drills.nonstopDebate.NonstopDebateNewObjectDrill
import org.abimon.osl.drills.nonstopDebate.NonstopDebateTimeLimitDrill
import org.abimon.osl.drills.stx.STXSetLanguageDrill
import org.abimon.osl.drills.wrd.*
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.*
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.parboiled.Action
import org.parboiled.Context
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.support.Chars
import org.parboiled.support.ParsingResult
import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.safeCast

open class OpenSpiralLanguageParser(private val oslContext: (String) -> ByteArray?, isParboiledCreated: Boolean) : SpiralParser(isParboiledCreated) {
    companion object {
        val FRAMES_PER_SECOND = 60

        var DEFAULT_STDOUT: PrintStream = System.out
        var DEFAULT_MAX_FOR_RANGE: Int = 1000


        operator fun invoke(oslContext: (String) -> ByteArray?): OpenSpiralLanguageParser = Parboiled.createParser(OpenSpiralLanguageParser::class.java, oslContext, true)

        fun readFromJar(name: String): ByteArray? =
                this::class.java.classLoader.getResourceAsStream(name)?.readBytes()
    }

    var gameContext: GameContext? = null
    var drGame: DRGame?
        get() = (gameContext as? GameContext.DRGameContext)?.game
        set(value) {
            gameContext = when (value) {
                DR1 -> GameContext.DR1GameContext
                DR2 -> GameContext.DR2GameContext
                UDG -> GameContext.UDGGameContext
                UnknownHopesPeakGame -> GameContext.UnknownHopesPeakGameContext
                V3 -> GameContext.V3GameContextObject
                is HopesPeakDRGame -> GameContext.CatchAllHopesPeakGameContext(value)
                else -> null
            }
        }
    var hopesPeakGame: HopesPeakDRGame?
        get() = (gameContext as? GameContext.HopesPeakGameContext)?.game
        set(value) {
            gameContext = when (value) {
                DR1 -> GameContext.DR1GameContext
                DR2 -> GameContext.DR2GameContext
                UDG -> GameContext.UDGGameContext
                UnknownHopesPeakGame -> GameContext.UnknownHopesPeakGameContext
                null -> null
                else -> GameContext.CatchAllHopesPeakGameContext(value)
            }
        }
    var v3Game: V3?
        get() = (gameContext as? GameContext.V3GameContext)?.game
        set(value) {
            gameContext = when (value) {
                V3 -> GameContext.V3GameContextObject
                null -> null
                else -> GameContext.CatchAllV3GameContext(value)
            }
        }

    var nonstopDebateGame: HopesPeakKillingGame?
        get() = (gameContext as? GameContext.NonstopDebateContext)?.game
        set(value) {
            gameContext = when (value) {
                DR1 -> GameContext.DR1NonstopDebateContext
                DR2 -> GameContext.DR2NonstopDebateContext
                UnknownHopesPeakGame -> GameContext.UnknownHopesPeakNonstopDebateContext
                null -> null
                else -> GameContext.CatchAllNonstopDebateContext(value)
            }
        }

    var strictParsing: Boolean = true

    var localiser: (String) -> String = String::toString

    var localisationFile: File?
        get() = null
        set(file) {
            localiser = { unlocalised ->
                file?.useLines { lines -> lines.firstOrNull { localised -> localised.startsWith("$unlocalised=") }?.substringAfter("$unlocalised=")?.replace("\\n", "\n") }
                        ?: unlocalised
            }
        }

    val flags = HashMap<String, Boolean>()
    val data = HashMap<String, Any>()

    val customIdentifiers: MutableMap<String, Int> by dataProperty("custom_identifiers", ::HashMap)
    val customFlagNames: MutableMap<String, Int> by dataProperty("custom_flag_names", ::HashMap)
    val customLabelNames: MutableMap<String, Int> by dataProperty("custom_label_names", ::HashMap)
    val customItemNames: MutableMap<String, Int> by dataProperty("custom_item_names", ::HashMap)
    val customAnimationNames: MutableMap<String, Int> by dataProperty("cusotm_animation_names", ::HashMap)

    val macros: MutableMap<String, String> by dataProperty("macros", ::HashMap)

    val wordScriptLabels: MutableList<String> by dataProperty("word_script_labels", ::ArrayList)
    val wordScriptParameters: MutableList<String> by dataProperty("word_script_parameters", ::ArrayList)
    val wordScriptStrings: MutableList<String> by dataProperty("word_script_strings", ::ArrayList)

    val states = HashMap<Int, ParserState>()

    val startingFlags = HashMap<String, Boolean>()
    val startingData = HashMap<String, Any>()

    val uuid = UUID.randomUUID().toString()
    val labels: MutableList<Int> = LinkedList()
    var flagCheckIndentation = 0

    var stdout: PrintStream = DEFAULT_STDOUT
    var maxForLoopFange: Int = DEFAULT_MAX_FOR_RANGE
    var allowReadingFromJar: Boolean = true

    var environment: DRGame? = null

    fun findLabel(): Int {
        var labelID = (0xFF * 0xFF) - 1

        while (labelID in labels)
            labelID--

        labels.add(labelID)
        return labelID
    }

    operator fun get(key: String): Any? = data[key]
    operator fun <T : Any> get(key: String, clazz: KClass<T>): T? = clazz.safeCast(data[key])

    operator fun set(key: String, value: Any?): Any? {
        if (silence)
            return null

        if (value == null)
            return data.remove(key)
        return data.put(key, value)
    }

    fun loadParserState(state: ParserState) {
        val (stateSilence, stateGame, stateStrictParsing, stateFlags, stateData, labels) = state

        this.silence = stateSilence
        this.gameContext = stateGame
        this.strictParsing = stateStrictParsing

        this.flags.clear()
        this.flags.putAll(stateFlags)

        this.data.clear()
        this.data.putAll(stateData)

        this.labels.clear()
        this.labels.addAll(labels)
    }

    fun saveParserState(): ParserState = ParserState(
            this.silence,
            this.gameContext,
            this.strictParsing,

            this.flags.entries.map { (a, b) -> a to b }.toTypedArray(),
            this.data.entries.map { (a, b) -> a to b }.toTypedArray(),

            this.labels.toTypedArray(),

            null
    )

    fun loadState(context: Context<Any>) {
        val (stateSilence, stateGame, stateStrictParsing, stateFlags, stateData, labels, valueStackSnapshot) = (states.remove(context.level)
                ?: return)

        this.silence = stateSilence
        this.gameContext = stateGame
        this.strictParsing = stateStrictParsing

        this.flags.clear()
        this.flags.putAll(stateFlags)

        this.data.clear()
        this.data.putAll(stateData)

        this.labels.clear()
        this.labels.addAll(labels)

        context.valueStack.restoreSnapshot(valueStackSnapshot)
    }

    fun saveState(context: Context<Any>) {
        states[context.level] = ParserState(
                this.silence,
                this.gameContext,
                this.strictParsing,

                this.flags.entries.map { (a, b) -> a to b }.toTypedArray(),
                this.data.entries.map { (a, b) -> a to b }.toTypedArray(),

                this.labels.toTypedArray(),

                context.valueStack.takeSnapshot()
        )
    }

    fun clearState(context: Context<Any>) {
        states.remove(context.level)
    }

    fun loadState(): Action<Any> = Action { context -> loadState(context); true }
    fun saveState(): Action<Any> = Action { context -> saveState(context); true }
    fun clearStateAction(): Action<Any> = Action { context -> clearState(context); true }

    fun <K, V> MutableMap<K, V>.putAll(entries: Array<Map.Entry<K, V>>) {
        entries.forEach { (key, value) -> this[key] = value }
    }

    open fun OpenSpiralHeader(): Rule = Sequence(
            clearState(),
            Action<Any> {
                startingFlags.clear()
                startingData.clear()

                startingFlags.putAll(flags)
                startingData.putAll(data)

                return@Action true
            },
            Sequence(
                    "OSL Script\n",
                    Action<Any> { push(arrayOf(null, match())) },
                    OpenSpiralHeaderLines(),
                    OptionalWhitespace(),
                    FirstOf(
                            Sequence(
                                    Action<Any> { strictParsing },
                                    EOI
                            ),
                            Action<Any> { !strictParsing }
                    )
            ),
            Action<Any> {
                flags.clear()
                data.clear()

                flags.putAll(startingFlags)
                data.putAll(startingData)

                return@Action true
            }
    )

    open fun OpenSpiralLanguage(): Rule = Sequence(
            clearState(),
            Action<Any> {
                startingFlags.clear()
                startingData.clear()

                startingFlags.putAll(flags)
                startingData.putAll(data)

                return@Action true
            },
            Sequence(
                    "OSL Script\n",
                    OpenSpiralLines(),
                    OptionalWhitespace(),
                    FirstOf(
                            Sequence(
                                    Action<Any> { strictParsing },
                                    EOI
                            ),
                            Action<Any> { !strictParsing }
                    )
            ),
            Action<Any> {
                flags.clear()
                data.clear()

                flags.putAll(startingFlags)
                data.putAll(startingData)

                return@Action true
            }
    )

    open fun OpenSpiralHeaderLines(): Rule = Sequence(
            OptionalWhitespace(),
            SpiralHeaderLine(),
            ZeroOrMore(Sequence(Optional(OptionalInlineWhitespace(), Comment()), Ch('\n'), OptionalWhitespace(), SpiralHeaderLine()))
    )

    open fun OpenSpiralLines(): Rule = Sequence(
            OptionalWhitespace(),
            SpiralTextLine(),
            ZeroOrMore(Sequence(Optional(OptionalInlineWhitespace(), Comment()), Ch('\n'), OptionalWhitespace(), SpiralTextLine()))
    )

    open fun SpiralHeaderLine(): Rule =
            FirstOf(
                    Action<Any> { false },
                    AddMacroDrill,
                    ForLoopDrill,
                    HeaderOSLDrill,
                    MacroDrill,
                    ItemSelectionDrill,

                    Sequence(
                            Sequence(
                                    OneOrMore(
                                            OneOrMore(AllButMatcher(charArrayOf('\n', '&'))),
                                            '&',
                                            '{',
                                            OneOrMore(AllButMatcher(charArrayOf('"', '}'))),
                                            '}'
                                    ),
                                    ZeroOrMore(AllButMatcher(charArrayOf('\n', Chars.EOI, '}')))
                            ),
                            Action<Any> { push(arrayOf(null, match())) }
                    ),
                    Sequence(
                            Sequence(
                                    OneOrMore(AllButMatcher(charArrayOf('\n', '{'))),
                                    '{',
                                    '\n'
                            ),
                            Action<Any> { push(arrayOf(null, match())) },
                            OpenSpiralHeaderLines(),
                            Sequence(
                                    OptionalWhitespace(),
                                    '}'
                            ),
                            Action<Any> { push(arrayOf(null, match())) }
                    ),
                    Sequence(
                            OneOrMore(AllButMatcher(charArrayOf('\n', Chars.EOI, '}'))),
                            Action<Any> { push(arrayOf(null, match())) }
                    )
            )

    open fun SpiralTextLine(): Rule = FirstOf(
            Comment(),

            SpiralLinLine(),
            SpiralWrdLine(),
            SpiralSTXLine(),
            SpiralNonstopDebateLine(),

            CompileAsDrill,
            ChangeGameDrill,
            ChangeContextDrill,
            EchoDrill,
            AddNameAliasDrill,
            AddFlagAliasDrill,
            AddLabelAliasDrill,
            AddItemNameAliasDrill,
            AddAnimationAliasDrill,
            StrictParsingDrill,
            MetaIfDrill,
            ErrorDrill,

            SetDataDrill,

//          Comment(),
//          Whitespace()
            EMPTY
    )

    open fun SpiralLinLine(): Rule =
            Sequence(
                    Action<Any> { gameContext is GameContext.HopesPeakGameContext },
                    FirstOf(
                            BasicLinTextDrill,
                            LinDialogueDrill,
                            LinSpriteDrill,
                            LinHideSpriteDrill,
                            LinUIDrill,
                            LinCameraFocusDrill,
                            LinIfDrill,
                            LinIfGameStateDrill,
                            LinIfRandDrill,
                            LinChoicesDrill,
                            LinMarkLabelDrill,
                            LinGoToDrill,
                            LinSetFlagDrill,
                            LinSpeakerDrill,
                            InternalLinItemSelectionDrill,

                            LinScreenFadeDrill,
                            LinScreenFlashDrill,

                            LinArithmeticGameState,
                            LinRandChoicesDrill,

                            LinAnimationDrill,

                            BasicLinSpiralDrill,
                            NamedLinSpiralDrill
                    )
            )

    open fun SpiralWrdLine(): Rule =
            Sequence(
                    Action<Any> { gameContext is GameContext.V3GameContext },
                    FirstOf(
                            WordCommandDrill,
                            WordStringDrill,

                            WrdDialogueDrill,

                            BasicWrdTextDrill,
                            WrdSpeakerDrill,

                            BasicWrdSpiralDrill,
                            NamedWrdSpiralDrill
                    )
            )

    open fun SpiralSTXLine(): Rule =
            Sequence(
                    Action<Any> { gameContext is GameContext.STXGameContext },
                    FirstOf(
                            WordStringDrill,
                            STXSetLanguageDrill
                    )
            )

    open fun SpiralNonstopDebateLine(): Rule =
            Sequence(
                    Action<Any> { gameContext is GameContext.NonstopDebateContext },
                    FirstOf(
                            NonstopDebateNewObjectDrill,
                            NonstopDebateTimeLimitDrill,

                            NonstopDebateBasicDrill,
                            NonstopDebateNamedDrill
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
        val headerRunner = ReportingParseRunner<Any>(OpenSpiralHeader())

        var stack: List<Any>
        var script: String = lines.replace("\r\n", "\n")

        do {
            stack = headerRunner.run(script).valueStack.reversed()
            script = stack.joinToString("\n") { str -> (str as Array<*>)[1].toString().trim() }
        } while (stack.any { value -> (value as Array<*>)[0] != null })

        val runner = ReportingParseRunner<Any>(OpenSpiralLanguage())
        return runner.run(script)
    }

    fun parseWithOutput(lines: String): Pair<ParsingResult<Any>, String> {
        val headerRunner = ReportingParseRunner<Any>(OpenSpiralHeader())

        var stack: List<Any>
        var script: String = lines.replace("\r\n", "\n")

        do {
            stack = headerRunner.run(script).valueStack.reversed()
            script = stack.joinToString("\n") { str -> (str as Array<*>)[1].toString().trim() }
        } while (stack.any { value -> (value as Array<*>)[0] != null })

        val runner = ReportingParseRunner<Any>(OpenSpiralLanguage())
        return runner.run(script) to script
    }

    fun load(name: String): ByteArray? = oslContext(name) ?: if (allowReadingFromJar) readFromJar(name) else null

    fun copy(): OpenSpiralLanguageParser {
        val copy = OpenSpiralLanguageParser(oslContext)
        val state = saveParserState()

        copy.stdout = stdout
        copy.maxForLoopFange = maxForLoopFange
        copy.localiser = localiser
        copy.allowReadingFromJar = allowReadingFromJar
        copy.loadParserState(state)

        return copy
    }

    val COLOUR_CODES = mapOf(
            DR1 to mapOf(
                    "pink" to 1,
                    "purple" to 2,
                    "yellow" to 3,
                    "blue" to 4,
                    "grey" to 7,
                    "gray" to 7,
                    "orange" to 9,
                    "turquoise" to 10,
                    "salmon" to 11,
                    "green" to 23,
                    "small_grey" to 24,
                    "small_gray" to 24
            ),
            DR2 to mapOf(
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
    )

    val WRD_COLOUR_CODES = mapOf(
            V3 to mapOf(
                    "yellow" to "cltSTRONG",
                    "blue" to "cltMIND",
                    "green" to "cltSYSTEM",

                    "bold" to "cltSTRONG",
                    "strong" to "cltSTRONG",

                    "mind" to "cltMIND",

                    "narrator" to "cltSYSTEM",
                    "system" to "cltSYSTEM"
            )
    )

    val HEX_CODES = mapOf(
            DR2 to mapOf(
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
    )

    open fun LinText(cmd: String, vararg allBut: Char): Rule =
            FirstOf(
                    Sequence(
                            clearTmpStack("LIN-TEXT-$cmd"),
                            "local.",
                            RuleWithVariables(OneOrMore(AllButMatcher(whitespace.plus(charArrayOf('\\', '\n')).plus(allBut)))),
                            Action<Any> { context ->
                                val localString = localiser(pop().toString())
                                val parserCopy = copy()
                                val stack = parserCopy.parse("OSL Script\nText|$localString").valueStack

                                if (stack.isEmpty) {
                                    pushTmp("LIN-TEXT-$cmd", localString)
                                    return@Action true
                                }

                                val value = stack.pop()

                                if (value is List<*>) {
                                    val text = value[1].toString()
                                    pushTmp("LIN-TEXT-$cmd", text)
                                } else {
                                    pushTmp("LIN-TEXT-$cmd", localString)
                                }

                                return@Action true
                            },
                            operateOnTmpActions("LIN-TEXT-$cmd") { stack ->
                                if (!tmpStack.containsKey(cmd))
                                    tmpStack[cmd] = LinkedList()
                                tmpStack[cmd]!!.push(stack.joinToString(""))
                            }
                    ),
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
                                                            FirstOf(COLOUR_CODES.flatMap { (_, values) -> values.keys }.toTypedArray()),
                                                            Action<Any> { push(match()) },
                                                            Action<Any> { context ->
                                                                val colour = pop().toString()
                                                                val text = pop().toString()

                                                                pushTmpAction("LIN-TEXT-$cmd", text).run(context)
                                                                pushTmpAction("LIN-TEXT-$cmd", "<CLT ${
                                                                (COLOUR_CODES[hopesPeakGame ?: UnknownHopesPeakGame]
                                                                        ?: emptyMap())[colour]
                                                                        ?: 0}>").run(context)
                                                                return@Action true
                                                            }
                                                    ),
                                                    Sequence(
                                                            '#',
                                                            FirstOf(HEX_CODES.flatMap { (_, values) -> values.keys }.toTypedArray()),
                                                            Action<Any> { push(match()) },
                                                            Action<Any> { context ->
                                                                val colour = pop().toString()
                                                                val text = pop().toString()

                                                                pushTmpAction("LIN-TEXT-$cmd", text).run(context)
                                                                pushTmpAction("LIN-TEXT-$cmd", "<CLT ${
                                                                (HEX_CODES[hopesPeakGame ?: UnknownHopesPeakGame]
                                                                        ?: emptyMap())[colour]
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
                                                            "{clear}",
                                                            InlineWhitespace(),
                                                            Action<Any> { context ->
                                                                val text = pop().toString()

                                                                pushTmpAction("LIN-TEXT-$cmd", text).run(context)
                                                                pushTmpAction("LIN-TEXT-$cmd", "<CLT>").run(context)
                                                                return@Action true
                                                            }
                                                    ),
                                                    Sequence(
                                                            '&',
                                                            FirstOf("{br}", "{break}", "{newline}"),
                                                            InlineWhitespace(),
                                                            Action<Any> {
                                                                val text = pop().toString()

                                                                pushTmp("LIN-TEXT-$cmd", text)
                                                                pushTmp("LIN-TEXT-$cmd", "\n")
                                                                return@Action true
                                                            }
                                                    ),
                                                    Sequence(
                                                            '&',
                                                            '{',
                                                            FirstOf(COLOUR_CODES.flatMap { (_, values) -> values.keys }.toTypedArray()),
                                                            Action<Any> { push(match()) },
                                                            '}',
                                                            InlineWhitespace(),
                                                            Action<Any> {
                                                                val colour = pop().toString()
                                                                val text = pop().toString()

                                                                pushTmp("LIN-TEXT-$cmd", text)
                                                                pushTmp("LIN-TEXT-$cmd", "<CLT ${
                                                                (COLOUR_CODES[hopesPeakGame ?: UnknownHopesPeakGame]
                                                                        ?: emptyMap())[colour]
                                                                        ?: 0}>")
                                                                return@Action true
                                                            }
                                                    ),
                                                    Sequence(
                                                            '#',
                                                            '{',
                                                            FirstOf(HEX_CODES.flatMap { (_, values) -> values.keys }.toTypedArray()),
                                                            Action<Any> { push(match()) },
                                                            '}',
                                                            InlineWhitespace(),
                                                            Action<Any> {
                                                                val colour = pop().toString()
                                                                val text = pop().toString()

                                                                pushTmp("LIN-TEXT-$cmd", text)
                                                                pushTmp("LIN-TEXT-$cmd", "<CLT ${
                                                                (HEX_CODES[hopesPeakGame ?: UnknownHopesPeakGame]
                                                                        ?: emptyMap())[colour]
                                                                        ?: 0}>")
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
            )

    open fun WrdText(cmd: String, vararg allBut: Char): Rule =
            FirstOf(
                    Sequence(
                            clearTmpStack("WRD-TEXT-$cmd"),
                            "local.",
                            RuleWithVariables(OneOrMore(AllButMatcher(whitespace.plus(charArrayOf('\\', '\n')).plus(allBut)))),
                            Action<Any> { context ->
                                val localString = localiser(pop().toString())
                                val parserCopy = copy()
                                val stack = parserCopy.parse("OSL Script\nText|$localString").valueStack

                                if (stack.isEmpty) {
                                    pushTmp("WRD-TEXT-$cmd", localString)
                                    return@Action true
                                }

                                val value = stack.pop()

                                if (value is List<*>) {
                                    val text = value[1].toString()
                                    pushTmp("WRD-TEXT-$cmd", text)
                                } else {
                                    pushTmp("WRD-TEXT-$cmd", localString)
                                }

                                return@Action true
                            },
                            operateOnTmpActions("WRD-TEXT-$cmd") { stack ->
                                if (!tmpStack.containsKey(cmd))
                                    tmpStack[cmd] = LinkedList()
                                tmpStack[cmd]!!.push(stack.joinToString(""))
                            }
                    ),
                    Sequence(
                            clearTmpStack("WRD-TEXT-$cmd"),
                            OneOrMore(FirstOf(
                                    Sequence(
                                            RuleWithVariables(OneOrMore(AllButMatcher(charArrayOf('\\', '\n').plus(allBut)))),
                                            '\\',
                                            FirstOf('&', '#'),
                                            Action<Any> { context ->
                                                pushTmpAction("WRD-TEXT-$cmd", "${pop()}&").run(context)
                                                return@Action true
                                            }
                                    ),
                                    Sequence(
                                            RuleWithVariables(ZeroOrMore(AllButMatcher(charArrayOf('&', '\n').plus(allBut)))),
                                            "&clear",
                                            Action<Any> { context ->
                                                val text = pop().toString()

                                                pushTmpAction("WRD-TEXT-$cmd", text).run(context)
                                                pushTmpAction("WRD-TEXT-$cmd", "<CLT>").run(context)
                                                return@Action true
                                            }
                                    ),
                                    Sequence(
                                            RuleWithVariables(ZeroOrMore(AllButMatcher(charArrayOf('&', '#', '\n').plus(allBut)))),
                                            Sequence(
                                                    '&',
                                                    FirstOf(WRD_COLOUR_CODES.flatMap { (_, values) -> values.keys }.toTypedArray()),
                                                    Action<Any> { push(match()) },
                                                    Action<Any> { context ->
                                                        val colour = pop().toString()
                                                        val text = pop().toString()

                                                        pushTmpAction("WRD-TEXT-$cmd", text).run(context)
                                                        pushTmpAction("WRD-TEXT-$cmd", "<CLT=${
                                                        (v3Game?.let { game -> WRD_COLOUR_CODES[game] }
                                                                ?: emptyMap())[colour]
                                                                ?: "cltNORMAL"}>").run(context)
                                                        return@Action true
                                                    }
                                            )
                                    ),
                                    Sequence(
                                            RuleWithVariables(ZeroOrMore(AllButMatcher(charArrayOf('&', '#', '\n').plus(allBut)))),
                                            FirstOf(
                                                    Sequence(
                                                            '&',
                                                            "{clear}",
                                                            InlineWhitespace(),
                                                            Action<Any> { context ->
                                                                val text = pop().toString()

                                                                pushTmpAction("WRD-TEXT-$cmd", text).run(context)
                                                                pushTmpAction("WRD-TEXT-$cmd", "<CLT>").run(context)
                                                                return@Action true
                                                            }
                                                    ),
                                                    Sequence(
                                                            '&',
                                                            '{',
                                                            FirstOf(WRD_COLOUR_CODES.flatMap { (_, values) -> values.keys }.toTypedArray()),
                                                            Action<Any> { push(match()) },
                                                            '}',
                                                            InlineWhitespace(),
                                                            Action<Any> { context ->
                                                                val colour = pop().toString()
                                                                val text = pop().toString()

                                                                pushTmpAction("WRD-TEXT-$cmd", text).run(context)
                                                                pushTmpAction("WRD-TEXT-$cmd", "<CLT=${
                                                                (v3Game?.let { game -> WRD_COLOUR_CODES[game] }
                                                                        ?: emptyMap())[colour]
                                                                        ?: "cltNORMAL"}>").run(context)
                                                                return@Action true
                                                            }
                                                    )
                                            )
                                    ),
                                    Sequence(
                                            RuleWithVariables(OneOrMore(AllButMatcher(charArrayOf('\n').plus(allBut)))),
                                            pushTmpFromStack("WRD-TEXT-$cmd")
                                    )
                            )),
                            operateOnTmpActions("WRD-TEXT-$cmd") { stack ->
                                if (!tmpStack.containsKey(cmd))
                                    tmpStack[cmd] = LinkedList()
                                tmpStack[cmd]!!.push(stack.joinToString(""))
                            }
                    )
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
            RuleWithVariables(OneOrMore(AllButMatcher(whitespace.plus(charArrayOf(',', '|')))))
    )

    override fun ParameterBut(cmd: String, vararg allBut: Char): Rule = FirstOf(
            Sequence(
                    '"',
                    RuleWithVariables(OneOrMore(ParamMatcher)),
                    pushTmpFromStack(cmd),
                    '"'
            ),
            Sequence(
                    RuleWithVariables(OneOrMore(AllButMatcher(whitespace.plus(charArrayOf(',', '|')).plus(allBut)))),
                    pushTmpFromStack(cmd)
            )
    )

    open fun RuleWithVariables(matching: Rule): Rule =
            Sequence(
                    FirstOf(
                            Sequence(
                                    matching,
                                    Action<Any> { push(match()) }
                            ),
                            Sequence(
                                    '"',
                                    Sequence(
                                            '%',
                                            OneOrMore(ParamMatcher)
                                    ),
                                    Action<Any> { match().substring(1).let { key -> key in data || key == "GAME" || key == "ENVIRONMENT" } },
                                    Action<Any> { push(match()) },
                                    '"'
                            ),
                            Sequence(
                                    Sequence(
                                            '%',
                                            OneOrMore(AllButMatcher(whitespace.plus(charArrayOf(',', '|', ')'))))
                                    ),
                                    Action<Any> { match().substring(1).let { key -> key in data || key == "GAME" || key == "ENVIRONMENT" } },
                                    Action<Any> { push(match()) }
                            )
                    ),
                    Action<Any>
                    {
                        var str = pop().toString()
                        str = str.replace("%GAME", drGame?.names?.firstOrNull() ?: "None")
                        str = str.replace("%ENVIRONMENT", environment?.names?.firstOrNull() ?: "None")

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
                            RuleWithVariables(OneOrMore(Digit())),

                            OptionalInlineWhitespace(),
                            ',',
                            OptionalInlineWhitespace(),

                            RuleWithVariables(OneOrMore(Digit())),
                            Action<Any> {
                                val flagID = pop()
                                val groupID = pop()

                                push(flagID)
                                push(groupID)
                            }
                    ),
                    Sequence(
                            RuleWithVariables(OneOrMore(Digit())),
                            Action<Any> {
                                val id = pop().toString().toIntOrNull() ?: return@Action false

                                val group = id shr 8
                                val flagID = id % 256

                                push(flagID)
                                push(group)
                            }
                    )
            )

    open fun GameState(): Rule = RuleWithVariables(OneOrMore(Digit()))

    open fun FlagValue(): Rule =
            FirstOf(
                    RuleWithVariables(OneOrMore(Digit())),
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
                            RuleWithVariables(OneOrMore(Digit())),

                            OptionalInlineWhitespace(),
                            ',',
                            OptionalInlineWhitespace(),

                            RuleWithVariables(OneOrMore(Digit())),
                            Action<Any> {
                                val first = pop()
                                val second = pop()

                                push(first)
                                push(second)
                            }
                    ),
                    Sequence(
                            RuleWithVariables(OneOrMore(Digit())),
                            Action<Any> {
                                val id = pop().toString().toIntOrNull() ?: return@Action false

                                push(id % 256)
                                push(id shr 8)
                            }
                    )
            )

    open fun SpeakerName(): Rule =
            FirstOf(
                    Sequence(
                            ParameterToStack(),
                            Action<Any> {
                                val speakerName = pop()

                                val game = hopesPeakGame ?: UnknownHopesPeakGame
                                val id = customIdentifiers[speakerName]
                                        ?: game.characterIdentifiers[speakerName]
                                        ?: return@Action false

                                push(id % 256)
                            }
                    ),
                    RuleWithVariables(OneOrMore(Digit()))
            )

    open fun FrameCount(): Rule =
            FirstOf(
                    Sequence(
                            RuleWithVariables(Decimal()),
                            FirstOf('s', Sequence(InlineWhitespace(), "seconds")),
                            Action<Any> {
                                val seconds = pop().toString().toFloatOrNull() ?: 1.0f
                                push((seconds * FRAMES_PER_SECOND).toInt())
                            }
                    ),
                    Sequence(
                            RuleWithVariables(OneOrMore(Digit())),
                            InlineWhitespace(),
                            Optional("frames")
                    )
            )

    open fun ItemID(): Rule =
            FirstOf(
                    RuleWithVariables(OneOrMore(Digit())),
                    Sequence(
                            ParameterToStack(),
                            Action<Any> {
                                val name = pop().toString()
                                if (name in customItemNames)
                                    return@Action push(customItemNames[name] ?: 0)

                                val index = (hopesPeakGame?.itemNames ?: emptyArray()).indexOf(name)

                                if (index == -1)
                                    return@Action false

                                return@Action push(index)
                            }
                    )
            )

    open fun AnimationID(): Rule =
            FirstOf(
                    Sequence(
                            ParameterToStack(),
                            Action<Any> {
                                val labelName = pop()

                                val id = customAnimationNames[labelName] ?: return@Action false

                                push(id % 256)
                                push(id shr 8)
                            }
                    ),
                    Sequence(
                            RuleWithVariables(OneOrMore(Digit())),

                            CommaSeparator(),

                            RuleWithVariables(OneOrMore(Digit())),
                            Action<Any> {
                                val first = pop()
                                val second = pop()

                                push(first)
                                push(second)
                            }
                    ),
                    Sequence(
                            "fla_",
                            RuleWithVariables(OneOrMore(Digit())),
                            Action<Any> {
                                val id = pop().toString().toIntOrNull() ?: return@Action false

                                push(id % 256)
                                push(id shr 8)
                            }
                    )
            )

    fun ensureString(string: String, cmd: EnumWordScriptCommand) {
        when (cmd) {
            EnumWordScriptCommand.LABEL -> if (string !in wordScriptLabels) wordScriptLabels.add(string)
            EnumWordScriptCommand.PARAMETER -> if (string !in wordScriptParameters) wordScriptParameters.add(string)
            EnumWordScriptCommand.STRING -> if (string !in wordScriptStrings) wordScriptStrings.add(string)
            EnumWordScriptCommand.RAW -> return
        }

        push(listOf(SpiralDrillBit(WordCommandDrill), cmd, string))
    }

    fun ensureParam(cmd: String, index: Int, wordCmd: EnumWordScriptCommand): Action<Any> = Action {
        if (silence)
            return@Action true

        if (tmpStack.containsKey(cmd))
            ensureString(tmpStack[cmd]!!.reversed()[index].toString(), wordCmd)

        return@Action true
    }

    inline fun <reified T : Any> dataProperty(key: String, crossinline defaultValue: () -> T):
            ReadOnlyProperty<Any?, T> = object : ReadOnlyProperty<Any?, T> {
        /**
         * Returns the value of the property for the given object.
         * @param thisRef the object for which the value is requested.
         * @param property the metadata for the property.
         * @return the property value.
         */
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (key !in data || data[key] !is T)
                data[key] = defaultValue()

            return data[key] as? T ?: defaultValue()
        }
    }
}