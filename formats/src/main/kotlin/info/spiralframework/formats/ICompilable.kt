package info.spiralframework.formats

import java.io.OutputStream

interface ICompilable {
    val dataSize: Long

    fun compile(output: OutputStream)
}