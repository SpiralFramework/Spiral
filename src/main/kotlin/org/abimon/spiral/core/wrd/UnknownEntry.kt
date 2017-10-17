package org.abimon.spiral.core.wrd

data class UnknownEntry(private val op: Int, private val arguments: IntArray): WRDScript {
    override val opCode: Int = op
    override val rawArguments: IntArray = arguments
}