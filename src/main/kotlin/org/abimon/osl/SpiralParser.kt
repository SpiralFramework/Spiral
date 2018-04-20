package org.abimon.osl

import org.abimon.osl.drills.DrillHead
import org.parboiled.Action
import org.parboiled.BaseParser
import org.parboiled.Context
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import java.awt.Color
import java.util.*

@BuildParseTree
abstract class SpiralParser(parboiledCreated: Boolean) : BaseParser<Any>() {
    var silence = false

    fun pushValue(value: Any): Unit {
        if (!silence)
            this.push(value)
    }

    override fun push(value: Any?): Boolean {
        if (!silence)
            return super.push(value)
        return true
    }

    //ParseUtils

    fun pushAction(value: Any? = null): Action<Any> = Action {
        push(value ?: match())
    }

    val tmpStack = HashMap<String, LinkedList<Any>>()
    var tmp: Any? = null
    var param: Any? = null

    fun clearState(): Action<Any> = Action { tmpStack.clear(); tmp = null; param = null; return@Action true }

    fun clearTmpStack(cmd: String): Action<Any> = Action {
        if (silence)
            return@Action true

        tmpStack.remove(cmd)
        return@Action true
    }

    fun pushDrillHead(cmd: String, head: DrillHead<out Any>): Action<Any> = Action { context ->
        if (silence)
            return@Action true
        if (cmd !in tmpStack)
            tmpStack[cmd] = LinkedList()
        tmpStack[cmd]!!.push(SpiralDrillBit(head))

        return@Action true
    }

    fun pushEmptyDrillHead(cmd: String, head: DrillHead<out Any>): Action<Any> = Action { context ->
        if (silence)
            return@Action true
        if (cmd !in tmpStack)
            tmpStack[cmd] = LinkedList()
        tmpStack[cmd]!!.push(SpiralDrillBit(head, ""))

        return@Action true
    }

    fun pushTmpAction(cmd: String, value: Any? = null): Action<Any> = Action {
        if (silence)
            return@Action true

        if (!tmpStack.containsKey(cmd)) tmpStack[cmd] = LinkedList(); tmpStack[cmd]!!.push(value ?: match())
        return@Action true
    }

    fun pushTmp(cmd: String, value: Any) {
        if (silence)
            return

        if (!tmpStack.containsKey(cmd)) tmpStack[cmd] = LinkedList(); tmpStack[cmd]!!.push(value)
    }

    fun peekTmpAction(cmd: String): Any? = tmpStack[cmd]?.peek()

    fun pushTmpFromStack(cmd: String): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        if (!tmpStack.containsKey(cmd))
            tmpStack[cmd] = LinkedList()
        if (!context.valueStack.isEmpty)
            tmpStack[cmd]!!.push(pop());
        return@Action true
    }

    fun pushTmpStack(cmd: String): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        context.valueStack.push(tmpStack.remove(cmd)?.reversed() ?: LinkedList<Any>())
        return@Action true
    }

    fun pushStackWithHead(cmd: String): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        val stackToPush = tmpStack.remove(cmd)?.asReversed() ?: run {
            System.err.println("[$cmd] Error: ${context.match} does not have a stack")
            return@Action false
        }
        val drillBit = stackToPush[0] as? SpiralDrillBit ?: run {
            System.err.println("[$cmd] Error: ${context.match} did not set the first value of the stack to be a DrillBit, instead it is ${stackToPush[0]}")
            return@Action false
        }

        drillBit.script = context.match
        stackToPush[0] = drillBit

        context.valueStack.push(stackToPush)

        return@Action true
    }

    fun pushAndOperateTmpStack(cmd: String, operate: (Context<Any>, List<Any>) -> Unit): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        val stack = tmpStack.remove(cmd)?.reversed() ?: LinkedList<Any>()
        context.valueStack.push(stack)
        operate(context, stack)
        return@Action true
    }

    fun operateOnTmpStack(cmd: String, operate: (Any) -> Unit): Action<Any> = Action {
        if (silence)
            return@Action true

        tmpStack[cmd]?.reversed()?.forEach(operate);
        return@Action true
    }

    fun operateOnTmpActions(cmd: String, operate: (List<Any>) -> Unit): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        if (tmpStack.containsKey(cmd))
            operate(tmpStack[cmd]!!.reversed())
        return@Action true
    }

    fun operateOnTmpActionsWithContext(cmd: String, operate: (Context<Any>, List<Any>) -> Unit): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        if (tmpStack.containsKey(cmd))
            operate(context, tmpStack[cmd]!!.reversed());
        return@Action true
    }

    fun pushStackToTmp(cmd: String): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        pushTmpAction(cmd, pop() ?: return@Action true).run(context)
    }

    fun copyTmp(from: String, to: String): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        if (!tmpStack.containsKey(to))
            tmpStack[to] = LinkedList()
        (tmpStack[from] ?: return@Action true).reversed().forEach { item -> tmpStack[to]!!.push(item) }
        tmpStack[from]!!.clear()
        return@Action true
    }

    fun popTmpFromStack(): Action<Any> = Action {
        if (silence)
            return@Action true

        tmp = pop()
        return@Action true
    }

    fun pushTmpToStack(): Action<Any> = Action {
        if (silence)
            return@Action true

        push(tmp ?: return@Action true)
    }

    fun popParamFromStack(): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        param = if (context.valueStack.isEmpty) null else pop()
        return@Action true
    }

    fun pushParamToStack(): Action<Any> = Action {
        if (silence)
            return@Action true

        push(param ?: return@Action true)
    }

    fun pushParamToTmp(cmd: String): Action<Any> = Action { context ->
        if (silence)
            return@Action true

        pushTmpAction(cmd, param ?: return@Action true).run(context)
    }

    fun clearTmpStack(): Action<Any> = Action {
        if (silence)
            return@Action true

        tmp = null
        return@Action true
    }

    fun clearParam(): Action<Any> = Action {
        if (silence)
            return@Action true

        param = null
        return@Action true
    }

    fun pushToStack(value: Any? = null): Action<Any> = Action {
        if (silence)
            return@Action true

        return@Action push(value ?: match())
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
    open fun Whitespace(): Rule = OneOrMore(WhitespaceCharacter())
    open fun Parameter(cmd: String): Rule = FirstOf(
            Sequence(
                    '"',
                    OneOrMore(ParamMatcher),
                    pushTmpAction(cmd),
                    '"'
            ),
            Sequence(
                    OneOrMore(AllButMatcher(whitespace)),
                    pushTmpAction(cmd)
            )
    )

    open fun ParameterBut(cmd: String, vararg allBut: Char): Rule = FirstOf(
            Sequence(
                    '"',
                    OneOrMore(ParamMatcher),
                    pushTmpAction(cmd),
                    '"'
            ),
            Sequence(
                    OneOrMore(AllButMatcher(whitespace.plus(allBut))),
                    pushTmpAction(cmd)
            )
    )

    /** param should push to the stack when matching */
    open fun ParamList(cmd: String, param: Rule, delimiter: Rule): Rule = Sequence(ZeroOrMore(clearParam(), param, popParamFromStack(), delimiter, pushParamToTmp("$cmd-params")), param, pushTmpFromStack("$cmd-params"), copyTmp("$cmd-params", cmd))

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

    open val COLOURS = mapOf(
            "WHITE" to Color.WHITE.rgb,
            "LIGHT GRAY" to Color.LIGHT_GRAY.rgb,
            "LIGHT GREY" to Color.LIGHT_GRAY.rgb,
            "LIGHT_GRAY" to Color.LIGHT_GRAY.rgb,
            "LIGHT_GREY" to Color.LIGHT_GRAY.rgb,
            "GRAY" to Color.GRAY.rgb,
            "GREY" to Color.GRAY.rgb,
            "DARK GRAY" to Color.DARK_GRAY.rgb,
            "DARK GREY" to Color.DARK_GRAY.rgb,
            "DARK_GRAY" to Color.DARK_GRAY.rgb,
            "DARK_GREY" to Color.DARK_GRAY.rgb,
            "BLACK" to Color.BLACK.rgb,
            "RED" to Color.RED.rgb,
            "PINK" to Color.PINK.rgb,
            "ORANGE" to Color.ORANGE.rgb,
            "YELLOW" to Color.YELLOW.rgb,
            "GREEN" to Color.GREEN.rgb,
            "MAGENTA" to Color.MAGENTA.rgb,
            "CYAN" to Color.CYAN.rgb,
            "BLUE" to Color.BLUE.rgb
    )

    open fun Colour(): Rule = FirstOf(
            Sequence(
                    "#",
                    NTimes(6, Digit(16)),
                    Action<Any> {
                        val rgb = match().toInt(16)

                        val r = (rgb shr 16) and 0xFF
                        val g = (rgb shr 8) and 0xFF
                        val b = (rgb shr 0) and 0xFF

                        push(b)
                        push(g)
                        push(r)

                        return@Action true
                    }
            ),
            Sequence(
                    "rgb(",
                    OneOrMore(Digit()),
                    Action<Any> { push(match()) },

                    OptionalWhitespace(),
                    ',',
                    OptionalWhitespace(),

                    OneOrMore(Digit()),
                    Action<Any> { push(match()) },

                    OptionalWhitespace(),
                    ',',
                    OptionalWhitespace(),

                    OneOrMore(Digit()),
                    Action<Any> { push(match()) },
                    OptionalWhitespace(),
                    ')',

                    Action<Any> {
                        val b = pop().toString().toIntOrNull() ?: 0
                        val g = pop().toString().toIntOrNull() ?: 0
                        val r = pop().toString().toIntOrNull() ?: 0

                        push(b % 256)
                        push(g % 256)
                        push(r % 256)
                    }
            ),
            Sequence(
                    FirstOf(COLOURS.keys.toTypedArray()),
                    Action<Any> {
                        val rgb = COLOURS[match().toUpperCase()] ?: return@Action false

                        val r = (rgb shr 16) and 0xFF
                        val g = (rgb shr 8) and 0xFF
                        val b = (rgb shr 0) and 0xFF

                        push(b)
                        push(g)
                        push(r)

                        return@Action true
                    }
            )
    )

    open fun Decimal(): Rule =
            Sequence(
                    OneOrMore(Digit()),
                    Optional(
                            Sequence(
                                    '.',
                                    OneOrMore(Digit())
                            )
                    )
            )
}