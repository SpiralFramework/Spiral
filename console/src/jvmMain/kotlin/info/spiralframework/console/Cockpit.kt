package info.spiralframework.console

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.binding.*
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.config.getConfigFile
import info.spiralframework.base.binding.DefaultSpiralEnvironment
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_FILE_NAME_KEY
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_SHA256_KEY
import info.spiralframework.base.common.events.SpiralEvent
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.events.SpiralEventListener
import info.spiralframework.base.common.events.SpiralEventPriority
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.base.common.text.ProgressTracker
import info.spiralframework.base.jvm.crypto.md5Hash
import info.spiralframework.base.jvm.crypto.verify
import info.spiralframework.base.jvm.io.files.relativePathFrom
import info.spiralframework.base.jvm.toFileSize
import info.spiralframework.console.data.*
import info.spiralframework.console.eventbus.*
import info.spiralframework.core.*
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY
import info.spiralframework.core.plugins.DefaultSpiralPluginRegistry
import info.spiralframework.core.plugins.SpiralPluginRegistry
import info.spiralframework.core.security.DefaultSpiralSignatures
import info.spiralframework.core.security.SpiralSignatures
import info.spiralframework.core.serialisation.DefaultSpiralSerialisation
import info.spiralframework.spiral.updater.jarLocationAsFile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.StandardOpenOption
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

/** The driving force behind the console interface for Spiral */
//<SELF : Cockpit<SELF>>
abstract class Cockpit @ExperimentalUnsignedTypes
internal constructor(var context: SpiralCockpitContext) {
    companion object {
        @ExperimentalTime
        @ExperimentalUnsignedTypes
        @Suppress("BlockingMethodInNonBlockingContext")
        @ExperimentalStdlibApi
        @JvmStatic
        fun main(args: Array<String>) = runBlocking<Unit> {
            val locale: SpiralLocale = DefaultSpiralLocale()
            val logger: SpiralLogger = DefaultSpiralLogger(LoggerFactory.getLogger(locale.localise("logger.parent.name")))
            val eventLogger = LoggerFactory.getLogger(locale.localise("logger.eventbus.name"))
            val config: SpiralConfig = DefaultSpiralConfig()
            val environment: SpiralEnvironment = DefaultSpiralEnvironment()
            val eventBus: SpiralEventBus = DefaultSpiralEventBus()
                    .installLoggingSubscriber()
            val cacheProvider: SpiralCacheProvider = DefaultSpiralCacheProvider()
            val parentContext: SpiralContext = DefaultSpiralContext(locale, logger, config, environment, eventBus, cacheProvider)

            val serialisation = DefaultSpiralSerialisation()

            val gurrenArgs: GurrenArgs
            if (GurrenArgs.disableConfigLoad(args)) {
                gurrenArgs = GurrenArgs(args)
            } else {
                val pojo = serialisation.yamlMapper.tryReadValue<GurrenArgs.Pojo>(File(config.getConfigFile(parentContext, "console")))
                gurrenArgs = pojo?.let { GurrenArgs(args, it) } ?: GurrenArgs(args)
            }
            val updateFile = File(Cockpit::class.java.jarLocationAsFile.absolutePath + ".update")

            val coreConfig: SpiralCoreConfig = serialisation.yamlMapper.tryReadValue(File(config.getConfigFile(parentContext, "core")))
                    ?: SpiralCoreConfig()
            val signatures: SpiralSignatures = DefaultSpiralSignatures()
            val pluginRegistry: SpiralPluginRegistry = DefaultSpiralPluginRegistry()
            val startingContext: SpiralCockpitContext = DefaultSpiralCockpitContext(
                    gurrenArgs,
                    coreConfig,
                    parentContext,
                    signatures,
                    pluginRegistry,
                    serialisation
            )

            var runUpdate: Boolean = false
            with(startingContext) {
                if (!updateFile.exists() && !gurrenArgs.disableUpdateCheck) {
                    val updateData = checkForUpdate(startingContext, "Console")

                    if (updateData != null) {
                        val (updateUrl, updateVersion) = updateData// ?: ("http://jenkins.abimon.org/view/Spiral/job/Spiral-Console/37/artifact/console/build/libs/spiral-console-shadow.jar" to 37)

                        val headResponse = Fuel.head(updateUrl).userAgent().response().takeResponseIfSuccessful() //.also(SpiralCoreData::printResponse)

                        if (headResponse != null) {
                            val jenkinsBuild = retrieveStaticValue(SPIRAL_ENV_BUILD_KEY)?.toIntOrNull()
                            printlnLocale("gurren.update.detected", "Spiral-Console", updateVersion, jenkinsBuild
                                    ?: constNull(), headResponse.contentLength.toFileSize())
                            printLocale("gurren.update.confirmation")

                            if (readConfirmation()) {
                                val signatureData = retrieveStaticValue(SPIRAL_FILE_NAME_KEY)?.let { fileName -> signatureForModule("Console", updateVersion.toString(), fileName) }

                                var shouldDownloadUnsigned = false
                                if (signatureData == null) {
                                    printlnLocale("gurren.update.unsigned.warning", retrieveStaticValue(SPIRAL_SHA256_KEY)
                                            ?: constNull())
                                    printLocale("gurren.update.unsigned.warning_confirmation")

                                    shouldDownloadUnsigned = readConfirmation(defaultToAffirmative = false)

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

                if (updateFile.exists() && updateFile.isFile) {
                    runUpdate = true

                    val build = buildForVersion(
                            this,
                            FileChannel.open(updateFile.toPath(), StandardOpenOption.READ).use(ReadableByteChannel::md5Hash)
                    )

                    if (build == null) {
                        //Unknown Build
                        printlnLocale("gurren.patch.unsigned.warning", retrieveStaticValue(SPIRAL_SHA256_KEY)
                                ?: constNull())
                        printLocale("gurren.patch.unsigned.warning_confirmation")

                        runUpdate = readConfirmation(defaultToAffirmative = false)

                        if (runUpdate) {
                            printlnLocale("gurren.patch.unsigned.approved_patch")
                        } else {
                            printlnLocale("gurren.patch.unsigned.denied_patch")
                        }
                    } else {
                        if (publicKey == null) {
                            if (spiralFrameworkOnline) {
                                //Online and key is down. Suspicious, but give the user a choice
                                //Don't delete the file

                                printlnLocale("gurren.patch.no_key.spiral_online.warning")
                                printLocale("gurren.patch.no_key.spiral_online.warning_confirmation")

                                runUpdate = readConfirmation(defaultToAffirmative = false)

                                if (runUpdate) {
                                    printlnLocale("gurren.patch.no_key.spiral_online.approved_patch")
                                } else {
                                    printlnLocale("gurren.patch.no_key.spiral_online.denied_patch")
                                }
                            } else if (signaturesCdnOnline) {
                                //Github's online, and our public key is null. Suspicious, but give the user a choice
                                //Don't delete the file

                                printlnLocale("gurren.patch.no_key.cdn_online.warning")
                                printLocale("gurren.patch.no_key.cdn_online.warning_confirmation")

                                runUpdate = readConfirmation(defaultToAffirmative = false)

                                if (runUpdate) {
                                    printlnLocale("gurren.patch.no_key.cdn_online.approved_patch")
                                } else {
                                    printlnLocale("gurren.patch.no_key.cdn_online.denied_patch")
                                }
                            } else {
                                //Both Github and I are down; unlikely, but possible.
                                //Give the user a choice, but tell them how to verify

                                printlnLocale("gurren.patch.no_key.offline.warning")
                                printLocale("gurren.patch.no_key.offline.warning_confirmation")

                                runUpdate = readConfirmation(defaultToAffirmative = false)

                                if (runUpdate) {
                                    printlnLocale("gurren.patch.no_key.offline.approved_patch")
                                } else {
                                    printlnLocale("gurren.patch.no_key.offline.denied_patch")
                                }
                            }
                        } else {
                            //Check Signature
                            val signatureData = signatureForModule("Console", build.toString(), requireNotNull(retrieveStaticValue(SPIRAL_FILE_NAME_KEY)))
                            if (signatureData == null) {
                                //Unsigned
                                printlnLocale("gurren.patch.unsigned.warning")
                                printLocale("gurren.patch.unsigned.warning_confirmation")

                                runUpdate = readConfirmation(defaultToAffirmative = false)

                                if (runUpdate) {
                                    printlnLocale("gurren.patch.unsigned.approved_patch")
                                } else {
                                    printlnLocale("gurren.patch.unsigned.denied_patch")
                                    updateFile.delete()
                                }
                            } else {
                                val isSigned = FileChannel.open(updateFile.toPath(), StandardOpenOption.READ).use { channel -> channel.verify(signatureData, requireNotNull(publicKey)) }
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
            }

            val instance: Cockpit

            if (runUpdate) {
                instance = CockpitUpdate(updateFile, startingContext, *args)
            } else {
                instance = invoke(startingContext)
            }

            instance.start()
            exitProcess(instance with { currentExitCode })
        }

        @ExperimentalUnsignedTypes
        @ExperimentalStdlibApi
        suspend operator fun invoke(args: Array<String>): Cockpit {
            val locale: SpiralLocale = DefaultSpiralLocale()
            val logger: SpiralLogger = DefaultSpiralLogger(LoggerFactory.getLogger(locale.localise("logger.parent.name")))
            val config: SpiralConfig = DefaultSpiralConfig()
            val environment: SpiralEnvironment = DefaultSpiralEnvironment()
            val eventBus: SpiralEventBus = DefaultSpiralEventBus()
                    .installLoggingSubscriber()
            val cacheProvider: SpiralCacheProvider = DefaultSpiralCacheProvider()
            val parentContext: SpiralContext = DefaultSpiralContext(locale, logger, config, environment, eventBus, cacheProvider)

            val serialisation = DefaultSpiralSerialisation()

            val gurrenArgs: GurrenArgs
            val coreConfig: SpiralCoreConfig
            if (GurrenArgs.disableConfigLoad(args)) {
                gurrenArgs = GurrenArgs(args)
                coreConfig = SpiralCoreConfig()
            } else {
                val pojo = serialisation.yamlMapper.tryReadValue<GurrenArgs.Pojo>(File(config.getConfigFile(parentContext, "console")))
                gurrenArgs = pojo?.let { GurrenArgs(args, it) } ?: GurrenArgs(args)
                coreConfig = serialisation.yamlMapper.tryReadValue(File(config.getConfigFile(parentContext, "core")))
                        ?: SpiralCoreConfig()
            }

            val signatures: SpiralSignatures = DefaultSpiralSignatures()
            val pluginRegistry: SpiralPluginRegistry = DefaultSpiralPluginRegistry()
            return invoke(DefaultSpiralCockpitContext(gurrenArgs, coreConfig, parentContext, signatures, pluginRegistry, serialisation))
        }

        operator fun invoke(context: SpiralCockpitContext): Cockpit {
            return if (context.args.isTool) {
                CockpitMechanic(context)
            } else {
                CockpitPilot(context)
            }
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

    val parameterParser: ParameterParser = ParameterParser(context)

    var currentExitCode: Int = 0

    /**
     * Signal to the cockpit that Spiral has finished initialising and to start up
     * This method will block the current thread. Use [start] if you to suspend it instead
     */
    fun startBlocking() = runBlocking { start() }

    abstract suspend fun start()

    fun registerCommandClass(commandRegistry: Any, registerSubclass: KClass<*> = ParboiledCommand::class) {
        commandRegistry.javaClass.kotlin.memberProperties.forEach { recruit ->
            if ((recruit.returnType.classifier as? KClass<*>)?.isSubclassOf(registerSubclass) == true || recruit.returnType.classifier == registerSubclass)
                context.register(recruit.get(commandRegistry) as SpiralEventListener<*>)
        }
    }

    init {
        context.register(object : SpiralEventListener<SpiralEvent> {
            override val eventClass: KClass<SpiralEvent> = SpiralEvent::class
            override val eventPriority: SpiralEventPriority = SpiralEventPriority.HIGHEST

            override suspend fun SpiralContext.handle(event: SpiralEvent) {
                if (event is RegisterCommandRequest && !event.cancelled) {
                    context.register(event.command)
                }

                if (event is UnregisterCommandRequest && !event.cancelled) {
                    context.deregister(event.command)
                }

                if (event is ScopeRequest && !event.cancelled) {
                    post(ScopeResponse(with { operationScope }))
                }
            }
        })
    }
}

//@Suppress("UNCHECKED_CAST")
//suspend operator fun <SELF: Cockpit, T> SELF.invoke(op: suspend SELF.() -> T): T = mutex.withLock { this.op() }

@Suppress("UNCHECKED_CAST")
infix fun <SELF : Cockpit, T> SELF.withBlocking(op: suspend SELF.() -> T): T = runBlocking { mutex.withLock { this@withBlocking.op() } }

@Suppress("UNCHECKED_CAST")
suspend infix fun <SELF : Cockpit, T> SELF.with(op: suspend SELF.() -> T): T = mutex.withLock { this.op() }