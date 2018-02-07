package org.abimon.spiral.core.objects.scripting.lin

interface LinScript {
    val opCode: Int
    val rawArguments: IntArray
}