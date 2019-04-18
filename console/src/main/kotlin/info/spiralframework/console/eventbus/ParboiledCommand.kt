package info.spiralframework.console.eventbus

import info.spiralframework.base.util.printlnErr
import info.spiralframework.base.util.printlnErrLocale
import info.spiralframework.base.util.printlnLocale
import info.spiralframework.console.Cockpit
import info.spiralframework.console.data.ParboiledMarker
import org.greenrobot.eventbus.Subscribe
import org.parboiled.Rule
import org.parboiled.errors.ParseError
import org.parboiled.parserunners.ReportingParseRunner

open class ParboiledCommand(val cockpit: Cockpit<*>, val rule: Rule, val scope: String? = null, val failedCommand: ParboiledCommand.(List<ParseError>) -> Unit, val command: ParboiledCommand.(List<Any>) -> Boolean) {
    companion object {
        val FAILURE: Boolean = true
        val SUCCESS: Boolean = false

        fun succeed(text: String, vararg params: Any): Boolean {
            printlnLocale(text, *params)
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

        fun fail(text: String, vararg params: Any): Boolean {
            printlnLocale(text, *params)
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
        
        val invalidCommand: ParboiledCommand.(List<ParseError>) -> Unit = { failed ->
            if (failed.isNotEmpty()) {
                printlnErrLocale("commands.invalid")
                failed.mapNotNull(ParseError::getErrorMessage).distinct().forEach { error -> printlnErr("\t$error") }
            } else {
                printlnErrLocale("commands.unk_error")
            }
        }
    }

    constructor(rule: Rule, scope: String? = null, cockpit: Cockpit<*>, command: ParboiledCommand.(List<Any>) -> Boolean) : this(cockpit, rule, scope, invalidCommand, command)
    constructor(rule: Rule, scope: String? = null, cockpit: Cockpit<*>, help: String, command: ParboiledCommand.(List<Any>) -> Boolean) : this(cockpit, rule, scope, { printlnErr(help) }, command)

    val runner = ReportingParseRunner<Any>(rule)
    var failed: Boolean = false

    @Subscribe
    fun handle(request: CommandRequest) {
        if (scope != null && cockpit.with { operationScope }.scopeName != scope)
            return

        val command = request.command

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
                        is ParboiledMarker.SUCCESS_BASE -> request.register(this)
                        is ParboiledMarker.FAILED_LOCALE -> printlnLocale(marker.localeMsg)
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