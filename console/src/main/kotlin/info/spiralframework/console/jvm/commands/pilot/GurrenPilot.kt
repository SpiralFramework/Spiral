package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.stringTypeParameter
import dev.brella.kornea.errors.common.*
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.localiseOrNull
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.data.HelpDetails
import info.spiralframework.console.jvm.commands.shared.GurrenShared
//import info.spiralframework.console.jvm.data.SrdiMesh
//import info.spiralframework.console.jvm.data.collada.*
import info.spiralframework.console.jvm.pipeline.registerFunctionWithAliasesWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.formats.common.games.DrGame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.ExperimentalUnsignedTypes
import kotlin.Pair
import kotlin.String
import kotlin.arrayOf
import kotlin.with

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
object GurrenPilot : CommandRegistrar {
    val COMMAND_NAME_REGEX = "[\\s_-]+".toRegex()

    /** Helper Variables */
    var keepLooping = AtomicBoolean(true)
    var game: DrGame? = null

    val PERCENT_FORMAT = DecimalFormat("00.00")

    val helpCommands: MutableMap<String, String> = HashMap()
    val submodules = arrayOf(
        GurrenExtractFilesPilot, GurrenExtractTexturesPilot, GurrenExtractModelsPilot,
        GurrenIdentifyPilot
    )

    private val helpMutex: Mutex = Mutex()
    private val helpDetails: MutableMap<CommonLocale?, MutableMap<String, HelpDetails>> = HashMap()

    inline fun help(vararg commands: String) =
        commands.forEach { helpCommands[it.replace(COMMAND_NAME_REGEX, "").toUpperCase()] = it }

    inline fun help(vararg commands: Pair<String, String>) =
        commands.forEach { (a, b) -> helpCommands[a.replace(COMMAND_NAME_REGEX, "").toUpperCase()] = b }

    inline fun help(command: Pair<String, List<String>>) =
        command.second.forEach { a -> helpCommands[a.replace(COMMAND_NAME_REGEX, "").toUpperCase()] = command.first }

    suspend fun helpFor(context: SpiralContext, command: String): HelpDetails? = helpMutex.withLock {
        helpDetails
            .computeIfAbsent(context.currentLocale()) { HashMap() }
            .compute(command) { key, previous -> previous?.copyWithUpdate(context) ?: HelpDetails(context, key) }
    }

    override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
        with(knolusContext) {
            registerFunctionWithAliasesWithContextWithoutReturn(functionNames = arrayOf("help", "help_with"), stringTypeParameter("command").asOptional()) { context, cmdNameParam ->
                context.spiralContext().doOnSuccess { spiralContext ->
                    cmdNameParam.doOnSuccess { cmdName ->
                        val cmdPrompt = helpCommands[cmdName.replace(COMMAND_NAME_REGEX, "").toUpperCase()]

                        if (cmdPrompt == null) {
                            spiralContext.printlnLocale("commands.pilot.help.err_not_found", cmdName)
                        } else {
                            helpFor(spiralContext, cmdPrompt)?.let { (key, name, _, desc, usage) ->
                                spiralContext.printlnLocale(
                                    "commands.pilot.help.for_command",
                                    name ?: spiralContext.localise("help.not_defined.name", key),
                                    desc ?: spiralContext.localise("help.not_defined.desc", key),
                                    usage ?: spiralContext.localise("help.not_defined.usage", key)
                                )
                            }
                        }
                    }.doOnEmpty {
                        println(buildString {
                            appendLine(spiralContext.localise("commands.pilot.help.header"))
                            appendLine()
                            helpCommands.values.distinct()
                                .mapNotNull { key ->
                                    helpFor(spiralContext, key)?.let { (key, name, blurb) ->
                                        Pair(name ?: spiralContext.localise("help.not_defined.name", key), blurb ?: spiralContext.localise("help.not_defined.blurb", key))
                                    }
                                }.sortedBy(Pair<String, String>::first)
                                .forEach { (name, blurb) ->
                                    append("> ")
                                    append(name)
                                    append(" - ")
                                    appendLine(blurb)
                                }
                        })
                    }
                }
            }

            registerFunctionWithAliasesWithContextWithoutReturn("show_environment", "show_env") { context ->
                context.spiralContext().doOnSuccessAsync(GurrenShared::showEnvironment)
            }

            registerFunctionWithAliasesWithContextWithoutReturn("exit", "quit") { context ->
                context.spiralContext().doOnSuccess { spiral -> spiral.printlnLocale("Goodbye !") }

                keepLooping.set(false)
            }

            submodules.forEach { module -> module.register(spiralContext, knolusContext) }
        }
    }
}
