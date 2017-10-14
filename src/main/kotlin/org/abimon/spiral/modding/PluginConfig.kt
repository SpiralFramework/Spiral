package org.abimon.spiral.modding

data class PluginConfig(
        val name: String,
        val version: String,
        val semantic_version: String = version,
        val uid: String,
        val description: String?
)