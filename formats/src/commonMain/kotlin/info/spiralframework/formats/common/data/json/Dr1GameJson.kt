package info.spiralframework.formats.common.data.json

import kotlinx.serialization.Serializable

@Serializable
data class Dr1GameJson(val character_ids: Map<Int, String>, val character_identifiers: Map<String, Int>, val colour_codes: Map<String, Int>, val item_names: Array<String>, val pak_names: Map<String, Array<String>>)
    