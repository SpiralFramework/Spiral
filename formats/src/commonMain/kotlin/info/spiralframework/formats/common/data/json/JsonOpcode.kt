package info.spiralframework.formats.common.data.json

import kotlinx.serialization.Serializable

@Serializable
data class JsonOpcode(val opcode: String, val argCount: Int, val name: String? = null, val names: Array<String>? = null)