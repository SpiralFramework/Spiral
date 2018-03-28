package org.abimon.osl

import org.abimon.osl.drills.*
import org.abimon.osl.drills.circuits.AddNameAliasDrill
import org.abimon.osl.drills.circuits.ChangeGameDrill
import org.abimon.osl.drills.circuits.EchoDrill
import org.abimon.osl.drills.circuits.HeaderOSLDrill
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.parboiled.*
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.support.ParsingResult
import java.util.*

@BuildParseTree
open class OpenSpiralLanguageParser(private val oslContext: (String) -> ByteArray?, isParboiledCreated: Boolean) : BaseParser<Any>() {
    companion object {
        operator fun invoke(oslContext: (String) -> ByteArray?): OpenSpiralLanguageParser = Parboiled.createParser(OpenSpiralLanguageParser::class.java, oslContext, true)
    }

    var game: HopesPeakDRGame = UnknownHopesPeakGame
    val customIdentifiers = HashMap<String, Int>()
    val flags = HashMap<String, Boolean>()

    val uuid = UUID.randomUUID().toString()

    open fun OpenSpiralLanguage(): Rule = Sequence(
            clearState(),
            Sequence("OSL Script\n", ZeroOrMore(Sequence(SpiralTextLine(), Ch('\n'))), SpiralTextLine()),
            EOI,
            Action<Any> {
                game = UnknownHopesPeakGame
                customIdentifiers.clear()
                flags.clear()
                return@Action true
            }
    )

    open fun SpiralTextLine(): Rule = FirstOf(
            Comment(),

            BasicTextDrill,
            DialogueDrill,
            BasicSpiralDrill,
            NamedSpiralDrill,
            BustSpriteDrill,
            UIDrill,

            ChangeGameDrill,
            EchoDrill,
            AddNameAliasDrill,
            HeaderOSLDrill,

//            Comment(),
//            Whitespace()
            EMPTY
    )

    override fun toRule(obj: Any?): Rule {
        when (obj) {
            is DrillHead<*> -> return obj.Syntax(this)
            else -> return super.toRule(obj)
        }
    }

    fun pushValue(value: Any): Unit {
        this.push(value)
    }

    fun parse(lines: String): ParsingResult<Any> {
        val runner = ReportingParseRunner<Any>(OpenSpiralLanguage())
        return runner.run(lines)
    }

    fun load(name: String): ByteArray? = oslContext(name)

    fun copy(): OpenSpiralLanguageParser {
        val copy = OpenSpiralLanguageParser(oslContext)

        copy.game = game
        copy.customIdentifiers.putAll(customIdentifiers)
        copy.flags.putAll(flags)

        return copy
    }

    //ParseUtils

    fun pushAction(value: Any? = null): Action<Any> = Action {
        push(value ?: match())
    }

    val tmpStack = HashMap<String, LinkedList<Any>>()
    var tmp: Any? = null
    var param: Any? = null

    fun clearState(): Action<Any> = Action { tmpStack.clear(); tmp = null; param = null; return@Action true }

    fun clearTmpStack(cmd: String): Action<Any> = Action { tmpStack.remove(cmd); return@Action true }
    fun pushTmpAction(cmd: String, value: Any? = null): Action<Any> = Action {
        if (!tmpStack.containsKey(cmd)) tmpStack[cmd] = LinkedList(); tmpStack[cmd]!!.push(value ?: match()); true
    }

    fun peekTmpAction(cmd: String): Any? = tmpStack[cmd]?.peek()

    fun pushTmpFromStack(cmd: String): Action<Any> = Action { if (!tmpStack.containsKey(cmd)) tmpStack[cmd] = LinkedList(); if (!it.valueStack.isEmpty) tmpStack[cmd]!!.push(pop()); true }
    fun pushTmpStack(cmd: String): Action<Any> = Action { context ->
        context.valueStack.push(tmpStack.remove(cmd)?.reversed() ?: LinkedList<Any>()); true
    }

    fun pushAndOperateTmpStack(cmd: String, operate: (Context<Any>, List<Any>) -> Unit): Action<Any> = Action { context ->
        val stack = tmpStack.remove(cmd)?.reversed() ?: LinkedList<Any>()
        context.valueStack.push(stack)
        operate(context, stack)
        return@Action true
    }

    fun operateOnTmpStack(cmd: String, operate: (Any) -> Unit): Action<Any> = Action { tmpStack[cmd]?.forEach(operate); true }
    fun operateOnTmpActions(cmd: String, operate: (List<Any>) -> Unit): Action<Any> = Action { if (tmpStack.containsKey(cmd)) operate(tmpStack[cmd]!!.reversed()); true }
    fun pushStackToTmp(cmd: String): Action<Any> = Action { context ->
        pushTmpAction(cmd, pop() ?: return@Action true).run(context)
    }

    fun pushToStack(parser: BaseParser<Any>): Action<Any> = Action { parser.push(parser.match()) }

    fun copyTmp(from: String, to: String): Action<Any> = Action {
        if (!tmpStack.containsKey(to)) tmpStack[to] = LinkedList(); (tmpStack[from]
            ?: return@Action true).reversed().forEach { tmpStack[to]!!.push(it) }; tmpStack[from]!!.clear(); true
    }

    fun popTmpFromStack(parser: BaseParser<Any>): Action<Any> = Action { tmp = parser.pop(); return@Action true }
    fun pushTmpToStack(parser: BaseParser<Any>): Action<Any> = Action { parser.push(tmp ?: return@Action true) }

    fun popParamFromStack(parser: BaseParser<Any>): Action<Any> = Action { param = if (it.valueStack.isEmpty) null else parser.pop(); return@Action true }
    fun pushParamToStack(parser: BaseParser<Any>): Action<Any> = Action { parser.push(param ?: return@Action true) }
    fun pushParamToTmp(cmd: String): Action<Any> = Action { context ->
        pushTmpAction(cmd, param ?: return@Action true).run(context)
    }

    fun clearTmpStack(): Action<Any> = Action { tmp = null; return@Action true }
    fun clearParam(): Action<Any> = Action { param = null; return@Action true }

    val digitsLower = charArrayOf(
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    )

    val digitsUpper = charArrayOf(
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z'
    )

    val whitespace = (Character.MIN_VALUE until Character.MAX_VALUE).filter { Character.isWhitespace(it) }.toCharArray()

    open fun Digit(): Rule = Digit(10)
    open fun Digit(base: Int): Rule = FirstOf(AnyOf(digitsLower.sliceArray(0 until base)), AnyOf(digitsUpper.sliceArray(0 until base)))
    open fun Whitespace(): Rule = OneOrMore(AnyOf(whitespace))
    open fun Parameter(cmd: String): Rule = FirstOf(
            Sequence(
                    '"',
                    OneOrMore(ParamMatcher),
                    pushTmpAction(cmd),
                    '"'
            ),
            Sequence(
                    AllButMatcher(whitespace),
                    pushTmpAction(cmd)
            )
    )

    /** param should push to the stack when matching */
    open fun ParamList(cmd: String, param: Rule, delimiter: Rule): Rule = Sequence(ZeroOrMore(clearParam(), param, popParamFromStack(this), delimiter, pushParamToTmp("$cmd-params")), param, pushTmpFromStack("$cmd-params"), copyTmp("$cmd-params", cmd))

    open fun Comment(): Rule = FirstOf(
            Sequence("//", ZeroOrMore(LineMatcher)),
            Sequence("#", ZeroOrMore(LineMatcher)),
            Sequence(
                    "/**",
                    ZeroOrMore(
                            FirstOf(
                                    Sequence(
                                            OneOrMore(AllButMatcher(charArrayOf('\\'))),
                                            '\\',
                                            '*'
                                    ),
                                    AllButMatcher(charArrayOf('*'))
                            )
                    ),
                    "*/"
            )
    )

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

    open fun LinText(cmd: String): Rule =
            Sequence(
                    clearTmpStack("LIN-TEXT-$cmd"),
                    OneOrMore(FirstOf(
                            Sequence(
                                    OneOrMore(AllButMatcher(charArrayOf('\\', '\n'))),
                                    Action<Any> { push(match()) },
                                    '\\',
                                    FirstOf('&', '#'),
                                    Action<Any> { context ->
                                        pushTmpAction("LIN-TEXT-$cmd", "${pop()}&").run(context)
                                        return@Action true
                                    }
                            ),
                            Sequence(
                                    ZeroOrMore(AllButMatcher(charArrayOf('&', '\n'))),
                                    Action<Any> { push(match()) },
                                    "&clear",
                                    Action<Any> { context ->
                                        val text = pop().toString()

                                        pushTmpAction("LIN-TEXT-$cmd", text).run(context)
                                        pushTmpAction("LIN-TEXT-$cmd", "<CLT>").run(context)
                                        return@Action true
                                    }
                            ),
                            Sequence(
                                    ZeroOrMore(AllButMatcher(charArrayOf('&', '#', '\n'))),
                                    Action<Any> { push(match()) },
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
                                    ZeroOrMore(AllButMatcher(charArrayOf('&', '#', '\n'))),
                                    Action<Any> { push(match()) },
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
                                    OneOrMore(AllButMatcher(charArrayOf('\n'))),
                                    pushTmpAction("LIN-TEXT-$cmd")
                            )
                    )),
                    operateOnTmpActions("LIN-TEXT-$cmd") { stack ->
                        if (!tmpStack.containsKey(cmd))
                            tmpStack[cmd] = LinkedList()
                        tmpStack[cmd]!!.push(stack.joinToString(""))
                    }
            )
}