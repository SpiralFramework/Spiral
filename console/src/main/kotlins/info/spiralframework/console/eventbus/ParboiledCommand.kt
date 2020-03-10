package info.spiralframework.console.eventbus

import info.spiralframework.base.binding.printlnErr
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.events.SpiralEventListener
import info.spiralframework.base.common.events.SpiralEventPriority
import info.spiralframework.base.common.locale.printlnErrLocale
import info.spiralframework.console.data.ParboiledMarker
import org.parboiled.Rule
import org.parboiled.errors.ParseError
import org.parboiled.parserunners.ReportingParseRunner
import kotlin.reflect.KClass

open class ParboiledCommand(val rule: Rule, val scope: String? = null, override val eventPriority: SpiralEventPriority = SpiralEventPriority.NORMAL, val failedCommand: suspend SpiralContext.(List<ParseError>) -> Unit, val command: suspend SpiralContext.(List<Any>) -> Boolean): SpiralEventListener<CommandRequest> {
    companion object {
        val FAILURE: Boolean = true
        val SUCCESS: Boolean = false

        fun SpiralContext.succeed(text: String, vararg params: Any): Boolean {
            println(localiseArray(text, params))
            return SUCCESS
        }
        
        fun succeed(action: () -> Unit): Boolean {
            action()
            return SUCCESS
        }
        
        fun succeedIf(action: () -> Boolean): Boolean {
            if (action())
                return SUCCESS
            return FAILURE
        }

        fun SpiralContext.fail(text: String, vararg params: Any): Boolean {
            println(localiseArray(text, params))
            return FAILURE
        }

        fun fail(action: () -> Unit): Boolean {
            action()
            return FAILURE
        }

        fun failIf(action: () -> Boolean): Boolean {
            if (action())
                return FAILURE
            return SUCCESS
        }
        
        val invalidCommand: suspend SpiralContext.(List<ParseError>) -> Unit = { failed ->
            if (failed.isNotEmpty()) {
                printlnErrLocale("commands.invalid")
                failed.mapNotNull(ParseError::getErrorMessage).distinct().forEach { error -> printlnErr("\t$error") }
            } else {
                printlnErrLocale("commands.unk_error")
            }
        }
    }

    constructor(rule: Rule, scope: String? = null, eventPriority: SpiralEventPriority = SpiralEventPriority.NORMAL, command: suspend SpiralContext.(List<Any>) -> Boolean) : this(rule, scope, eventPriority, invalidCommand, command)
    constructor(rule: Rule, scope: String? = null, help: String, eventPriority: SpiralEventPriority = SpiralEventPriority.NORMAL, command: suspend SpiralContext.(List<Any>) -> Boolean) : this(rule, scope, eventPriority, { printlnErr(help) }, command)

    val runner = ReportingParseRunner<Any>(rule)
    var failed: Boolean = false

    override val eventClass: KClass<CommandRequest> = CommandRequest::class

    override suspend fun SpiralContext.handle(event: CommandRequest) {
        if (scope != null && event.scope.scopeName != scope)
            return

        val command = event.command

        runner.parseErrors.clear()
        val result = runner.run(command)
        if (result.matched) {
            if (result.hasErrors()) {
                failed = true
                failedCommand(result.parseErrors)
            } else {
                val stack = result.valueStack.reversed()
                val params = stack.filterNot { param -> param is ParboiledMarker }
                val markers = stack.filterIsInstance(ParboiledMarker::class.java)

                markers.forEach { marker ->
                    when (marker) {
                        is ParboiledMarker.SUCCESS_COMMAND -> failed = command(params) == FAILURE
                        is ParboiledMarker.SUCCESS_BASE -> event.register(this@ParboiledCommand)
                        is ParboiledMarker.FAILED_LOCALE -> println(localiseArray(marker.localeMsg, marker.params))
                    }
                }

                if (ParboiledMarker.SUCCESS_BASE in markers && markers.size == 1) {
                    printlnErrLocale("commands.invalid_syntax")
                }
            }
        } else {
            failed = true
        }
//        failed = result.hasErrors()
//
//        if (result.hasErrors()) {
//            failedCommand(result.parseErrors)
//        } else {
//            failed = (command(result.valueStack.reversed()) == FAILURE)
//        }
    }
}