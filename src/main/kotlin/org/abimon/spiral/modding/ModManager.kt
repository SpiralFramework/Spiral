package org.abimon.spiral.modding

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.github.kittinunf.fuel.Fuel
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.modding.data.SpiralFingerprint
import org.abimon.spiral.util.responseStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.security.sha512Hash

/** Soon™ */
object ModManager {
    val BASE_URL = "https://dr.abimon.org/spiral/modRepository"
    val API_BASE_URL = "https://api.abimon.org/spiral"

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
}