package org.abimon.spiral.core.wrd

interface WRDScript {
    val opCode: Int
    val rawArguments: IntArray
    val cmdArguments: IntArray
}