package info.spiralframework.console.jvm

import dev.brella.knolus.KnolusUnion
import dev.brella.knolus.run
import dev.brella.knolus.transform.KnolusTransVisitorRestrictions
import dev.brella.knolus.transform.parseKnolusTransRule
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.jvm.files.AsyncFileOutputFlow
import dev.brella.kornea.toolkit.common.printLine
import info.spiralframework.antlr.pipeline.PipelineLexer
import info.spiralframework.antlr.pipeline.PipelineParser
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.text.doublePadWindowsPaths
import info.spiralframework.base.common.text.lazyString
import info.spiralframework.base.jvm.crypto.verify
import info.spiralframework.console.jvm.commands.data.HelpDetails
import info.spiralframework.console.jvm.commands.pilot.GurrenPilot
import info.spiralframework.console.jvm.data.GurrenSpiralContext
import info.spiralframework.console.jvm.pipeline.*
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY
import info.spiralframework.core.common.SPIRAL_ENV_VERSION_KEY
import kotlinx.coroutines.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonToken
import java.io.File
import kotlin.system.measureTimeMillis

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class CockpitPilot internal constructor(startingContext: GurrenSpiralContext) : Cockpit(startingContext) {
    override suspend fun start() {
        with(context) {
            println(
                localise(
                    "gurren.pilot.init", retrieveStaticValue(SPIRAL_ENV_BUILD_KEY)
                                         ?: retrieveStaticValue(SPIRAL_ENV_VERSION_KEY)
                                         ?: localise("gurren.default_version")
                )
            )

            GurrenPilot.launch {
                val file = File("stats.csv")
                file.delete()
                val runtime = Runtime.getRuntime()
                val out = AsyncFileOutputFlow(file)
                val csv = arrayOf(
                    "time" to { System.currentTimeMillis() },
                    "memory_used" to { runtime.totalMemory() - runtime.freeMemory() },
                    "memory_free" to runtime::freeMemory,
                    "memory_total" to runtime::totalMemory,
                    "memory_max" to runtime::maxMemory
                )
                out.printLine(csv.joinToString { (key) -> key })

                while (isActive) {
                    delay(200 - measureTimeMillis { out.printLine(csv.joinToString { (_, func) -> func().toString() }) })
                }
            }

            if (publicKey == null) {
                warn("gurren.pilot.plugin_load.missing_public")
            } else {
                val enabledPlugins = enabledPlugins
                val plugins = discover()
                    .filter { entry -> enabledPlugins[entry.pojo.uid] == entry.pojo.semanticVersion }
                    .filter { entry ->
                        if (entry.source == null)
                            return@filter true

                        val signature = signatureForPlugin(
                            entry.pojo.uid, entry.pojo.semanticVersion.toString(), entry.pojo.pluginFileName
                                                                                   ?: entry.source!!.path.substringAfterLast('/')
                        )
                        if (signature == null) {
                            debug(
                                "gurren.pilot.plugin_load.missing_signature", entry.pojo.name, entry.pojo.version
                                                                                               ?: entry.pojo.semanticVersion
                            )
                            return@filter false
                        }

                        return@filter entry.source
                            ?.openStream()
                            ?.verify(signature, publicKey ?: return@filter false) == true
                    }

                plugins.forEach { plugin ->
                    info(
                        "gurren.pilot.plugin_load.loading", plugin.pojo.name, plugin.pojo.version
                                                                              ?: plugin.pojo.semanticVersion
                    )
                    loadPlugin(plugin)
                }
            }

            GurrenPilot.register(context)

            GurrenPilot.globalContext["spiralContext"] = KnolusTypedWrapper(context)

            loop@ while (GurrenPilot.keepLooping.get()) {
                delay(50)
                val localScope = with { operationScope }
                print(localScope.scopePrint)

                val rawInput = readLine()
                val input: String

                if (rawInput == "script()") {
                    print(">>> ")

                    var newLineCount = 0
                    val buffer = StringBuilder()

                    while (newLineCount < 1) {
                        val line = readLine() ?: break@loop
                        if (line.isBlank()) newLineCount++
                        else buffer.appendLine(line.doublePadWindowsPaths())
                    }

                    input = buffer.toString()
                } else {
                    //TODO: Fix this bug; without the extra newline, our antlr grammar expects a newline, not an EOF
                    //In addition, append a space to the end of this so that no parameter scripts work fine
                    input = rawInput?.doublePadWindowsPaths()?.plus(" \n") ?: break
                }

                val lexer = PipelineLexer(CharStreams.fromString(input))
                debug("Tokens: \n{0}", lazyString {
                    lexer.allTokens.joinToString("\n") { token ->
                        if (token is CommonToken) token.toString(lexer)
                        else token.toString()
                    }
                })

                parseKnolusTransRule(
                    input,
                    KnolusTransVisitorRestrictions.Permissive,
                    ::PipelineLexer,
                    ::PipelineParser,
                    ::PipelineVisitor
                ) { parser, visitor -> visitor.visitScope(parser.scope()) }
                    .flatMap { (scope) -> (scope as KnolusUnion.ScopeType).run(GurrenPilot.globalContext) }
                    .doOnFailure {
                        val commandEntered = input.split('\n')
                            .dropLastWhile(String::isBlank)
                            .lastOrNull()
                            ?.substringBefore('(')
                            ?.trim()
                            ?.let(HelpDetails::sanitiseFunctionIdentifier)

                        if (commandEntered == null) {
                            printlnLocale("commands.unknown")
                            return@doOnFailure
                        }

                        val commandSimilarity =
                            GurrenPilot.helpCommands.values.distinct()
                                .mapNotNull { key -> Pair(key, GurrenPilot.helpFor(context, key)?.cmdKey?.commonPrefixWith(commandEntered, true)?.length ?: return@mapNotNull null) }
                                .maxByOrNull(Pair<String, Int>::second)

                        if (commandSimilarity == null) {
                            printlnLocale("commands.unknown")
                            return@doOnFailure
                        }

                        val help = GurrenPilot.helpFor(context, commandSimilarity.first)

                        when {
                            commandSimilarity.second == commandSimilarity.first.length ->
                                printlnLocale("commands.usage", help?.usage ?: commandSimilarity.first)
                            commandSimilarity.second > commandSimilarity.first.length / 3 ->
                                printlnLocale("commands.did_you_mean", help?.cmd ?: commandSimilarity.first)
                            else ->
                                printlnLocale("commands.unknown")
                        }
                    }.doOnThrown { withException ->
                        debug("ANTLR Parsing Error: ", withException.exception)
                    }

//                val matchingCommands = post(CommandRequest(readLine() ?: break, localScope)).foundCommands
//
//                if (matchingCommands.isEmpty())
//                    printlnLocale("commands.unknown")
            }
        }
    }
}