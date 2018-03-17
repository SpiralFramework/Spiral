package org.abimon.spiral.modding

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.github.kittinunf.fuel.Fuel
import org.abimon.spiral.core.archives.IArchive
import org.abimon.spiral.core.data.EnumSignedStatus
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.archives.ZIPFormat
import org.abimon.spiral.core.userAgent
import org.abimon.spiral.modding.data.ModConfig
import org.abimon.spiral.modding.data.SpiralFingerprint
import org.abimon.spiral.modding.data.SpiralModData
import org.abimon.spiral.util.compareTo
import org.abimon.spiral.util.copyWithProgress
import org.abimon.spiral.util.rocketFuel.largeResponse
import org.abimon.spiral.util.rocketFuel.responseStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.and
import org.abimon.visi.security.sha512Hash
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile

object ModManager : APIManager() {
    val MOD_FOLDER = File("mods").apply {
        if (!exists())
            mkdir()
    }

    val OFFICIAL_DR_MODS = arrayOf("DR1_DATA", "DR1_DATA_US", "DR1_DATA_KEYBOARD", "DR1_DATA_KEYBOARD_US", "DR2_DATA", "DR2_DATA_US", "DR2_DATA_KEYBOARD", "DR2_DATA_KEYBOARD_US")

    val modsInFolder: MutableMap<String, Triple<File, ModConfig, EnumSignedStatus>> = HashMap() //File to PluginConfig to Signed
    val newEnabledMods: MutableList<String> = ArrayList()

    fun scanForMods() {
        scanForUpdates()
        modsInFolder.clear()
        MOD_FOLDER.listFiles { file -> ZIPFormat.isFormat(null, file.name, { FileInputStream(file) }) }.forEach { potentialMod ->
            val metadata = getMetadataForFile(potentialMod) ?: return@forEach

            modsInFolder[metadata.uid] = (potentialMod to metadata and isSigned(metadata.uid, metadata.version, potentialMod))
        }
    }

    fun scanForUpdates() {
        val mods = MOD_FOLDER.listFiles { file -> ZIPFormat.isFormat(null, file.name, { FileInputStream(file) }) }.groupBy { potentialPlugin -> getMetadataForFile(potentialPlugin)?.uid }
        mods.forEach { uid, modList ->
            if (uid == null)
                return@forEach

            if (modList.size > 1) {
                val sorted = modList.sortedWith(Comparator { o1, o2 -> semanticVersionToInts(getMetadataForFile(o1)!!.semantic_version).compareTo(semanticVersionToInts(getMetadataForFile(o2)!!.semantic_version)) }).reversed()
                val latest = sorted[0]
                val replacing = sorted[1]

                sorted.forEachIndexed { index, file -> if (index > 0) file.delete() }
                latest.renameTo(replacing)
            }
        }
    }

    fun getMetadataForFile(file: File): ModConfig? {
        try {
            ZipFile(file).use { zip ->
                val jsonEntry = zip.getEntry("mod.json")
                if (jsonEntry != null)
                    return SpiralData.MAPPER.readValue(zip.getInputStream(jsonEntry), ModConfig::class.java)

                val yamlEntry = zip.getEntry("mod.yaml") ?: return null

                return SpiralData.YAML_MAPPER.readValue(zip.getInputStream(yamlEntry), ModConfig::class.java)
            }
        } catch (io: IOException) {
            return null
        } catch (json: JsonParseException) {
            return null
        }
    }

    fun modConfigFor(uid: String, version: String): ModConfig? {
        if(SpiralData.billingDead)
            return null

        val (_, response, r) = Fuel.get("$API_BASE_URL/mods/$uid/$version/info").responseStream()

        if (response.statusCode == 200)
            return SpiralData.MAPPER.readValue(r.component1() ?: return null, ModConfig::class.java)

        return null
    }

    fun modSize(uid: String, version: String): Long? {
        if(SpiralData.billingDead)
            return null

        val (_, response, _) = Fuel.head("$API_BASE_URL/mods/$uid/$version/download").response()

        if (response.statusCode == 200)
            return response.contentLength

        return null
    }

    fun downloadMod(uid: String, version: String, progress: (Long, Long) -> Unit = { _, _ -> }): Boolean {
        if(SpiralData.billingDead)
            return false

        val (name) = modConfigFor(uid, version) ?: return false
        val (_, result) = Fuel.get("$API_BASE_URL/mods/$uid/$version/download").largeResponse()
        val (response) = result

        if(response == null)
            return false

        if(response.statusCode == 200) {
            FileOutputStream(File(MOD_FOLDER, "$name-$version.zip")).use { out ->
                response.dataStream.copyWithProgress(out, progress = { copied -> progress(copied, response.contentLength) })
            }

            return true
        } else
            return false
    }

    fun getModForFingerprint(fingerprint: DataSource): SpiralFingerprint? = getModsForFingerprint(fingerprint).firstOrNull()
    fun getModForFingerprint(fingerprint: String): SpiralFingerprint? = getModsForFingerprint(fingerprint).firstOrNull()
    fun getModsForFingerprint(fingerprint: DataSource): Array<SpiralFingerprint> = getModsForFingerprint(fingerprint.use { it.sha512Hash() })
    fun getModsForFingerprint(fingerprint: String): Array<SpiralFingerprint> {
        if(SpiralData.billingDead)
            return emptyArray()

        val (_, response, _) = Fuel.Companion.get("$API_BASE_URL/fingerprint/$fingerprint").responseStream()

        if (response.statusCode == 404)
            return emptyArray()

        try {
            return SpiralData.MAPPER.readValue(response.data, Array<SpiralFingerprint>::class.java)
        } catch (json: JsonParseException) {
        } catch (json: JsonMappingException) {
        }

        return emptyArray()
    }

    fun getModsForFingerprints(fingerprints: Array<String>): Map<String, Array<SpiralFingerprint>> {
        if(SpiralData.billingDead)
            return emptyMap()

        val fingerprintMap: MutableMap<String, MutableList<SpiralFingerprint>> = HashMap()
        fingerprints.forEach { fingerprint -> fingerprintMap[fingerprint] = ArrayList() }

        fingerprints.toList().chunked(100).forEach { chunk ->
            val bodyData = SpiralData.MAPPER.writeValueAsBytes(chunk)
            var row = 0

            while (true) {
                val (_, response, _) = Fuel.post("$API_BASE_URL/fingerprints/query?row=$row&limit=100").userAgent().body(bodyData).responseStream()

                if (response.statusCode != 200)
                    break

                try {
                    val rows = SpiralData.MAPPER.readValue(response.data, Array<SpiralFingerprint>::class.java)
                    row += rows.size

                    rows.forEach { fingerprint -> fingerprintMap[fingerprint.fingerprint]!!.add(fingerprint) }
                } catch (json: JsonParseException) {
                    break
                } catch (json: JsonMappingException) {
                    break
                }
            }
        }

        return fingerprintMap.mapValues { (_, list) -> list.toTypedArray() }
    }

    fun getModsForFingerprints(fingerprints: Array<DataSource>): Map<String, Array<SpiralFingerprint>> = getModsForFingerprints(fingerprints.map { ds -> ds.use { stream -> stream.sha512Hash() } }.toTypedArray())

    fun getModsForArchive(archive: IArchive): Map<String, Pair<String, String>> {
        val definiteMods: MutableList<String> = ArrayList()
        val mods: MutableMap<String, Pair<String, String>> = HashMap()

        val rawFingerprints = archive.fileEntries.map { (name, ds) -> name to ds().use { stream -> stream.sha512Hash() } }.toMap()
        val fingerprints = getModsForFingerprints(rawFingerprints.values.toTypedArray())

        val fileMods = fingerprints.values.toTypedArray().flatten().groupBy { fingerprint -> fingerprint.filename }.mapValues { (filename, fingerprints) -> fingerprints.filter { fingerprint -> fingerprint.fingerprint == rawFingerprints[filename] } }
        println(fileMods)

        return mods
    }

    fun uidForName(name: String): String? {
        if (modsInFolder.containsKey(name))
            return name

        return modsInFolder.values.firstOrNull { (_, config, _) -> config.name == name }?.second?.uid
    }

    override fun apiSearch(query: String): Array<SpiralModData>
            = super.apiSearch(query).filter { modData -> !modData.plugin }.toTypedArray()
}