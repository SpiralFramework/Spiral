package org.abimon.spiral.core.wrd

import org.abimon.spiral.util.toShort

data class UnknownEntry(private val op: Int, private val arguments: IntArray): WRDScript {
    override val opCode: Int = op
    override val rawArguments: IntArray = arguments
    override val cmdArguments: IntArray = (0 until arguments.size / 2).map { i -> toShort(arguments, false, true, i * 2) }.toIntArray()
}