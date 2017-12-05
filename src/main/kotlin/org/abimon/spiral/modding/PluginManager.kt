package org.abimon.spiral.modding

import com.fasterxml.jackson.core.JsonParseException
import com.github.kittinunf.fuel.Fuel
import org.abimon.spiral.core.data.EnumSignedStatus
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.modding.data.PluginConfig
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.util.compareTo
import org.abimon.spiral.util.responseStream
import org.abimon.visi.lang.and
import org.abimon.visi.security.RSAPublicKey
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.security.PublicKey
import java.util.jar.JarFile
import java.util.zip.ZipFile

object PluginManager: APIManager() {
    val PLUGIN_FOLDER = File("plugins").apply {
        if(!exists())
            mkdir()
    }

    val pluginsInFolder: MutableMap<String, Triple<File, PluginConfig, EnumSignedStatus>> = HashMap() //File to PluginConfig to Signed
    val loadedPlugins: MutableMap<String, Triple<File, PluginConfig, IPlugin>> = HashMap()

    fun scanForPlugins() {
        scanForUpdates()
        pluginsInFolder.clear()
        PLUGIN_FOLDER.listFiles { file -> file.extension == "jar" }.forEach { potentialPlugin ->
            val metadata = getMetadataForFile(potentialPlugin) ?: return@forEach

            pluginsInFolder[metadata.uid] = (potentialPlugin to metadata and isSigned(metadata.uid, metadata.version, potentialPlugin))
        }
    }

    fun getMetadataForFile(file: File): PluginConfig? {
        try {
            ZipFile(file).use { zip ->
                val jsonEntry = zip.getEntry("plugin.json")
                if (jsonEntry != null)
                    return SpiralData.MAPPER.readValue(zip.getInputStream(jsonEntry), PluginConfig::class.java)

                val yamlEntry = zip.getEntry("plugin.yaml") ?: return null

                return SpiralData.YAML_MAPPER.readValue(zip.getInputStream(yamlEntry), PluginConfig::class.java)
            }
        } catch (io: IOException) {
            return null
        } catch (json: JsonParseException) {
            return null
        }
    }

    fun loadPlugin(uid: String): Boolean {
        if(pluginsInFolder.containsKey(uid)) {
            if(!loadClasses(pluginsInFolder[uid]!!.first))
                return false
            (loadedPlugins[uid] ?: return false).third.enable(SpiralModel.imperator)

            return true
        }

        return false
    }

    private fun loadClasses(file: File): Boolean {
        try {
            val metadata = getMetadataForFile(file) ?: return false
            val jarFile = JarFile(file)
            val urls = arrayOf(URL("jar:file:${file.absolutePath}!/"))
            val classLoader = URLClassLoader.newInstance(urls)

            jarFile.use { jar ->
                jar.entries().toList().forEach { entry ->
                    if (entry.isDirectory || !entry.name.endsWith(".class"))
                        return@forEach
                    // -6 because of .class
                    val className = entry.name.substring(0, entry.name.length - 6).replace('/', '.')
                    val clazz = loadClass(classLoader, className)

                    if(clazz.interfaces.contains(IPlugin::class.java))
                        loadedPlugins[metadata.uid] = (file to metadata and clazz.newInstance() as IPlugin)
                }
            }

            return loadedPlugins.containsKey(metadata.uid)
        } catch(io: IOException) {
            return false
        }
    }

    private fun loadClass(classLoader: ClassLoader, name: String): Class<*> {
        try {
            return Class.forName(name)
        } catch (link: LinkageError) {
        } catch (inInitializer: ExceptionInInitializerError) {
        } catch (notFound: ClassNotFoundException) {
        }

        return classLoader.loadClass(name)
    }

    fun scanForUpdates() {
        val plugins = PLUGIN_FOLDER.listFiles { file -> file.extension == "jar" }.groupBy { potentialPlugin -> getMetadataForFile(potentialPlugin)?.uid }
        plugins.forEach { uid, pluginList ->
            if(uid == null)
                return@forEach

            if(pluginList.size > 1) {
                val sorted = pluginList.sortedWith(Comparator { o1, o2 -> semanticVersionToInts(getMetadataForFile(o1)!!.semantic_version).compareTo(semanticVersionToInts(getMetadataForFile(o2)!!.semantic_version)) }).reversed()
                val latest = sorted[0]
                val replacing = sorted[1]

                sorted.forEachIndexed { index, file -> if(index > 0) file.delete() }
                latest.renameTo(replacing)
            }
        }
    }

    val publicKey: PublicKey?
        get() {
            val (_, response, _) = Fuel.get("$BASE_URL/public.key").response()

            if(response.httpStatusCode != 200)
                return null

            return RSAPublicKey(String(response.data))
        }

    fun pluginConfigFor(uid: String, version: String): PluginConfig? {
        val (_, response, r) = Fuel.get("$API_BASE_URL/mods/$uid/$version/info").responseStream()

        if(response.httpStatusCode == 200)
            return SpiralData.MAPPER.readValue(r.component1() ?: return null, PluginConfig::class.java)

        return null
    }

    fun pluginSize(uid: String, version: String): Long? {
        val (_, response, _) = Fuel.head("$API_BASE_URL/mods/$uid/$version/download").response()

        if(response.httpStatusCode == 200)
            return response.httpContentLength

        return null
    }

    fun downloadPlugin(uid: String, version: String, progress: (Long, Long) -> Unit = { _, _ -> }): Boolean {
        val (name) = pluginConfigFor(uid, version) ?: return false
        val (_, response, _) = Fuel.download("$API_BASE_URL/mods/$uid/$version/download").progress(progress).destination { response, url -> File(PLUGIN_FOLDER, "$name-$version.jar") }.responseStream()

        return response.httpStatusCode == 200
    }

    fun uidForName(name: String): String? {
        if(pluginsInFolder.containsKey(name))
            return name

        return pluginsInFolder.values.firstOrNull { (_, config, _) -> config.name == name }?.second?.uid
    }
}