package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.booleanTypeParameter
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.flagTypeParameter
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.objectTypeParameter
import dev.brella.knolus.stringTypeParameter
import dev.brella.knolus.toFormattedBoolean
import dev.brella.knolus.types.KnolusArray
import dev.brella.knolus.types.KnolusString
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.knolus.types.asString
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.img.DXT1PixelData
import dev.brella.kornea.img.bc7.BC7PixelData
import dev.brella.kornea.img.createPngImage
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.BinaryInputFlow
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.SeekableInputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.readFloatLE
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.readAndClose
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useInputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.toolkit.common.useAndFlatMap
import dev.brella.kornea.toolkit.common.useAndMap
import dev.brella.kornea.toolkit.coroutines.ascii.progressBar
import info.spiralframework.base.binding.prompt
import info.spiralframework.base.binding.readConfirmation
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.printLocale
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.logging.error
import info.spiralframework.base.common.text.doublePadWindowsPaths
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.data.SrdiMesh
import info.spiralframework.console.jvm.data.collada.*
import info.spiralframework.console.jvm.pipeline.registerFunctionWithAliasesWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.registerFunctionWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.serialisation.SpiralSerialisation
import info.spiralframework.formats.common.archives.SpcArchive
import info.spiralframework.formats.common.archives.SpiralArchive
import info.spiralframework.formats.common.archives.openDecompressedSource
import info.spiralframework.formats.common.archives.srd.MaterialsSrdEntry
import info.spiralframework.formats.common.archives.srd.MeshSrdEntry
import info.spiralframework.formats.common.archives.srd.SrdArchive
import info.spiralframework.formats.common.archives.srd.TRESrdEntry
import info.spiralframework.formats.common.archives.srd.TXISrdEntry
import info.spiralframework.formats.common.archives.srd.TextureSrdEntry
import info.spiralframework.formats.common.archives.srd.VTXSrdEntry
import info.spiralframework.formats.common.archives.srd.traverse
import info.spiralframework.formats.common.games.DrGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import kotlin.Any
import kotlin.Boolean
import kotlin.ExperimentalUnsignedTypes
import kotlin.Int
import kotlin.IntArray
import kotlin.OptIn
import kotlin.Pair
import kotlin.String
import kotlin.Throwable
import kotlin.apply
import kotlin.arrayOf
import kotlin.let
import kotlin.repeat
import kotlin.requireNotNull
import kotlin.takeIf
import kotlin.time.ExperimentalTime
import kotlin.toList
import kotlin.toULong
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

    inline fun help(vararg commands: String) =
        commands.forEach { helpCommands[it.replace(COMMAND_NAME_REGEX, "").toUpperCase()] = it }

    inline fun help(vararg commands: Pair<String, String>) =
        commands.forEach { (a, b) -> helpCommands[a.replace(COMMAND_NAME_REGEX, "").toUpperCase()] = b }


    override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
        with(knolusContext) {
            registerFunctionWithAliasesWithContextWithoutReturn(functionNames = arrayOf("help", "help_with"), stringTypeParameter("command").asOptional()) { context, cmdNameParam ->
                context.spiralContext().doOnSuccess { spiralContext ->
                    cmdNameParam.doOnSuccess { cmdName ->
                        val cmdPrompt = helpCommands[cmdName.replace(COMMAND_NAME_REGEX, "").toUpperCase()]

                        if (cmdPrompt == null) {
                            spiralContext.printlnLocale("commands.pilot.help.err_not_found", cmdName)
                        } else {
                            spiralContext.printlnLocale("commands.pilot.help.for_command", spiralContext.localise("help.$cmdPrompt.name"), spiralContext.localise("help.$cmdPrompt.desc"), spiralContext.localise("help.$cmdPrompt.usage"))
                        }
                    }.doOnEmpty {
                        println(buildString {
                            appendLine(spiralContext.localise("commands.pilot.help.header"))
                            appendLine()
                            helpCommands.values.distinct()
                                .map { key -> Pair(spiralContext.localise("help.$key.name"), spiralContext.localise("help.$key.blurb")) }
                                .sortedBy(Pair<String, String>::first)
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
