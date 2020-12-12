package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.KnolusUnion
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.modules.functionregistry.registerFunctionWithoutReturn
import dev.brella.knolus.modules.functionregistry.registerMemberFunctionWithoutReturn
import dev.brella.knolus.run
import dev.brella.knolus.stringTypeParameter
import dev.brella.knolus.transform.KnolusTransVisitorRestrictions
import dev.brella.knolus.transform.parseKnolusTransRule
import dev.brella.knolus.types.KnolusString
import dev.brella.knolus.types.asType
import dev.brella.kornea.errors.common.doOnFailure
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.errors.common.doOnThrown
import dev.brella.kornea.errors.common.filter
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.getOrBreak
import info.spiralframework.antlr.pipeline.PipelineLexer
import info.spiralframework.antlr.pipeline.PipelineParser
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.logging.error
import info.spiralframework.base.common.text.doublePadWindowsPaths
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.data.HelpDetails
import info.spiralframework.console.jvm.pipeline.KnolusTypedWrapper
import info.spiralframework.console.jvm.pipeline.PipelineVisitor
import info.spiralframework.console.jvm.pipeline.jobParameter
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.console.jvm.pipeline.wrap
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.antlr.v4.runtime.CharStreams
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.util.regex.PatternSyntaxException

object GurrenWatchtower : CommandRegistrar {
    override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
        with(knolusContext) {
            registerFunctionWithoutReturn("cancel", jobParameter("job")) { job ->
                job.cancel()
                println("Job cancelled")
            }
            registerMemberFunctionWithoutReturn(jobParameter(), "cancel") { job ->
                job.cancel()
                println("Job cancelled")
            }

            registerFunctionWithContextWithoutReturn("watch", stringTypeParameter("name"), stringTypeParameter("folder"), stringTypeParameter("filter", default = ".*")) { context, name, folder, filter ->
                val regex = try {
                    filter.toRegex()
                } catch (regex: PatternSyntaxException) {
                    println("Bad filter: $filter")
                    return@registerFunctionWithContextWithoutReturn
                }

                val spiralContext = context.spiralContext().getOrBreak { return@registerFunctionWithContextWithoutReturn }

                context[name].flatMap { value -> value.asType(context, KnolusTypedWrapper.JOB) }
                    .filter { job -> job.inner.isActive }
                    .doOnSuccess {
                        println("'$name' is already running!")
                        return@registerFunctionWithContextWithoutReturn
                    }

                println("Please enter the script to run whenever a file changes")

                var newLineCount = 0
                val buffer = StringBuilder()

                while (newLineCount < 1) {
                    print(">>> ")
                    val line = readLine() ?: return@registerFunctionWithContextWithoutReturn
                    if (line.isBlank()) newLineCount++
                    else buffer.appendLine(line.doublePadWindowsPaths())
                }

                val input = buffer.toString()

                val lexer = PipelineLexer(CharStreams.fromString(input))
                parseKnolusTransRule(
                    input,
                    KnolusTransVisitorRestrictions.Permissive,
                    ::PipelineLexer,
                    ::PipelineParser,
                    ::PipelineVisitor
                ) { parser, visitor -> visitor.visitScope(parser.scope()) }
                    .doOnFailure {
                        val commandEntered = input.split('\n')
                            .dropLastWhile(String::isBlank)
                            .lastOrNull()
                            ?.substringBefore('(')
                            ?.trim()
                            ?.let(HelpDetails::sanitiseFunctionIdentifier)

                        if (commandEntered == null) {
                            spiralContext.printlnLocale("commands.unknown")
                            return@doOnFailure
                        }

                        val commandSimilarity =
                            GurrenPilot.helpCommands.values.distinct()
                                .mapNotNull { key -> Pair(key, GurrenPilot.helpFor(spiralContext, key)?.cmdKey?.commonPrefixWith(commandEntered, true)?.length ?: return@mapNotNull null) }
                                .maxByOrNull(Pair<String, Int>::second)

                        if (commandSimilarity == null) {
                            spiralContext.printlnLocale("commands.unknown")
                            return@doOnFailure
                        }

                        val help = GurrenPilot.helpFor(spiralContext, commandSimilarity.first)

                        when {
                            commandSimilarity.second == commandSimilarity.first.length ->
                                spiralContext.printlnLocale("commands.usage", help?.usage ?: commandSimilarity.first)
                            commandSimilarity.second > commandSimilarity.first.length / 3 ->
                                spiralContext.printlnLocale("commands.did_you_mean", help?.cmd ?: commandSimilarity.first)
                            else ->
                                spiralContext.printlnLocale("commands.unknown")
                        }
                    }.doOnThrown { withException ->
                        spiralContext.error("ANTLR Parsing Error: ", withException.exception)
                    }.doOnSuccess { (scope) ->
                        context[name, true] = wrap(GurrenPilot.launch {
                            val watcher = FileSystems.getDefault().newWatchService()
                            val folderPath = Path.of(folder)
                            folderPath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)

                            while (isActive) {
                                val watchKey = watcher.poll()
                                if (watchKey == null) {
                                    delay(5_000)
                                    continue
                                }

                                watchKey.pollEvents().forEach { event ->
                                    when (event.kind()) {
                                        StandardWatchEventKinds.OVERFLOW -> return@forEach
                                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                                            val path = event.context() as Path
                                            val child = folderPath.resolve(path)

                                            if (child.toString().matches(regex)) {
                                                (scope as KnolusUnion.ScopeType).run(GurrenPilot.globalContext) {
                                                    this["changed"] = KnolusString(child.toString())
                                                }
                                            }
                                        }
                                    }
                                }

                                watchKey.reset()

                                yield()
                            }
                        })
                    }
            }
        }
    }
}