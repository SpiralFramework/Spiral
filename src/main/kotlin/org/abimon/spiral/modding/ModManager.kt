package org.abimon.spiral.modding

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.github.kittinunf.fuel.Fuel
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.userAgent
import org.abimon.spiral.modding.data.SpiralFingerprint
import org.abimon.spiral.util.responseStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.security.sha512Hash

/** Soon™ */
object ModManager {
    val BASE_URL = "https://dr.abimon.org/spiral/modRepository"
    val API_BASE_URL = "https://api.abimon.org/spiral"

    val OFFICIAL_DR_MODS = arrayOf("DR1_DATA", "DR1_DATA_US", "DR1_DATA_KEYBOARD", "DR1_DATA_KEYBOARD_US", "DR2_DATA", "DR2_DATA_US", "DR2_DATA_KEYBOARD", "DR2_DATA_KEYBOARD_US")

    fun getModForFingerprint(fingerprint: DataSource): SpiralFingerprint? = getModsForFingerprint(fingerprint).firstOrNull()
    fun getModForFingerprint(fingerprint: String): SpiralFingerprint? = getModsForFingerprint(fingerprint).firstOrNull()
    fun getModsForFingerprint(fingerprint: DataSource): Array<SpiralFingerprint> = getModsForFingerprint(fingerprint.use { it.sha512Hash() })
    fun getModsForFingerprint(fingerprint: String): Array<SpiralFingerprint> {
        val (_, response, _) = Fuel.Companion.get("$API_BASE_URL/fingerprint/$fingerprint").responseStream()

        if(response.httpStatusCode == 404)
            return emptyArray()

        try {
            return SpiralData.MAPPER.readValue(response.data, Array<SpiralFingerprint>::class.java)
        } catch(json: JsonParseException) {
        } catch(json: JsonMappingException) {
        }

        return emptyArray()
    }

    fun getModsForFingerprints(fingerprints: Array<String>): Map<String, Array<SpiralFingerprint>> {
        val fingerprintMap: MutableMap<String, MutableList<SpiralFingerprint>> = HashMap()
        val bodyData = SpiralData.MAPPER.writeValueAsBytes(fingerprints)
        var row: Int = 0

        fingerprints.forEach { fingerprint -> fingerprintMap[fingerprint] = ArrayList() }
        while (true) {
            val (_, response, _) = Fuel.post("$API_BASE_URL/fingerprints/query?row=$row&limit=100").userAgent().body(bodyData).responseStream()

            if (response.httpStatusCode != 200)
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

        return fingerprintMap.mapValues { (_, list) -> list.toTypedArray() }
    }

    fun getModsForFingerprints(fingerprints: Array<DataSource>): Map<String, Array<SpiralFingerprint>> = getModsForFingerprints(fingerprints.map { ds -> ds.use { stream -> stream.sha512Hash() } }.toTypedArray())
}