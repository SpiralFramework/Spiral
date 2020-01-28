package info.spiralframework.core.plugins

import com.github.kittinunf.fuel.Fuel
import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.config.SpiralConfig
import info.spiralframework.base.util.*
import info.spiralframework.core.*
import info.spiralframework.core.SpiralCoreData.printResponse
import info.spiralframework.core.plugins.events.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLClassLoader
import java.security.SecureRandom
import java.util.*
import java.util.zip.ZipFile
import kotlin.collections.ArrayList
import kotlin.experimental.xor
import kotlin.reflect.full.createInstance

object PluginRegistry : SpiralCoreConfigAccessor {
    interface PojoProvider {
        val pojo: SpiralPluginDefinitionPojo
    }

    enum class LoadPluginResult(val success: Boolean) {
        SUCCESS(true),
        ALREADY_LOADED(false),
        LOADED_ON_CLASSPATH(false),
        PLUGIN_LOAD_CANCELLED(false),
        NO_PLUGIN_CLASS_CONSTRUCTOR(false),
        PLUGIN_CLASS_NOT_SPIRAL_PLUGIN(false),
        MODULE_NOT_SUPPORTED(false),
        REQUIRED_MODULE_NOT_LOADED(false);
    }

    val pojoServiceLoader = ServiceLoader.load(PojoProvider::class.java)

    private val mutableLoadedPlugins: MutableList<ISpiralPlugin> = ArrayList()
    val loadedPlugins: List<ISpiralPlugin>
        get() = mutableLoadedPlugins

    private val pluginLoaders: MutableMap<String, URLClassLoader> = HashMap()

    fun discover(): List<PluginEntry> {
        if (EventBus.getDefault().postCancellable(BeginPluginDiscoveryEvent()))
            return emptyList()

        //We scan five locations
        //1. Local working folder called 'plugins'
        //2. Folder where Spiral is called 'plugins'
        //3. Plugin storage folder as defined by our project directory thing
        //4. (Optionally) a user defined path
        //5. ServiceLoader

        val ourFile = File(PluginRegistry::class.java.protectionDomain.codeSource.location.path).absoluteFile

        val localWorkingPlugins = File("plugins")
                .ensureDirectoryExists()
                .walk()
                .discoverPlugins()
        val localSpiralPlugins = File(ourFile.parentFile, "plugins")
                .ensureDirectoryExists()
                .walk()
                .discoverPlugins()
        val storagePlugins = File(SpiralConfig.projectDirectories.dataLocalDir, "plugins")
                .ensureDirectoryExists()
                .walk()
                .discoverPlugins()

        val classpathPlugins = pojoServiceLoader.asIterable().map(PojoProvider::pojo)
                .map { pojo -> PluginEntry(pojo, null) }

        val plugins = ArrayList<PluginEntry>().apply {
            addAll(localWorkingPlugins)
            addAll(localSpiralPlugins)
            addAll(storagePlugins)
            addAll(classpathPlugins)
        }.sortedBy { entry -> entry.pojo.semanticVersion }
                .asReversed()
                .distinctBy { entry -> entry.pojo.uid }
                .filter { entry -> !EventBus.getDefault().postCancellable(DiscoveredPluginEvent(entry)) }

        EventBus.getDefault().post(EndPluginDiscoveryEvent())

        return plugins
    }

    fun Sequence<File>.discoverPlugins(): List<PluginEntry> = this.flatMap { file ->
        try {
            val zip = ZipFile(file)
            zip.entries().asSequence().filter { entry -> entry.name.endsWith("plugin.yaml") || entry.name.endsWith("plugin.yml") || entry.name.endsWith("plugin.json')") }
                    .mapNotNull { entry ->
                        if (entry.name.endsWith("json"))
                            zip.getInputStream(entry).use { stream -> SpiralSerialisation.JSON_MAPPER.tryReadValue<SpiralPluginDefinitionPojo>(stream) }
                        else
                            zip.getInputStream(entry).use { stream -> SpiralSerialisation.YAML_MAPPER.tryReadValue<SpiralPluginDefinitionPojo>(stream) }
                    }
                    .map { pojo -> PluginEntry(pojo, file.toURI().toURL()) }
        } catch (io: IOException) {
            return@flatMap emptySequence<PluginEntry>()
        }
    }.toList()


    fun loadPlugin(pluginEntry: PluginEntry): LoadPluginResult {
        //First thing's first, check to make sure the plugin hasn't already been loaded

        if (loadedPlugins.any { plugin -> plugin.uid == pluginEntry.pojo.uid }) {
            val sameUID = loadedPlugins.first { plugin -> plugin.uid == pluginEntry.pojo.uid }
            //Same UID, uho
            //Check if we're trying to load a different version

            if (sameUID.version == pluginEntry.pojo.semanticVersion) {
                return LoadPluginResult.ALREADY_LOADED
            } else {
                //Unload existing plugin
                unloadPlugin(sameUID)
            }
        }

        if (pluginEntry.pojo.uid in pluginLoaders) {
            return LoadPluginResult.LOADED_ON_CLASSPATH
        }

        if (pluginEntry.pojo.requiredModules?.any { str -> str !in SpiralCoreData.loadedModules } == true) {
            return LoadPluginResult.REQUIRED_MODULE_NOT_LOADED
        }

        if (pluginEntry.pojo.supportedModules?.none { str -> str == SpiralCoreData.mainModule } == true) {
            return LoadPluginResult.MODULE_NOT_SUPPORTED
        }

        if (EventBus.getDefault().postCancellable(BeginLoadingPluginEvent(pluginEntry))) {
            return LoadPluginResult.PLUGIN_LOAD_CANCELLED
        }

        val result = loadPluginInternal(pluginEntry)

        if (result.success) {
            EventBus.getDefault().post(SuccessfulPluginLoadEvent(pluginEntry, result))
        } else {
            EventBus.getDefault().post(FailedPluginLoadEvent(pluginEntry, result))
            unloadPluginInternal(pluginEntry.pojo.uid)
        }

        return result
    }

    private fun loadPluginInternal(pluginEntry: PluginEntry): LoadPluginResult {
        val classLoader: ClassLoader
        if (pluginEntry.source != null && pluginLoaders.values.none { loader -> loader.urLs.any { url -> url == pluginEntry.source } }) {
            classLoader = URLClassLoader.newInstance(arrayOf(pluginEntry.source))
            pluginLoaders[pluginEntry.pojo.uid] = classLoader
        } else {
            classLoader = this::class.java.classLoader
        }

        val pluginKlass = classLoader.loadClass(pluginEntry.pojo.pluginClass).kotlin
        val plugin = (pluginKlass.objectInstance
                ?: runCatching { pluginKlass.createInstance() }.getOrDefault(null)
                ?: return LoadPluginResult.NO_PLUGIN_CLASS_CONSTRUCTOR) as? ISpiralPlugin
                ?: return LoadPluginResult.PLUGIN_CLASS_NOT_SPIRAL_PLUGIN

        mutableLoadedPlugins.add(plugin)
        plugin.load()

        return LoadPluginResult.SUCCESS
    }

    fun unloadPlugin(plugin: ISpiralPlugin) {
        plugin.unload()
        mutableLoadedPlugins.remove(plugin)
        unloadPluginInternal(plugin.uid)
    }

    private fun unloadPluginInternal(uid: String) {
        pluginLoaders.remove(uid)?.close()
    }

    //Checks
    fun queryEnablePlugin(plugin: PluginEntry): Boolean {
        var loadPlugin = false

        val publicKey = SpiralSignatures.PUBLIC_KEY
        if (publicKey == null) {
            if (SpiralSignatures.spiralFrameworkOnline) {
                //Online and key is down. Suspicious, but give the user a choice
                //Don't delete the file

                printlnLocale("core.plugins.enable.no_key.spiral_online.warning")
                printLocale("core.plugins.enable.no_key.spiral_online.warning_confirmation")

                loadPlugin = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                if (loadPlugin) {
                    printlnLocale("core.plugins.enable.no_key.spiral_online.approved_plugin")
                } else {
                    printlnLocale("core.plugins.enable.no_key.spiral_online.denied_plugin")
                }
            } else if (SpiralSignatures.githubOnline) {
                //Github's online, and our public key is null. Suspicious, but give the user a choice
                //Don't delete the file

                printlnLocale("core.plugins.enable.no_key.github_online.warning")
                printLocale("core.plugins.enable.no_key.github_online.warning_confirmation")

                loadPlugin = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                if (loadPlugin) {
                    printlnLocale("core.plugins.enable.no_key.github_online.approved_plugin")
                } else {
                    printlnLocale("core.plugins.enable.no_key.github_online.denied_plugin")
                }
            } else {
                //Both Github and I are down; unlikely, but possible.
                //Give the user a choice, but tell them how to verify

                printlnLocale("core.plugins.enable.no_key.offline.warning")
                printLocale("core.plugins.enable.no_key.offline.warning_confirmation")

                loadPlugin = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                if (loadPlugin) {
                    printlnLocale("core.plugins.enable.no_key.offline.approved_plugin")
                } else {
                    printlnLocale("core.plugins.enable.no_key.offline.denied_plugin")
                }
            }
        } else {
            val signature = SpiralSignatures.signatureForPlugin(plugin.pojo.uid, plugin.pojo.semanticVersion.toString(), plugin.pojo.pluginFileName
                    ?: plugin.source!!.path.substringAfterLast('/'))

            if (signature == null) {
                //Ask user if they want to load an unsigned plugin
                printlnLocale("core.plugins.enable.unsigned_warning", plugin.source?.openStream()?.sha256Hash())
                printLocale("core.plugins.enable.unsigned_warning_prompt")

                loadPlugin = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                if (loadPlugin) {
                    printlnLocale("core.plugins.enable.approved_unsigned")
                } else {
                    printlnLocale("core.plugins.enable.denied_unsigned")
                }
            } else {
                if (plugin.source?.openStream()?.verify(signature, publicKey) == true) {
                    //Signature verified
                    printlnLocale("core.plugins.enable.signature_verified", plugin.pojo.name, plugin.pojo.version
                            ?: plugin.pojo.semanticVersion)
                    loadPlugin = true
                } else {
                    //Signature cannot be verified
                    printlnLocale("core.plugins.enable.invalid_signature.error")
                    loadPlugin = false
                }
            }
        }

        return loadPlugin
    }

    fun unloadAll(uid: String): List<Triple<String, String, SemVer>> {
        val loaded = loadedPlugins.filter { plugin -> plugin.uid == uid }
        loaded.forEach(this::unloadPlugin)

        return loaded.map { plugin -> Triple(plugin.uid, plugin.name, plugin.version) }
    }

    fun searchDatabase(name: String? = null, description: String? = null): Array<SpiralPluginDefinitionPojo> =
            Fuel.get("$spiralApiBase/plugins/search", listOfNotNull(name?.let("name"::to), description?.let("desc"::to)))
                    .timeout(networkConnectTimeout) //Time out if it takes longer than 5s to connect to our API
                    .timeoutRead(networkReadTimeout) //Time out if it takes longer than 5s to read a response
                    .response().also(SpiralCoreData::printResponse)
                    .takeIfSuccessful()
                    ?.let { SpiralSerialisation.JSON_MAPPER.tryReadValue<Array<SpiralPluginDefinitionPojo>>(it) }
                    ?: emptyArray()

    fun getDownloadInfo(uid: String, target: String? = null, version: String? = null): SpiralPluginDownloadInfo? =
            Fuel.get("$spiralApiBase/plugins/${listOfNotNull(uid, target, version).joinToString("/")}/download_info")
                    .timeout(networkConnectTimeout)
                    .timeoutRead(networkReadTimeout)
                    .response().also(SpiralCoreData::printResponse)
                    .takeIfSuccessful()
                    ?.let { SpiralSerialisation.JSON_MAPPER.tryReadValue(it) }

    fun getDownloadUrl(uid: String, target: String? = null, version: String? = null): String =
            "$spiralApiBase/plugins/${listOfNotNull(uid, target, version).joinToString("/")}/download"

    const val QUARANTINE_MAGIC_NUMBER = 0x4C525053544E5251L
    val quarantineRandom = SecureRandom.getInstanceStrong()

    fun quarantinePlugin(pluginFile: File) {
        if (isQuarantined(pluginFile))
            return

        val quarantinePlugin = File(pluginFile.absolutePath + ".quarantine")
        FileOutputStream(quarantinePlugin).use { out ->
            //This won't give us a super secure quarantined file, but it will ensure that the file can't just be run
            //At the point where we need to worry about a process restoring these plugin files, it's already game over
            val xorNums = ByteArray(8).apply { quarantineRandom.nextBytes(this) }

            out.writeInt64LE(QUARANTINE_MAGIC_NUMBER)
            xorNums.forEach { out.write(it.toInt() and 0xFF) }
            FileInputStream(pluginFile).use { pluginIn ->
                var index = 0

                pluginIn.readChunked { chunk ->
                    for (i in chunk.indices) {
                        chunk[i] = chunk[i] xor xorNums[index++ % xorNums.size]
                    }

                    out.write(chunk)
                }
            }
        }

        FileOutputStream(pluginFile).use { out -> out.write(0) }
        pluginFile.delete()
    }

    fun restorePlugin(quarantineFile: File) {
        if (!isQuarantined(quarantineFile))
            return

        val pluginFile = File(quarantineFile.absolutePath.substringBeforeLast(".quarantine"))
        FileOutputStream(pluginFile).use { out ->
            FileInputStream(quarantineFile).use { pluginIn ->
                pluginIn.skip(8)
                val xorNums = ByteArray(8)
                pluginIn.read(xorNums)
                var index = 0

                pluginIn.readChunked { chunk ->
                    for (i in chunk.indices) {
                        chunk[i] = chunk[i] xor xorNums[index++ % xorNums.size]
                    }

                    out.write(chunk)
                }
            }
        }

        FileOutputStream(quarantineFile).use { out -> out.write(0) }
        quarantineFile.delete()
    }

    fun isQuarantined(pluginFile: File) =
            FileInputStream(pluginFile).use { stream -> stream.readInt64LE() == QUARANTINE_MAGIC_NUMBER }

    init {
        loadPlugin(PluginEntry(SpiralCorePlugin.pojo, null))
    }
}