package info.spiralframework.osl

import info.spiralframework.osl.drills.DrillHead
import org.parboiled.Action
import org.parboiled.BaseParser
import org.parboiled.Context
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.support.Var
import java.awt.Color
import java.util.*
import java.util.concurrent.TimeUnit

@BuildParseTree
abstract class SpiralParser(parboiledCreated: Boolean) : BaseParser<Any>() {
    var silence = false

    fun silence(): Action<Any> = Action { silence = true; true }
    fun desilence(): Action<Any> = Action { silence = false; true }

    fun pushValue(value: Any): Unit {
        if (!silence)
            this.push(value)
    }

    override fun push(value: Any?): Boolean {
        if (!silence)
            return super.push(value)
        return true
    }

    override fun pop(): Any {
        if (!silence)
            return super.pop()
        return super.peek()
    }

    override fun pop(down: Int): Any {
        if (!silence)
            return super.pop(down)
        return super.peek(down)
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
            tmpStack[cmd]!!.push(pop())
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
    open fun InlineWhitespaceCharacter(): Rule = AnyOf(charArrayOf('\t', ' '))
    open fun InlineWhitespace(): Rule = OneOrMore(InlineWhitespaceCharacter())
    open fun OptionalInlineWhitespace(): Rule = ZeroOrMore(InlineWhitespaceCharacter())
    open fun Parameter(cmd: String): Rule = FirstOf(
            Sequence(
                    '"',
                    OneOrMore(ParamMatcher),
                    pushTmpAction(cmd),
                    '"'
            ),
            Sequence(
                    OneOrMore(info.spiralframework.osl.AllButMatcher(whitespace)),
                    pushTmpAction(cmd)
            )
    )

    open fun ParameterToStack(): Rule = FirstOf(
            Sequence(
                    '"',
                    OneOrMore(ParamMatcher),
                    pushToStack(),
                    '"'
            ),
            Sequence(
                    OneOrMore(info.spiralframework.osl.AllButMatcher(whitespace)),
                    pushToStack()
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
                    OneOrMore(info.spiralframework.osl.AllButMatcher(whitespace.plus(allBut))),
                    pushTmpAction(cmd)
            )
    )

    open fun WhitespaceSandwich(rule: Rule): Rule = Sequence(OptionalInlineWhitespace(), rule, OptionalInlineWhitespace())

    open fun SurroundedRule(rule: Rule, vararg surrounding: Pair<Rule, Rule>): Rule =
            FirstOf(surrounding.map { (prefix, suffix) -> Sequence(prefix, rule, suffix) }.toTypedArray())

    /** param should push to the stack when matching */
    open fun ParamList(cmd: String, param: Rule, delimiter: Rule): Rule {
        val parameters = Var<MutableList<Any>>(ArrayList())
        return Sequence(
                Action<Any> { parameters.get().clear(); true },
                OptionalInlineWhitespace(),
                param,
                Action<Any> { parameters.get().add(pop()) },
                ZeroOrMore(Sequence(delimiter, OptionalInlineWhitespace(), param, Action<Any> { parameters.get().add(pop()) })),
                Action<Any> {
                    if (!tmpStack.containsKey(cmd))
                        tmpStack[cmd] = LinkedList()
                    val stack = tmpStack[cmd]!!
                    parameters.get().forEach(stack::push)
                    return@Action true
                }
        )
    }

    open fun Comment(): Rule = FirstOf(
            Sequence("//", ZeroOrMore(LineMatcher)),
            Sequence("#", ZeroOrMore(LineMatcher)),
            Sequence(
                    "/**",
                    ZeroOrMore(
                            FirstOf(
                                    Sequence(
                                            OneOrMore(info.spiralframework.osl.AllButMatcher(charArrayOf('\\'))),
                                            '\\',
                                            '*'
                                    ),
                                    info.spiralframework.osl.AllButMatcher(charArrayOf('*'))
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

                    OptionalInlineWhitespace(),
                    ',',
                    OptionalInlineWhitespace(),

                    OneOrMore(Digit()),
                    Action<Any> { push(match()) },

                    OptionalInlineWhitespace(),
                    ',',
                    OptionalInlineWhitespace(),

                    OneOrMore(Digit()),
                    Action<Any> { push(match()) },
                    OptionalInlineWhitespace(),
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

    open fun MapValue(vararg pairs: kotlin.Pair<String, Any>): Rule = MapValue(pairs.toMap())
    open fun MapValue(map: Map<String, Any>): Rule = Sequence(
            ParameterToStack(),
            Action<Any> {
                val key = pop().toString()

                val value = map[key] ?: return@Action false
                push(value)
            }
    )

    open fun <T> FirstOfKey(vararg pairs: kotlin.Pair<T, Rule>, obtainKey: (Context<Any>) -> T?): Rule =
            FirstOf(pairs.map { (key, rule) -> Sequence(Action<Any> { context -> obtainKey(context) == key }, rule) }.toTypedArray())

    @Suppress("UNCHECKED_CAST")
    open fun <T> MapWithKey(map: Map<T, Any>, obtainKey: (Context<Any>) -> T?): Action<Any> =
            Action<Any> {
                val value = map[obtainKey(context)] ?: return@Action false
                push(value)
            }

    @Suppress("UNCHECKED_CAST")
    open fun <T> MapValueWithKey(map: Map<T, Map<String, Any>>, obtainKey: (Context<Any>) -> T?): Rule = Sequence(
            Action<Any> { context ->
                val key = obtainKey(context) ?: return@Action false
                push(key)
            },
            ParameterToStack(),
            Action<Any> {
                val key = pop().toString()
                val mapKey = pop() as? T ?: return@Action false

                val value = (map[mapKey] ?: return@Action false)[key] ?: return@Action false
                push(value)
            }
    )

    open fun MapValueInsensitive(vararg pairs: kotlin.Pair<String, Any>): Rule = MapValueInsensitive(pairs.toMap())
    open fun MapValueInsensitive(map: Map<String, Any>): Rule = Sequence(
            ParameterToStack(),
            Action<Any> {
                val key = pop().toString()

                val value = map[key.toUpperCase()] ?: return@Action false
                push(value)
            }
    )

    @Suppress("UNCHECKED_CAST")
    open fun <T> MapValueInsensitiveWithKey(map: Map<T, Map<String, Any>>, obtainKey: (Context<Any>) -> T?): Rule = Sequence(
            Action<Any> { context ->
                val key = obtainKey(context) ?: return@Action false
                push(key)
            },
            ParameterToStack(),
            Action<Any> {
                val key = pop().toString()
                val mapKey = pop() as? T ?: return@Action false

                val value = (map[mapKey] ?: return@Action false)[key.toUpperCase()] ?: return@Action false
                push(value)
            }
    )


    open fun Decimal(): Rule = Decimal(OneOrMore(Digit()))
    open fun Decimal(digitRule: Rule): Rule =
            Sequence(
                    digitRule,
                    Optional(
                            Sequence(
                                    '.',
                                    digitRule
                            )
                    )
            )

    open fun Separator(): Rule =
            Sequence(
                    AnyOf(charArrayOf('|', ':')),
                    OptionalInlineWhitespace()
            )

    open fun CommaSeparator(): Rule =
            Sequence(
                    OptionalInlineWhitespace(),
                    ',',
                    OptionalInlineWhitespace()
            )

    open fun Duration(baseUnit: TimeUnit): Rule {
        val duration = Var<Long>(0)
        val tmpDuration = Var<Long>(0)

        val durationRule = Sequence(
                OneOrMore(Digit()),
                Action<Any> { tmpDuration.set(match().toLongOrNull() ?: 0) },
                OptionalInlineWhitespace(),
                FirstOf(
                        Sequence(
                                FirstOf("ms", "milliseconds", "millisecond"),
                                Action<Any> { duration.set(duration.get() + baseUnit.convert(tmpDuration.get(), TimeUnit.MILLISECONDS)) }
                        ),
                        Sequence(
                                FirstOf("s", "seconds", "second"),
                                Action<Any> { duration.set(duration.get() + baseUnit.convert(tmpDuration.get(), TimeUnit.SECONDS)) }
                        ),
                        Sequence(
                                FirstOf("m", "minutes", "minute"),
                                Action<Any> { duration.set(duration.get() + baseUnit.convert(tmpDuration.get(), TimeUnit.MINUTES)) }
                        ),
                        Sequence(
                                FirstOf("hr", "h", "hours", "hour"),
                                Action<Any> { duration.set(duration.get() + baseUnit.convert(tmpDuration.get(), TimeUnit.HOURS)) }
                        ),
                        Sequence(
                                FirstOf("d", "days", "day"),
                                Action<Any> { duration.set(duration.get() + baseUnit.convert(tmpDuration.get(), TimeUnit.DAYS)) }
                        )
                )
        )

        return Sequence(
                Action<Any> { duration.set(0) },
                durationRule,
                ZeroOrMore(
                        FirstOf(',', InlineWhitespace(), ':'),
                        OptionalInlineWhitespace(),
                        durationRule
                ),

                Action<Any> { push(duration.get()) }
        )
    }

    open fun NoopPass(context: Context<Any>): Boolean = false
    open fun NoopFail(context: Context<Any>): Boolean = false

    open fun FunctionRule(parameters: Array<Pair<Any, Rule>>, whenMissing: Array<(Context<Any>) -> Boolean> = parameters.map { return@map this::NoopFail }.toTypedArray()): Rule {
        val hasBeenPassed = Var<BooleanArray>()

        val separators = parameters.mapIndexed { index, _ -> if (index == 0) EMPTY else CommaSeparator() }
        val sequenced = Sequence(parameters.mapIndexed { index, (_, rule) -> FirstOf(Sequence(separators[index], rule), Action(whenMissing[index])) }.toTypedArray())
        val named = Sequence(
                ZeroOrMore(
                        FirstOf(parameters.mapIndexed { index, (name, rule) ->
                            Sequence(
                                    Action<Any> { !hasBeenPassed.get()[index] },
                                    if (name is Array<*>) FirstOf(name) else name.toString(),
                                    OptionalInlineWhitespace(),
                                    FirstOf(
                                            ':',
                                            '='
                                    ),
                                    OptionalInlineWhitespace(),
                                    rule,
                                    Action<Any> { hasBeenPassed.get()[index] = true; true },
                                    separators[index]
                            )
                        }.toTypedArray())
                ),

                Action<Any> { actionContext ->
                    hasBeenPassed.get().foldIndexed(true) { index, success, passed ->
                        if (passed || !success)
                            return@foldIndexed success

                        return@foldIndexed whenMissing[index](actionContext)
                    }
                }
        )


        return Sequence(
                Action<Any> { hasBeenPassed.set(BooleanArray(parameters.size) { false }) },
                '(',
                OptionalInlineWhitespace(),
                FirstOf(
                        sequenced,
                        named
                ),
                OptionalInlineWhitespace(),
                ')',
                Optional(';')
        )
    }
}
