package info.spiralframework.osb.common

interface OpenSpiralBitcodeVisitor {
    object DEBUG: OpenSpiralBitcodeVisitor {
        override fun addPlainOpcode(opcode: Int, arguments: IntArray) = println("addPlainOpcode($opcode, ${arguments.joinToString()})")
        override fun addPlainOpcodeNamed(opcodeName: String, arguments: IntArray) = println("addPlainOpcodeNamed($opcodeName, ${arguments.joinToString()})")
        override fun addVariableOpcode(opcode: Int, arguments: Array<OSLUnion>) = println("addVariableOpcode($opcode, ${arguments.joinToString()})")
        override fun addVariableOpcodeNamed(opcodeName: String, arguments: Array<OSLUnion>) = println("addVariableOpcodeNamed($opcodeName, ${arguments.joinToString()})")
    }

    fun addPlainOpcode(opcode: Int, arguments: IntArray)
    fun addPlainOpcodeNamed(opcodeName: String, arguments: IntArray)
    fun addVariableOpcode(opcode: Int, arguments: Array<OSLUnion>)
    fun addVariableOpcodeNamed(opcodeName: String, arguments: Array<OSLUnion>)
}