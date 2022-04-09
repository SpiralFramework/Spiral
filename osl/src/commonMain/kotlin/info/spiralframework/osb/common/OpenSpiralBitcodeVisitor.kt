package info.spiralframework.osb.common

import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext

public interface OpenSpiralBitcodeVisitor {
    public object DEBUG : OpenSpiralBitcodeVisitor {
        override suspend fun setVersion(context: SpiralContext, version: SemanticVersion): Unit =
            println("setVersion($version)")

        override suspend fun addDialogue(context: SpiralContext, speaker: Int, dialogue: OSLUnion): Unit =
            println("addDialogue($speaker, $dialogue)")

        override suspend fun addDialogue(context: SpiralContext, speakerName: String, dialogue: OSLUnion): Unit =
            println("addDialogue($speakerName, $dialogue)")

        override suspend fun functionCall(
            context: SpiralContext,
            functionName: String,
            parameters: Array<OSLUnion.FunctionParameterType>
        ): OSLUnion.NoOpType = runNoOp {
            println(
                "addFunctionCall($functionName(${
                    parameters.map { (name, value) ->
                        if (name != null) "$name = ${
                            stringify(
                                context,
                                value
                            )
                        }" else stringify(context, value)
                    }.joinToString()
                }))"
            )
        }

        override suspend fun addFlagCheck(
            context: SpiralContext,
            mainBranch: OpenSpiralBitcodeFlagBranch,
            elseIfBranches: Array<OpenSpiralBitcodeFlagBranch>,
            elseBranch: ByteArray?,
            level: Int
        ): Unit = println("[$level] addFlagCheck($mainBranch, $elseIfBranches, $elseBranch)")

        override suspend fun addTree(context: SpiralContext, treeType: Int, scope: ByteArray, level: Int): Unit =
            println("[$level] addTree($treeType, $scope)")

        override suspend fun addLoadMap(
            context: SpiralContext,
            mapID: OSLUnion,
            state: OSLUnion,
            arg3: OSLUnion,
            scope: ByteArray,
            level: Int
        ): Unit = println("[$level] loadMap($mapID, $state, $arg3, $scope)")

        override suspend fun addPlainOpcode(context: SpiralContext, opcode: Int, arguments: IntArray): Unit =
            println("addPlainOpcode($opcode, ${arguments.joinToString()})")

        override suspend fun addPlainOpcodeNamed(
            context: SpiralContext,
            opcodeName: String,
            arguments: IntArray
        ): Unit = println("addPlainOpcodeNamed($opcodeName, ${arguments.joinToString()})")

        override suspend fun addVariableOpcode(context: SpiralContext, opcode: Int, arguments: Array<OSLUnion>): Unit =
            println("addVariableOpcode($opcode, ${arguments.joinToString()})")

        override suspend fun addVariableOpcodeNamed(
            context: SpiralContext,
            opcodeName: String,
            arguments: Array<OSLUnion>
        ): Unit = println("addVariableOpcodeNamed($opcodeName, ${arguments.joinToString()})")

        override suspend fun getData(context: SpiralContext, name: String): OSLUnion {
            println("getData($name)")
            return OSLUnion.RawStringType("\${$name}")
        }

        override suspend fun setData(context: SpiralContext, name: String, data: OSLUnion): Unit =
            println("setData($name, $data)")

        override suspend fun closeLongReference(context: SpiralContext): String {
            println("closeLongReference()")
            return "\t[CLOSE]"
        }

        override suspend fun colourCodeFor(context: SpiralContext, clt: String): String {
            println("colourCodeFor($clt)")
            return "<CLT $clt>"
        }

        override suspend fun stringify(context: SpiralContext, data: OSLUnion): String {
            println("stringify($data)")
            return data.toString()
        }

        override suspend fun end(context: SpiralContext): Unit = println("end()")
    }

    public suspend fun setVersion(context: SpiralContext, version: SemanticVersion)
    public suspend fun addDialogue(context: SpiralContext, speaker: Int, dialogue: OSLUnion)
    public suspend fun addDialogue(context: SpiralContext, speakerName: String, dialogue: OSLUnion)
    public suspend fun functionCall(
        context: SpiralContext,
        functionName: String,
        parameters: Array<OSLUnion.FunctionParameterType>
    ): OSLUnion?

    public suspend fun addFlagCheck(
        context: SpiralContext,
        mainBranch: OpenSpiralBitcodeFlagBranch,
        elseIfBranches: Array<OpenSpiralBitcodeFlagBranch>,
        elseBranch: ByteArray?,
        level: Int
    )

    public suspend fun addTree(context: SpiralContext, treeType: Int, scope: ByteArray, level: Int)
    public suspend fun addLoadMap(
        context: SpiralContext,
        mapID: OSLUnion,
        state: OSLUnion,
        arg3: OSLUnion,
        scope: ByteArray,
        level: Int
    )

    public suspend fun addPlainOpcode(context: SpiralContext, opcode: Int, arguments: IntArray)
    public suspend fun addPlainOpcodeNamed(context: SpiralContext, opcodeName: String, arguments: IntArray)
    public suspend fun addVariableOpcode(context: SpiralContext, opcode: Int, arguments: Array<OSLUnion>)
    public suspend fun addVariableOpcodeNamed(context: SpiralContext, opcodeName: String, arguments: Array<OSLUnion>)

    public suspend fun getData(context: SpiralContext, name: String): OSLUnion?
    public suspend fun setData(context: SpiralContext, name: String, data: OSLUnion)
    public suspend fun colourCodeFor(context: SpiralContext, clt: String): String?
    public suspend fun closeLongReference(context: SpiralContext): String?

    public suspend fun stringify(context: SpiralContext, data: OSLUnion): String

    public suspend fun end(context: SpiralContext)
}