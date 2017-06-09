package org.abimon.util

import org.parboiled.Action
import org.parboiled.BaseParser
import org.parboiled.Rule
import java.util.*

fun pushAction(parser: BaseParser<Any>, value: Any? = null): Action<Any> = Action { parser.push(value ?: parser.match()) }

val tmpStack = HashMap<String, LinkedList<Any>>()
var tmp: Any? = null
var param: Any? = null
fun clearTmpStack(cmd: String): Action<Any> = Action { tmpStack.remove(cmd); return@Action true }
fun pushTmpAction(parser: BaseParser<Any>, cmd: String, value: Any? = null): Action<Any> = Action { if (!tmpStack.containsKey(cmd)) tmpStack[cmd] = LinkedList(); tmpStack[cmd]!!.push(value ?: parser.match()); true }
fun pushTmpFromStack(parser: BaseParser<Any>, cmd: String): Action<Any> = Action { if (!tmpStack.containsKey(cmd)) tmpStack[cmd] = LinkedList(); tmpStack[cmd]!!.push(parser.pop()); true }
fun pushTmpStack(parser: BaseParser<Any>, cmd: String): Action<Any> = Action { parser.push(tmpStack.remove(cmd)?.reversed() ?: LinkedList<Any>()) }
fun operateOnTmpStack(parser: BaseParser<Any>, cmd: String, operate: (Any) -> Unit): Action<Any> = Action { if(tmpStack.containsKey(cmd)) tmpStack[cmd]!!.forEach(operate); true }
fun pushStackToTmp(parser: BaseParser<Any>, cmd: String): Action<Any> = Action { pushTmpAction(parser, cmd, parser.pop() ?: return@Action true).run(it) }

fun pushToStack(parser: BaseParser<Any>): Action<Any> = Action { parser.push(parser.match()) }

fun copyTmp(from: String, to: String): Action<Any> = Action { if (!tmpStack.containsKey(to)) tmpStack[to] = LinkedList(); (tmpStack[from] ?: return@Action true).reversed().forEach { tmpStack[to]!!.push(it) }; tmpStack[from]!!.clear(); true }

fun popTmpFromStack(parser: BaseParser<Any>): Action<Any> = Action { tmp = parser.pop(); return@Action true }
fun pushTmpToStack(parser: BaseParser<Any>): Action<Any> = Action { parser.push(tmp ?: return@Action true) }

fun popParamFromStack(parser: BaseParser<Any>): Action<Any> = Action { param = parser.pop(); return@Action true }
fun pushParamToStack(parser: BaseParser<Any>): Action<Any> = Action { parser.push(param ?: return@Action true) }
fun pushParamToTmp(parser: BaseParser<Any>, cmd: String): Action<Any> = Action { pushTmpAction(parser, cmd, param ?: return@Action true).run(it) }

fun clearTmpStack(): Action<Any> = Action { tmp = null; return@Action true }
fun clearParam(): Action<Any> = Action { param = null; return@Action true }

/** param should push to the stack when matching */
fun BaseParser<Any>.ParamList(cmd: String, param: Rule, delimiter: Rule = Ch('\n')): Rule = Sequence(ZeroOrMore(clearParam(), param, popParamFromStack(this), delimiter, pushParamToTmp(this, "$cmd-params")), param, pushTmpFromStack(this, "$cmd-params"), copyTmp("$cmd-params", cmd))

fun BaseParser<Any>.makeCommand(make: BaseParser<Any>.() -> Rule): Rule = make(this)
//fun BaseParser<Any>.Parameter(key: String): Rule = Sequence(
//        '"',
//        OneOrMore(ParameterMatcher),
//        pushTmpAction(this, key),
//        '"'
//)

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

fun BaseParser<*>.Digit(): Rule = Digit(10)
fun BaseParser<*>.Digit(base: Int): Rule = FirstOf(AnyOf(digitsLower.sliceArray(0 until base)), AnyOf(digitsUpper.sliceArray(0 until base)))
fun BaseParser<*>.Whitespace(): Rule = OneOrMore(AnyOf(whitespace))


//val voice: MusicalParser = Parboiled.createParser(MusicalParser::class.java)
//fun parse(song: String, env: MessageEnvironment) {
//    val runner = ReportingParseRunner<Any>(voice.Song("\n"))
//    for (line in song.split('\n')) {
//        val result = runner.run(line)
//        if (!result.parseErrors.isEmpty())
//            println(ErrorUtils.printParseError(result.parseErrors[0]))
//        else {
//            result.valueStack.reversed().forEach { value ->
//                if (value is LinkedList<*>) (value[0] as ICommand).execute(value.subList(1, value.size).toTypedArray(), env)
//            }
//        }
//    }
//}