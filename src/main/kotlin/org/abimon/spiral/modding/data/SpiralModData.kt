package org.abimon.spiral.modding.data

data class SpiralModData(
        val uid: String,
        val plugin: Boolean,
        val latest_version: String,
        val mod_name: String,
        val short_desc: String?
)