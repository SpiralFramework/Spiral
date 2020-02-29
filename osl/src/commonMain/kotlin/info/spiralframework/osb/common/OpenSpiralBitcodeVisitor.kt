package info.spiralframework.osb.common

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext

interface OpenSpiralBitcodeVisitor {
    object DEBUG : OpenSpiralBitcodeVisitor {
        override suspend fun setVersion(context: SpiralContext, version: SemanticVersion) = println("setVersion($version)")
        override suspend fun addDialogue(context: SpiralContext, speaker: Int, dialogue: OSLUnion) = println("addDialogue($speaker, $dialogue)")
        override suspend fun addDialogue(context: SpiralContext, speakerName: String, dialogue: OSLUnion) = println("addDialogue($speakerName, $dialogue)")
        override suspend fun functionCall(context: SpiralContext, functionName: String, parameters: Array<OSLUnion.FunctionParameterType>) = runNoOp { println("addFunctionCall($functionName(${parameters.map { (name, value) -> if (name != null) "$name = ${stringify(context, value)}" else stringify(context, value) }.joinToString()}))") }
        override suspend fun addFlagCheck(context: SpiralContext, mainBranch: OpenSpiralBitcodeFlagBranch, elseIfBranches: Array<OpenSpiralBitcodeFlagBranch>, elseBranch: ByteArray?, level: Int) = println("[$level] addFlagCheck($mainBranch, $elseIfBranches, $elseBranch)")
        override suspend fun addTree(context: SpiralContext, treeType: Int, scope: ByteArray, level: Int) = println("[$level] addTree($treeType, $scope)")
        override suspend fun addLoadMap(context: SpiralContext, mapID: OSLUnion, state: OSLUnion, arg3: OSLUnion, scope: ByteArray, level: Int) = println("[$level] loadMap($mapID, $state, $arg3, $scope)")

        override suspend fun addPlainOpcode(context: SpiralContext, opcode: Int, arguments: IntArray) = println("addPlainOpcode($opcode, ${arguments.joinToString()})")
        override suspend fun addPlainOpcodeNamed(context: SpiralContext, opcodeName: String, arguments: IntArray) = println("addPlainOpcodeNamed($opcodeName, ${arguments.joinToString()})")
        override suspend fun addVariableOpcode(context: SpiralContext, opcode: Int, arguments: Array<OSLUnion>) = println("addVariableOpcode($opcode, ${arguments.joinToString()})")
        override suspend fun addVariableOpcodeNamed(context: SpiralContext, opcodeName: String, arguments: Array<OSLUnion>) = println("addVariableOpcodeNamed($opcodeName, ${arguments.joinToString()})")

        override suspend fun getData(context: SpiralContext, name: String): OSLUnion? {
            println("getData($name)")
            return OSLUnion.RawStringType("\${$name}")
        }

        override suspend fun setData(context: SpiralContext, name: String, data: OSLUnion) = println("setData($name, $data)")
        override suspend fun closeLongReference(context: SpiralContext): String? {
            println("closeLongReference()")
            return "\t[CLOSE]"
        }

        override suspend fun colourCodeFor(context: SpiralContext, clt: String): String? {
            println("colourCodeFor($clt)")
            return "<CLT $clt>"
        }

        override suspend fun stringify(context: SpiralContext, data: OSLUnion): String {
            println("stringify($data)")
            return data.toString()
        }

        override suspend fun end(context: SpiralContext) = println("end()")
    }

    suspend fun setVersion(context: SpiralContext, version: SemanticVersion)
    suspend fun addDialogue(context: SpiralContext, speaker: Int, dialogue: OSLUnion)
    suspend fun addDialogue(context: SpiralContext, speakerName: String, dialogue: OSLUnion)
    suspend fun functionCall(context: SpiralContext, functionName: String, parameters: Array<OSLUnion.FunctionParameterType>): OSLUnion?
    suspend fun addFlagCheck(context: SpiralContext, mainBranch: OpenSpiralBitcodeFlagBranch, elseIfBranches: Array<OpenSpiralBitcodeFlagBranch>, elseBranch: ByteArray?, level: Int)
    suspend fun addTree(context: SpiralContext, treeType: Int, scope: ByteArray, level: Int)
    suspend fun addLoadMap(context: SpiralContext, mapID: OSLUnion, state: OSLUnion, arg3: OSLUnion, scope: ByteArray, level: Int)

    suspend fun addPlainOpcode(context: SpiralContext, opcode: Int, arguments: IntArray)
    suspend fun addPlainOpcodeNamed(context: SpiralContext, opcodeName: String, arguments: IntArray)
    suspend fun addVariableOpcode(context: SpiralContext, opcode: Int, arguments: Array<OSLUnion>)
    suspend fun addVariableOpcodeNamed(context: SpiralContext, opcodeName: String, arguments: Array<OSLUnion>)

    suspend fun getData(context: SpiralContext, name: String): OSLUnion?
    suspend fun setData(context: SpiralContext, name: String, data: OSLUnion)
    suspend fun colourCodeFor(context: SpiralContext, clt: String): String?
    suspend fun closeLongReference(context: SpiralContext): String?

    suspend fun stringify(context: SpiralContext, data: OSLUnion): String

    suspend fun end(context: SpiralContext)
}