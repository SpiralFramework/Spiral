package info.spiralframework.console

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.LocaleLogger
import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.config.SpiralConfig
import info.spiralframework.base.util.*
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.console.data.ParameterParser
import info.spiralframework.console.data.SpiralScope
import info.spiralframework.console.eventbus.CockpitInitialisedEvent
import info.spiralframework.console.eventbus.ParboiledCommand
import info.spiralframework.console.eventbus.RegisterCommandRequest
import info.spiralframework.core.*
import info.spiralframework.core.eventbus.EventBusBridgeLogger
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.spiral.updater.jarLocationAsFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.StandardOpenOption
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

/** The driving force behind the console interface for Spiral */
abstract class Cockpit<SELF: Cockpit<SELF>> internal constructor(val args: GurrenArgs) {
    companion object {
        /** The logger for Spiral */
        val LOGGER: Logger

        val stopwatch = Stopwatch()

        @JvmStatic
        fun main(args: Array<String>) {
            val gurrenArgs: GurrenArgs
            if (GurrenArgs.disableConfigLoad(args)) {
                gurrenArgs = GurrenArgs(args)
            } else {
                val pojo = SpiralSerialisation.YAML_MAPPER.tryReadValue<GurrenArgs.Pojo>(SpiralConfig.getConfigFile("cockpit"))
                gurrenArgs = pojo?.let { GurrenArgs(args, it) } ?: GurrenArgs(args)
            }
            val updateFile = File(Cockpit::class.java.jarLocationAsFile.absolutePath + ".update")

            if (!updateFile.exists() && !gurrenArgs.disableUpdateCheck) {
                val updateData = SpiralCoreData.checkForUpdate("Console")

                if (updateData != null) {
                    val (updateUrl, updateVersion) = updateData// ?: ("http://jenkins.abimon.org/view/Spiral/job/Spiral-Console/37/artifact/console/build/libs/spiral-console-shadow.jar" to 37)

                    val headResponse = Fuel.head(updateUrl).userAgent().response().also(SpiralCoreData::printResponse).takeResponseIfSuccessful()

                    if (headResponse != null) {
                        printlnLocale("gurren.update.detected", "Spiral-Console", updateVersion, SpiralCoreData.jenkinsBuild, headResponse.contentLength.toFileSize())
                        printLocale("gurren.update.confirmation")

                        if (SpiralLocale.readConfirmation()) {
                            val signatureData = SpiralSignatures.signatureForModule("Console", updateVersion.toString(), SpiralCoreData.fileName!!)

                            var shouldDownloadUnsigned = false
                            if (signatureData == null) {
                                printlnLocale("gurren.update.unsigned.warning", SpiralCoreData.sha256Hash)
                                printLocale("gurren.update.unsigned.warning_confirmation")

                                shouldDownloadUnsigned = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                                if (shouldDownloadUnsigned) {
                                    printlnLocale("gurren.update.unsigned.approved_download")
                                } else {
                                    printlnLocale("gurren.update.unsigned.denied_download")
                                }
                            }

                            if (signatureData != null || shouldDownloadUnsigned) {
                                val (_, response) = ProgressTracker(downloadingText = "gurren.update.downloading", downloadedText = "") {
                                    Fuel.download(updateUrl).fileDestination { _, _ -> updateFile }
                                            .progress(this::trackDownload)
                                            .userAgent().response()
                                }

                                if (response.isSuccessful) {
                                    printlnLocale("gurren.update.downloaded")
                                } else {
                                    printlnLocale("gurren.update.download_failed", response.statusCode, response.responseMessage)
                                    updateFile.delete()
                                }
                            }
                        }
                    }

//                    Files.copy(Paths.get(URI(this::class.java.jarLocation.toURI().toString())), updateFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
//                    installUpdate(updateFile.absolutePath, "-u", *args)
//                    return
                }
            }

            var runUpdate: Boolean = false

            if (updateFile.exists() && updateFile.isFile) {
                runUpdate = true

                val build = SpiralCoreData.buildForVersion(
                        FileChannel.open(updateFile.toPath(), StandardOpenOption.READ).use(ReadableByteChannel::md5Hash)
                )

                if (build == null) {
                    //Unknown Build
                    printlnLocale("gurren.patch.unsigned.warning", SpiralCoreData.sha256Hash)
                    printLocale("gurren.patch.unsigned.warning_confirmation")

                    runUpdate = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                    if (runUpdate) {
                        printlnLocale("gurren.patch.unsigned.approved_patch")
                    } else {
                        printlnLocale("gurren.patch.unsigned.denied_patch")
                    }
                } else {
                    if (SpiralSignatures.PUBLIC_KEY == null) {
                        if (SpiralSignatures.spiralFrameworkOnline) {
                            //Online and key is down. Suspicious, but give the user a choice
                            //Don't delete the file

                            printlnLocale("gurren.patch.no_key.spiral_online.warning")
                            printLocale("gurren.patch.no_key.spiral_online.warning_confirmation")

                            runUpdate = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                            if (runUpdate) {
                                printlnLocale("gurren.patch.no_key.spiral_online.approved_patch")
                            } else {
                                printlnLocale("gurren.patch.no_key.spiral_online.denied_patch")
                            }
                        } else if (SpiralSignatures.githubOnline) {
                            //Github's online, and our public key is null. Suspicious, but give the user a choice
                            //Don't delete the file

                            printlnLocale("gurren.patch.no_key.github_online.warning")
                            printLocale("gurren.patch.no_key.github_online.warning_confirmation")

                            runUpdate = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                            if (runUpdate) {
                                printlnLocale("gurren.patch.no_key.github_online.approved_patch")
                            } else {
                                printlnLocale("gurren.patch.no_key.github_online.denied_patch")
                            }
                        } else {
                            //Both Github and I are down; unlikely, but possible.
                            //Give the user a choice, but tell them how to verify

                            printlnLocale("gurren.patch.no_key.offline.warning")
                            printLocale("gurren.patch.no_key.offline.warning_confirmation")

                            runUpdate = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                            if (runUpdate) {
                                printlnLocale("gurren.patch.no_key.offline.approved_patch")
                            } else {
                                printlnLocale("gurren.patch.no_key.offline.denied_patch")
                            }
                        }
                    } else {
                        //Check Signature
                        val signatureData = SpiralSignatures.signatureForModule("Console", build.toString(), SpiralCoreData.fileName!!)
                        if (signatureData == null) {
                            //Unsigned
                            printlnLocale("gurren.patch.unsigned.warning")
                            printLocale("gurren.patch.unsigned.warning_confirmation")

                            runUpdate = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                            if (runUpdate) {
                                printlnLocale("gurren.patch.unsigned.approved_patch")
                            } else {
                                printlnLocale("gurren.patch.unsigned.denied_patch")
                                updateFile.delete()
                            }
                        } else {
                            val isSigned = FileChannel.open(updateFile.toPath(), StandardOpenOption.READ).use { channel -> channel.verify(signatureData, SpiralSignatures.PUBLIC_KEY!!) }
                            if (isSigned) {
                                runUpdate = true
                            } else {
                                printlnLocale("gurren.patch.invalid_signature.error")
                                runUpdate = false
                                updateFile.delete()
                            }
                        }
                    }
                }
            }

            val instance: Cockpit<*>

            if (runUpdate) {
                instance = CockpitUpdate(updateFile, gurrenArgs, *args)
            } else {
                instance = invoke(gurrenArgs)
            }

            instance.start()
            System.exit(instance with { currentExitCode })
        }

        operator fun invoke(args: Array<String>): Cockpit<*> {
            val gurrenArgs: GurrenArgs
            if (GurrenArgs.disableConfigLoad(args)) {
                gurrenArgs = GurrenArgs(args)
            } else {
                val pojo = SpiralSerialisation.YAML_MAPPER.tryReadValue<GurrenArgs.Pojo>(SpiralConfig.getConfigFile("cockpit"))
                gurrenArgs = pojo?.let { GurrenArgs(args, it) } ?: GurrenArgs(args)
            }

            return invoke(gurrenArgs)
        }

        operator fun invoke(args: GurrenArgs): Cockpit<*> {
            return if (args.isTool) {
                CockpitMechanic(args)
            } else {
                CockpitPilot(args)
            }
        }

        init {
            SpiralLocale.addBundle("SpiralConsole")
            SpiralLocale.addBundle("SpiralConsole-Mechanic")
            SpiralLocale.addBundle("SpiralConsole-Pilot")

            DataHandler.byteArrayToMap = { byteArray -> SpiralSerialisation.JSON_MAPPER.readValue(byteArray, Map::class.java).mapKeys { (key) -> key.toString() } }
            DataHandler.stringToMap = { string -> SpiralSerialisation.JSON_MAPPER.readValue(string, Map::class.java).mapKeys { (key) -> key.toString() } }
            DataHandler.fileToMap = { file -> SpiralSerialisation.JSON_MAPPER.readValue(file, Map::class.java).mapKeys { (key) -> key.toString() } }
            DataHandler.streamToMap = { stream -> SpiralSerialisation.JSON_MAPPER.readValue(stream, Map::class.java).mapKeys { (key) -> key.toString() } }

            SpiralCoreData.mainModule = "spiral-console"

            LOGGER = LocaleLogger(LoggerFactory.getLogger(locale<String>("logger.commands.name", SpiralCoreData.version ?: "Developer")))
            EventBus.getDefault()
                    .installLoggingSubscriber(LOGGER)
        }
    }

    val runningJar = File(Cockpit::class.java.protectionDomain.codeSource.location.toURI())
    val runningDirectory = File(System.getProperty("user.dir")).absoluteFile!!
    val relativeRunningJar = runningJar relativePathFrom runningDirectory

    /** The mutex to use to access this classes properties */
    val mutex = Mutex()

    /**
     * The scope of operation that Spiral is currently operating in
     */
    var operationScope: SpiralScope = SpiralScope("default", "> ")

    val parameterParser: ParameterParser = ParameterParser()
    val bus: EventBus = EventBus.builder()
            .eventInheritance(false)
            .logger(EventBusBridgeLogger(LOGGER))
            .build()
            .installLoggingSubscriber(LOGGER)

    var currentExitCode: Int = 0

    /**
     * Signal to the cockpit that Spiral has finished initialising and to start up
     * This method will block the current thread. Use [startAsync] if you want finer control over the job that's launched
     */
    fun start() = runBlocking { startAsync().join() }

    abstract fun startAsync(scope: CoroutineScope = GlobalScope): Job

    @Suppress("UNCHECKED_CAST")
    suspend operator fun <T> invoke(op: suspend SELF.() -> T): T = mutex.withLock { (this@Cockpit as SELF).op() }
    @Suppress("UNCHECKED_CAST")
    infix fun <T> with(op: suspend SELF.() -> T): T = runBlocking { mutex.withLock { (this@Cockpit as SELF).op() } }
    @Suppress("UNCHECKED_CAST")
    suspend infix fun <T> withAsync(op: suspend SELF.() -> T): T = mutex.withLock { (this@Cockpit as SELF).op() }

    fun registerCommandClass(commandRegistry: Any, registerSubclass: KClass<*> = ParboiledCommand::class) {
        commandRegistry.javaClass.kotlin.memberProperties.forEach { recruit ->
            if((recruit.returnType.classifier as? KClass<*>)?.isSubclassOf(registerSubclass) == true || recruit.returnType.classifier == registerSubclass)
                bus.register(recruit.get(commandRegistry))
        }
    }

    init {
        @Suppress("UNCHECKED_CAST", "LeakingThis")
        EventBus.getDefault().post(CockpitInitialisedEvent(this as SELF))
        EventBus.getDefault().register(object {
            @Subscribe(priority = Int.MIN_VALUE)
            fun onEvent(event: Any) {
                if (event is RegisterCommandRequest && !event.isCanceled) {
                    bus.register(event.command)
                }
            }
        })
    }
}