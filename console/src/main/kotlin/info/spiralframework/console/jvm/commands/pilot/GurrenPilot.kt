package info.spiralframework.console.jvm.commands.pilot

//import info.spiralframework.console.jvm.data.SrdiMesh
//import info.spiralframework.console.jvm.data.collada.*
import dev.brella.knolus.KnolusUnion
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.context.KnolusGlobalContext
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.modules.functionregistry.registerFunctionWithoutReturn
import dev.brella.knolus.modules.functionregistry.registerMemberFunctionWithContext
import dev.brella.knolus.objectTypeAsStringParameter
import dev.brella.knolus.objectTypeParameter
import dev.brella.knolus.restrictions.CompoundKnolusRestriction
import dev.brella.knolus.restrictions.KnolusRecursiveRestriction
import dev.brella.knolus.run
import dev.brella.knolus.stringTypeParameter
import dev.brella.knolus.transform.KnolusTransVisitorRestrictions
import dev.brella.knolus.transform.parseKnolusTransRule
import dev.brella.knolus.types.KnolusString
import dev.brella.knolus.types.asString
import dev.brella.knolus.types.asType
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.toolkit.common.mapToArray
import info.spiralframework.antlr.pipeline.PipelineLexer
import info.spiralframework.antlr.pipeline.PipelineParser
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.logging.error
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.text.doublePadWindowsPaths
import info.spiralframework.base.jvm.select
import info.spiralframework.console.jvm.commands.data.HelpDetails
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.pipeline.KnolusTypedWrapper
import info.spiralframework.console.jvm.pipeline.PipelineVisitor
import info.spiralframework.console.jvm.pipeline.registerFunctionWithAliasesWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.console.jvm.pipeline.wrap
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import org.antlr.v4.runtime.CharStreams
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.text.DecimalFormat
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.PatternSyntaxException
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
object GurrenPilot: CoroutineScope {
    val COMMAND_NAME_REGEX = "[\\s_-]+".toRegex()

    /** Helper Variables */
    var keepLooping = AtomicBoolean(true)
    var formatContext: SpiralProperties = SpiralProperties()

    val debuggingCounter = AtomicInteger(0)
    val debuggingThread = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "Debugging-${debuggingCounter.incrementAndGet()}") }
        .asCoroutineDispatcher()

    override val coroutineContext: CoroutineContext = SupervisorJob() + debuggingThread

    val globalContext = KnolusGlobalContext(null, CompoundKnolusRestriction.fromPermissive(KnolusRecursiveRestriction(maxDepth = 20, maxRecursiveCount = 30)))

    suspend fun handle(context: KnolusContext, value: Any): KorneaResult<Any?> =
        when (value) {
            is KnolusUnion.Action<*> -> value.run(context)
            is KnolusUnion.VariableValue<*> -> handle(context, value.value)
            is KnolusUnion.ReturnStatement -> KorneaResult.success(value.value)
            else -> KorneaResult.empty()
        }


    val PERCENT_FORMAT = DecimalFormat("00.00")

    val helpCommands: MutableMap<String, String> = HashMap()
    val submodules = arrayOf(
        GurrenExtractFilesPilot, GurrenExtractTexturesPilot, GurrenExtractModelsPilot,
        GurrenIdentifyPilot, GurrenConvertPilot,

        GurrenWatchtower
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

    suspend fun register(spiralContext: SpiralContext) {
        with(globalContext) {
            registerMemberFunctionWithContext(objectTypeParameter(), "toString") { context, obj ->
                obj.asString(context).getOrNull()?.let(::KnolusString) ?: KnolusString(obj.toString())
            }

            registerFunctionWithoutReturn("println", objectTypeAsStringParameter(), ::println)

//            registerFunctionWithContextWithoutReturn("running") { context ->
//
//            }

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

            registerFunctionWithAliasesWithContextWithoutReturn("show_properties", "show_prop") { context ->
                val string = formatContext.entries()
                    .takeIf(Set<*>::isNotEmpty)
                    ?.joinToString("\n", prefix = "Properties: \n") { (k, v) -> "\t${k.name}: $v" }

                if (string == null) {
                    println("No properties currently defined!")
                } else {
                    println(string)
                }
            }

            registerFunctionWithAliasesWithContextWithoutReturn(functionNames = arrayOf("set", "set_property", "set_prop"), stringTypeParameter("property").asOptional()) { context, chosenProperty ->
                val spiralContext = context.spiralContext().getOrBreak { return@registerFunctionWithAliasesWithContextWithoutReturn }
                val availableProperties = spiralContext.availableProperties.filter { it.key.isPersistent }

                val property = chosenProperty.map { input ->
                    availableProperties.groupBy { prop ->
                        if (prop.aliases.isNotEmpty())
                            prop.aliases.mapTo(mutableListOf(prop.name.commonPrefixWith(input, true).length)) { alias ->
                                alias.commonPrefixWith(input, true).length
                            }.maxOrNull()!!
                        else
                            prop.name.commonPrefixWith(input, true).length
                    }
                        .entries
                        .maxByOrNull(Map.Entry<Int, *>::key)
                        ?.let { (_, v) -> v.firstOrNull { prop -> prop.name.length == input.length || prop.aliases.any { alias -> alias.length == input.length } } }
                }.getOrNull()

                if (property == null) {
                    chosenProperty.doOnSuccess { input ->
                        println("Sorry, '$input' is a little ambiguous, try again maybe?")
                    }
                } else {
                    property.fillIn(spiralContext, formatContext, null).doOnSuccess { formatContext = it }
                    return@registerFunctionWithAliasesWithContextWithoutReturn
                }

                select("Please select a property: ", availableProperties.mapToArray(ISpiralProperty<*>::name), HashMap<String, ISpiralProperty<*>>().apply {
                    availableProperties.forEach { prop ->
                        this[prop.name] = prop
                        prop.aliases.associateWithTo(this) { prop }
                    }
                }).flatMap { prop -> prop.fillIn(spiralContext, formatContext, null) }
                    .doOnSuccess { formatContext = it }
            }

            registerFunctionWithAliasesWithContextWithoutReturn("exit", "quit") { context ->
                context.spiralContext().doOnSuccess { spiral -> spiral.printlnLocale("Goodbye !") }

                keepLooping.set(false)
            }

            submodules.forEach { module -> module.register(spiralContext, globalContext) }
        }
    }
}
