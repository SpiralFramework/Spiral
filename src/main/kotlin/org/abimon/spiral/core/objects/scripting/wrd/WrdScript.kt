package org.abimon.spiral.core.objects.scripting.wrd

interface WrdScript {
    val opCode: Int
    val rawArguments: IntArray
}