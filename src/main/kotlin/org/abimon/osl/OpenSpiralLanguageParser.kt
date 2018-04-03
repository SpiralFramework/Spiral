package org.abimon.osl

import org.abimon.osl.drills.DrillHead
import org.abimon.osl.drills.LinUIDrill
import org.abimon.osl.drills.circuits.AddNameAliasDrill
import org.abimon.osl.drills.circuits.ChangeGameDrill
import org.abimon.osl.drills.circuits.EchoDrill
import org.abimon.osl.drills.circuits.HeaderOSLDrill
import org.abimon.osl.drills.lin.*
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.parboiled.Action
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.support.ParsingResult
import java.util.*


open class OpenSpiralLanguageParser(private val oslContext: (String) -> ByteArray?, isParboiledCreated: Boolean): SpiralParser(isParboiledCreated) {
    companion object {
        operator fun invoke(oslContext: (String) -> ByteArray?): OpenSpiralLanguageParser = Parboiled.createParser(OpenSpiralLanguageParser::class.java, oslContext, true)
    }

    var game: DRGame = UnknownHopesPeakGame
    val customIdentifiers = HashMap<String, Int>()
    val flags = HashMap<String, Boolean>()

    val uuid = UUID.randomUUID().toString()

    open fun OpenSpiralLanguage(): Rule = Sequence(
            clearState(),
            Sequence("OSL Script\n", ZeroOrMore(Sequence(SpiralTextLine(), Ch('\n'))), SpiralTextLine()),
            Action<Any> {
                game = UnknownHopesPeakGame
                customIdentifiers.clear()
                flags.clear()
                return@Action true
            }
    )

    open fun SpiralTextLine(): Rule = FirstOf(
            Comment(),

            BasicLinTextDrill,
            LinDialogueDrill,
            BasicLinSpiralDrill,
            NamedLinSpiralDrill,
            LinBustSpriteDrill,
            LinHideSpriteDrill,
            LinUIDrill,

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