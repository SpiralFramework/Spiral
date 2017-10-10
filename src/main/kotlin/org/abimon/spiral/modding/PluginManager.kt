package org.abimon.spiral.modding

import com.fasterxml.jackson.core.JsonParseException
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.visi.lang.and
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile
import java.util.zip.ZipFile

object PluginManager {
    val PLUGIN_FOLDER = File("plugins").apply {
        if(!exists())
            mkdir()
    }

    val pluginsInFolder: MutableMap<String, Triple<File, PluginConfig, Boolean>> = HashMap() //File to PluginConfig to Signed

    val loadedPlugins: MutableMap<String, Triple<File, PluginConfig, IPlugin>> = HashMap()

    fun scanForPlugins() {
        pluginsInFolder.clear()
        PLUGIN_FOLDER.listFiles { file -> file.extension == "jar" }.forEach { potentialPlugin ->
            val metadata = getMetadataForFile(potentialPlugin) ?: return@forEach

            pluginsInFolder[metadata.uid] = (potentialPlugin to metadata and isSigned(metadata.uid, potentialPlugin))
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

    fun isSigned(uid: String, file: File): Boolean = true //Tmp hack

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
                    var className = entry.name.substring(0, entry.name.length - 6)
                    className = className.replace('/', '.')
                    val clazz = classLoader.loadClass(className)

                    if(clazz.interfaces.contains(IPlugin::class.java))
                        loadedPlugins[metadata.uid] = (file to metadata and clazz.newInstance() as IPlugin)
                }
            }

            return loadedPlugins.containsKey(metadata.uid)
        } catch(io: IOException) {
            return false
        }
    }
}