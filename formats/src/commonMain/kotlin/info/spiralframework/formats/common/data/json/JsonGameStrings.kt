package info.spiralframework.formats.common.data.json

import kotlinx.serialization.Serializable

@Serializable
public data class JsonGameStrings(
    val dr1: List<String> = emptyList(),
    val dr2: List<String> = emptyList(),
    val v3: List<String> = emptyList(),
    val udg: List<String> = emptyList()
)