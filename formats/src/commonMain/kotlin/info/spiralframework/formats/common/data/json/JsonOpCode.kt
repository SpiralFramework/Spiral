package info.spiralframework.formats.common.data.json

import kotlinx.serialization.Serializable

@Serializable
data class JsonOpCode(val opcode: String, val argCount: Int)