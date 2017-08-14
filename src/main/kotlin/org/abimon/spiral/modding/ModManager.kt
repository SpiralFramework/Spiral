package org.abimon.spiral.modding

import org.abimon.modBrowser.ModInfo
import org.abimon.modBrowser.ModRepository
import org.abimon.visi.io.DataSource
import org.abimon.visi.security.sha512Hash

object ModManager {
    val REPOSITORY = ModRepository("https://dr.abimon.org/spiral/modRepository")

    fun getModForFingerprint(fingerprint: DataSource): Pair<ModInfo, String>? = getModForFingerprint(fingerprint.use { it.sha512Hash() })
    fun getModForFingerprint(fingerprint: String): Pair<ModInfo, String>? {
        REPOSITORY.mods.forEach { mod ->
            val version = REPOSITORY.getModFingerprintsForName(mod.name)!!.filter { (_, prints) -> prints.containsValue(fingerprint) }.keys.firstOrNull() ?: return@forEach
            return mod to version
        }

        return null
    }
}