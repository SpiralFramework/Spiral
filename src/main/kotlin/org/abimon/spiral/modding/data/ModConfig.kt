package org.abimon.spiral.modding.data

import org.abimon.spiral.core.data.EnumDRGame

data class ModConfig(
        val name: String,
        val version: String,
        val semantic_version: String = version,
        val uid: String,
        val description: String?,
        val applicable_titles: Array<EnumDRGame> = emptyArray(),
        val filterByTitle: Map<EnumDRGame, Array<String>> = emptyMap()
)