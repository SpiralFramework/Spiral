package org.abimon.osl

import org.parboiled.*
import org.parboiled.annotations.BuildParseTree
import java.util.*

@BuildParseTree
open class SpiralParser(parboiledCreated: Boolean): BaseParser<Any>() {
    companion object {
        operator fun invoke(oslContext: (String) -> ByteArray?): OpenSpiralLanguageParser = Parboiled.createParser(OpenSpiralLanguageParser::class.java, true)
    }

    fun pushValue(value: Any): Unit {
        this.push(value)
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
    open fun Whitespace(): Rule = OneOrMore(AnyOf(whitespace))
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
}