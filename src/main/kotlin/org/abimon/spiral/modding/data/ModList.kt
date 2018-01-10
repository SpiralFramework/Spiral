package org.abimon.spiral.modding.data

data class ModList(
        val mods: Map<String, ModConfig> = emptyMap(),
        val mod_files: Map<String, List<Pair<String, String>>> = emptyMap()
)