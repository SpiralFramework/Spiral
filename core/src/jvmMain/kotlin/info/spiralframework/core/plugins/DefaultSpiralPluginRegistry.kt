package info.spiralframework.core.plugins

import info.spiralframework.base.binding.readConfirmation
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_MODULE_KEY
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.printLocale
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.jvm.crypto.sha256Hash
import info.spiralframework.base.jvm.crypto.verify
import info.spiralframework.core.*
import info.spiralframework.core.plugins.events.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.brella.kornea.io.jvm.files.ensureDirectoryExists
import java.io.File
import java.io.IOException
import java.net.URLClassLoader
import java.util.*
import java.util.zip.ZipFile
import kotlin.reflect.full.createInstance

class DefaultSpiralPluginRegistry : SpiralPluginRegistry {
    val pojoServiceLoader = ServiceLoader.load(SpiralPluginRegistry.PojoProvider::class.java)

    private val loadedPlugins: MutableList<ISpiralPlugin> = ArrayList()

    private val pluginLoaders: MutableMap<String, URLClassLoader> = HashMap()

    override fun SpiralCoreContext.loadedPlugins(): List<ISpiralPlugin> = loadedPlugins

    override suspend fun SpiralCoreContext.discover(): List<PluginEntry> {
        if (postCancellable(this, BeginPluginDiscoveryEvent()))
            return emptyList()

        //We scan five locations
        //1. Local working folder called 'plugins'
        //2. Folder where Spiral is called 'plugins'
        //3. Plugin storage folder as defined by our project directory thing
        //4. (Optionally) a user defined path
        //5. ServiceLoader

        val ourFile = File(DefaultSpiralPluginRegistry::class.java.protectionDomain.codeSource.location.path).absoluteFile

        val localWorkingPlugins = File("plugins")
                .ensureDirectoryExists()
                .walk()
                .discoverPlugins(this)
        val localSpiralPlugins = File(ourFile.parentFile, "plugins")
                .ensureDirectoryExists()
                .walk()
                .discoverPlugins(this)
        val storagePlugins = File(getLocalDataDir("plugins"))
                .ensureDirectoryExists()
                .walk()
                .discoverPlugins(this)

        val classpathPlugins = pojoServiceLoader.asIterable()
                .map { provider -> provider.readPojo(this) }
                .map { pojo -> PluginEntry(pojo, null) }

        val plugins = ArrayList<PluginEntry>().apply {
            addAll(localWorkingPlugins)
            addAll(localSpiralPlugins)
            addAll(storagePlugins)
            addAll(classpathPlugins)
        }.sortedBy { entry -> entry.pojo.semanticVersion }
                .asReversed()
                .distinctBy { entry -> entry.pojo.uid }
                .filter { entry -> !postCancellable(this, DiscoveredPluginEvent(entry)) }

        post(EndPluginDiscoveryEvent())

        return plugins
    }

    suspend fun Sequence<File>.discoverPlugins(context: SpiralCoreContext): List<PluginEntry> = this.flatMap { file ->
        try {
            val zip = ZipFile(file)
            zip.entries().asSequence().filter { entry -> entry.name.endsWith("plugin.yaml") || entry.name.endsWith("plugin.yml") || entry.name.endsWith("plugin.json')") }
                    .mapNotNull { entry ->
                        if (entry.name.endsWith("json"))
                            zip.getInputStream(entry).use { stream -> context.jsonMapper.tryReadValue<SpiralPluginDefinitionPojo>(stream) }
                        else
                            zip.getInputStream(entry).use { stream -> context.yamlMapper.tryReadValue<SpiralPluginDefinitionPojo>(stream) }
                    }
                    .map { pojo -> PluginEntry(pojo, file.toURI().toURL()) }
        } catch (io: IOException) {
            return@flatMap emptySequence<PluginEntry>()
        }
    }.toList()

    override suspend fun SpiralCoreContext.loadPlugin(pluginEntry: PluginEntry): LoadPluginResult {
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

        if (pluginEntry.pojo.requiredModules?.any { str -> str !in loadedModules } == true) {
            return LoadPluginResult.REQUIRED_MODULE_NOT_LOADED
        }

        val mainModule = retrieveStaticValue(SPIRAL_MODULE_KEY)
        if (pluginEntry.pojo.supportedModules?.none { str -> str == mainModule } == true) {
            return LoadPluginResult.MODULE_NOT_SUPPORTED
        }

        if (postCancellable(this, BeginLoadingPluginEvent(pluginEntry))) {
            return LoadPluginResult.PLUGIN_LOAD_CANCELLED
        }

        val result = loadPluginInternal(pluginEntry)

        if (result.success) {
            post(SuccessfulPluginLoadEvent(pluginEntry, result))
        } else {
            post(FailedPluginLoadEvent(pluginEntry, result))
            unloadPluginInternal(pluginEntry.pojo.uid)
        }

        return result
    }

    private suspend fun SpiralCoreContext.loadPluginInternal(pluginEntry: PluginEntry): LoadPluginResult {
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

        loadedPlugins.add(plugin)
        plugin.load(this)

        return LoadPluginResult.SUCCESS
    }

    override suspend fun SpiralCoreContext.unloadPlugin(plugin: ISpiralPlugin) {
        plugin.unload(this)
        loadedPlugins.remove(plugin)
        unloadPluginInternal(plugin.uid)
    }

    private suspend fun unloadPluginInternal(uid: String) {
        withContext(Dispatchers.IO) {
            pluginLoaders.remove(uid)?.close()
        }
    }

    //TODO: Rework, now that we load a key from the jar file
    override suspend fun SpiralCoreContext.queryEnablePlugin(plugin: PluginEntry): Boolean {
        var loadPlugin = false

        val publicKey = this.publicKey
        if (publicKey == null) {
            if (spiralFrameworkOnline()) {
                //Online and key is down. Suspicious, but give the user a choice
                //Don't delete the file

                printlnLocale("core.plugins.enable.no_key.spiral_online.warning")
                printLocale("core.plugins.enable.no_key.spiral_online.warning_confirmation")

                loadPlugin = readConfirmation(defaultToAffirmative = false)

                if (loadPlugin) {
                    printlnLocale("core.plugins.enable.no_key.spiral_online.approved_plugin")
                } else {
                    printlnLocale("core.plugins.enable.no_key.spiral_online.denied_plugin")
                }
            } else if (signaturesCdnOnline()) {
                //Our Signatures CDN online, and our public key is null. Suspicious, but give the user a choice
                //Don't delete the file

                printlnLocale("core.plugins.enable.no_key.cdn_online.warning")
                printLocale("core.plugins.enable.no_key.cdn_online.warning_confirmation")

                loadPlugin = readConfirmation(defaultToAffirmative = false)

                if (loadPlugin) {
                    printlnLocale("core.plugins.enable.no_key.cdn_online.approved_plugin")
                } else {
                    printlnLocale("core.plugins.enable.no_key.cdn_online.denied_plugin")
                }
            } else {
                //Both Github and I are down; unlikely, but possible.
                //Give the user a choice, but tell them how to verify

                printlnLocale("core.plugins.enable.no_key.offline.warning")
                printLocale("core.plugins.enable.no_key.offline.warning_confirmation")

                loadPlugin = readConfirmation(defaultToAffirmative = false)

                if (loadPlugin) {
                    printlnLocale("core.plugins.enable.no_key.offline.approved_plugin")
                } else {
                    printlnLocale("core.plugins.enable.no_key.offline.denied_plugin")
                }
            }
        } else {
            val signature = signatureForPlugin(plugin.pojo.uid, plugin.pojo.semanticVersion.toString(), plugin.pojo.pluginFileName
                    ?: plugin.source!!.path.substringAfterLast('/'))

            if (signature == null) {
                //Ask user if they want to load an unsigned plugin
                printlnLocale("core.plugins.enable.unsigned_warning", plugin.source?.openStream()?.sha256Hash()
                        ?: constNull())
                printLocale("core.plugins.enable.unsigned_warning_prompt")

                loadPlugin = readConfirmation(defaultToAffirmative = false)

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
}