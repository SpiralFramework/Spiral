package org.abimon.spiral.core.objects.scripting.lin

interface LinScript {
    val opCode: Int
    val rawArguments: IntArray

    /**
     * This should be an interpretable statement in something like OSL
     */
    fun format(): String = "0x${opCode.toString(16)}|${rawArguments.joinToString()}"
}