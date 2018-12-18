package org.abimon.spiral.core.objects

import java.io.OutputStream

interface ICompilable {
    val dataSize: Long

    fun compile(output: OutputStream)
}