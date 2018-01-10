package org.abimon.spiral.modding.data

import org.abimon.spiral.core.data.EnumDRGame

data class ModConfig(
        val name: String,
        val version: String,
        val semantic_version: String = version,
        val uid: String,
        val description: String?,
        val applicable_titles: List<EnumDRGame> = emptyList(),
        val filterByTitle: Map<EnumDRGame, List<String>> = emptyMap()
)