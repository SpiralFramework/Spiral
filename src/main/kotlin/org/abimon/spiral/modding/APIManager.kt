package org.abimon.spiral.modding

import com.github.kittinunf.fuel.Fuel
import org.abimon.spiral.core.data.EnumSignedStatus
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.userAgent
import org.abimon.spiral.modding.data.SpiralModData
import org.abimon.spiral.util.SemanticVersion
import org.abimon.spiral.util.rocketFuel.responseStream
import org.abimon.visi.lang.and
import org.abimon.visi.security.verify
import java.io.File

abstract class APIManager {
    companion object {
        val BASE_URL = "https://dr.abimon.org/spiral/modRepository"
        val API_BASE_URL = "https://api.abimon.org/spiral"
    }

    fun semanticVersionToInts(version: String): SemanticVersion {
        val components = version.split('.', limit = 3)

        val major = components[0].toIntOrNull() ?: 0
        val minor = if(components.size > 1) components[1].toIntOrNull() ?: 0 else 0
        val patch = if(components.size > 2) components[2].toIntOrNull() ?: 0 else 0

        return major to minor and patch
    }

    open fun apiSearch(query: String): Array<SpiralModData> {
        if(SpiralData.billingDead)
            return emptyArray()

        val (_, response, r) = Fuel.get("$API_BASE_URL/search", listOf("q" to query)).userAgent().responseStream()

        if(response.statusCode == 200)
            return SpiralData.MAPPER.readValue(r.component1() ?: return emptyArray(), Array<SpiralModData>::class.java)

        return emptyArray()
    }

    open fun isSigned(uid: String, version: String, file: File): EnumSignedStatus {
        if(SpiralData.billingDead)
            return EnumSignedStatus.UNSIGNED

        val (_, response, _) = Fuel.get("$API_BASE_URL/mods/$uid/$version/signature").response()

        if(response.statusCode != 200)
            return EnumSignedStatus.UNSIGNED

        val valid = file.inputStream().use { stream -> stream.verify(response.data, PluginManager.publicKey ?: return EnumSignedStatus.NO_PUBLIC_KEY) }

        if(valid)
            return EnumSignedStatus.SIGNED

        return EnumSignedStatus.INVALID_SIGNATURE
    }
}