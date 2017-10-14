package org.abimon.spiral.modding

import com.fasterxml.jackson.core.JsonParseException
import com.github.kittinunf.fuel.Fuel
import org.abimon.spiral.core.data.EnumSignedStatus
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.visi.lang.and
import org.abimon.visi.security.RSAPublicKey
import org.abimon.visi.security.verify
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.security.PublicKey
import java.util.jar.JarFile
import java.util.zip.ZipFile

object PluginManager {
    val BASE_URL = "https://dr.abimon.org/spiral/modRepository"
    val PLUGIN_FOLDER = File("plugins").apply {
        if(!exists())
            mkdir()
    }

    val pluginsInFolder: MutableMap<String, Triple<File, PluginConfig, EnumSignedStatus>> = HashMap() //File to PluginConfig to Signed
    val loadedPlugins: MutableMap<String, Triple<File, PluginConfig, IPlugin>> = HashMap()

    fun scanForPlugins() {
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

    fun isSigned(uid: String, version: String, file: File): EnumSignedStatus {
        val (_, response, _) = Fuel.get("$BASE_URL/$uid/$version/signature.dat").response()

        if(response.httpStatusCode != 200)
            return EnumSignedStatus.UNSIGNED

        val valid = file.inputStream().use { stream -> stream.verify(response.data, publicKey ?: return EnumSignedStatus.NO_PUBLIC_KEY) }

        if(valid)
            return EnumSignedStatus.SIGNED

        return EnumSignedStatus.INVALID_SIGNATURE
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

    val publicKey: PublicKey?
        get() {
            val (_, response, _) = Fuel.get("$BASE_URL/public.key").response()

            if(response.httpStatusCode != 200)
                return null

            return RSAPublicKey(String(response.data))
        }

    fun semanticVersionToInts(version: String): Triple<Int, Int, Int> {
        val components = version.split('.', limit = 3)

        val major = components[0].toIntOrNull() ?: 0
        val minor = if(components.size > 1) components[1].toIntOrNull() ?: 0 else 0
        val patch = if(components.size > 2) components[2].toIntOrNull() ?: 0 else 0

        return major to minor and patch
    }
}