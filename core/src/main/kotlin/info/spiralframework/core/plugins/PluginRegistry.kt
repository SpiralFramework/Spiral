package info.spiralframework.core.plugins

import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.config.SpiralConfig
import info.spiralframework.base.util.ensureDirectoryExists
import info.spiralframework.base.util.printLocale
import info.spiralframework.base.util.printlnLocale
import info.spiralframework.base.util.verify
import info.spiralframework.core.SpiralSerialisation
import info.spiralframework.core.SpiralSignatures
import info.spiralframework.core.plugins.events.*
import info.spiralframework.core.postCancellable
import info.spiralframework.core.tryReadValue
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.net.URLClassLoader
import java.util.*
import java.util.zip.ZipFile
import kotlin.reflect.full.createInstance

object PluginRegistry {
    interface PojoProvider {
        val pojo: SpiralPluginDefinitionPojo
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

    //TODO: Don't use an int return value
    fun loadPlugin(pluginEntry: PluginEntry): Int {
        //First thing's first, check to make sure the plugin hasn't already been loaded

        if (loadedPlugins.any { plugin -> plugin.uid == pluginEntry.pojo.uid }) {
            val sameUID = loadedPlugins.first { plugin -> plugin.uid == pluginEntry.pojo.uid }
            //Same UID, uho
            //Check if we're trying to load a different version

            if (sameUID.version == pluginEntry.pojo.semanticVersion) {
                return -1
            } else {
                //Unload existing plugin
                unloadPlugin(sameUID)
            }
        }

        if (pluginEntry.pojo.uid in pluginLoaders) {
            return -2
        }

        if (EventBus.getDefault().postCancellable(BeginLoadingPluginEvent(pluginEntry))) {
            return -9
        }

        val result = loadPluginInternal(pluginEntry)

        if (result < 0) {
            EventBus.getDefault().post(FailedPluginLoadEvent(pluginEntry, result))
            unloadPluginInternal(pluginEntry.pojo.uid)
        } else {
            EventBus.getDefault().post(SuccessfulPluginLoadEvent(pluginEntry, result))
        }

        return result
    }

    private fun loadPluginInternal(pluginEntry: PluginEntry): Int {
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
                ?: return -3) as? ISpiralPlugin ?: return -4

        mutableLoadedPlugins.add(plugin)
        plugin.load()

        return 0
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
                printlnLocale("core.plugins.enable.unsigned_warning")
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
                    printlnLocale("core.plugins.enable.signature_verified", plugin.pojo.name, plugin.pojo.version ?: plugin.pojo.semanticVersion)
                    loadPlugin = true
                } else {
                    //Signature cannot be verified

                    loadPlugin = false
                }
            }
        }

        return loadPlugin
    }

    init {
        loadPlugin(PluginEntry(SpiralCorePlugin.pojo, null))
    }
}