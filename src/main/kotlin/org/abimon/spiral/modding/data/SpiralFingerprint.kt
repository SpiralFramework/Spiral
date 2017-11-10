package org.abimon.spiral.modding.data

data class SpiralFingerprint(
        val fingerprint: String,
        val filename: String,
        val mod_uid: String,
        val mod_version: String,
        val format_name: String?
)