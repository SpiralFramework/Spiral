package info.spiralframework.osb.common

import info.spiralframework.base.common.SemanticVersion

interface OpenSpiralBitcodeVisitor {
    object DEBUG : OpenSpiralBitcodeVisitor {
        override suspend fun setVersion(version: SemanticVersion) = println("setVersion($version)")
        override suspend fun addDialogue(speaker: Int, dialogue: OSLUnion) = println("addDialogue($speaker, $dialogue)")
        override suspend fun addDialogue(speakerName: String, dialogue: OSLUnion) = println("addDialogue($speakerName, $dialogue)")
        override suspend fun addFunctionCall(functionName: String, parameters: Array<OSLUnion.FunctionParameterType>) = println("addFunctionCall($functionName(${parameters.map { (name, value) -> if (name != null) "$name = ${stringify(value)}" else stringify(value) }.joinToString()}))")

        override suspend fun addPlainOpcode(opcode: Int, arguments: IntArray) = println("addPlainOpcode($opcode, ${arguments.joinToString()})")
        override suspend fun addPlainOpcodeNamed(opcodeName: String, arguments: IntArray) = println("addPlainOpcodeNamed($opcodeName, ${arguments.joinToString()})")
        override suspend fun addVariableOpcode(opcode: Int, arguments: Array<OSLUnion>) = println("addVariableOpcode($opcode, ${arguments.joinToString()})")
        override suspend fun addVariableOpcodeNamed(opcodeName: String, arguments: Array<OSLUnion>) = println("addVariableOpcodeNamed($opcodeName, ${arguments.joinToString()})")

        override suspend fun getData(name: String): OSLUnion? {
            println("getData($name)")
            return OSLUnion.RawStringType("\${$name}")
        }

        override suspend fun setData(name: String, data: OSLUnion) = println("setData($name, $data)")
        override suspend fun closeLongReference(): String? {
            println("closeLongReference()")
            return "\t[CLOSE]"
        }

        override suspend fun colourCodeFor(clt: String): String? {
            println("colourCodeFor($clt)")
            return "<CLT $clt>"
        }

        override suspend fun stringify(data: OSLUnion): String {
            println("stringify($data)")
            return data.toString()
        }

        override suspend fun end() = println("end()")
    }

    suspend fun setVersion(version: SemanticVersion)
    suspend fun addDialogue(speaker: Int, dialogue: OSLUnion)
    suspend fun addDialogue(speakerName: String, dialogue: OSLUnion)
    suspend fun addFunctionCall(functionName: String, parameters: Array<OSLUnion.FunctionParameterType>)

    suspend fun addPlainOpcode(opcode: Int, arguments: IntArray)
    suspend fun addPlainOpcodeNamed(opcodeName: String, arguments: IntArray)
    suspend fun addVariableOpcode(opcode: Int, arguments: Array<OSLUnion>)
    suspend fun addVariableOpcodeNamed(opcodeName: String, arguments: Array<OSLUnion>)

    suspend fun getData(name: String): OSLUnion?
    suspend fun setData(name: String, data: OSLUnion)
    suspend fun colourCodeFor(clt: String): String?
    suspend fun closeLongReference(): String?

    suspend fun stringify(data: OSLUnion): String

    suspend fun end()
}