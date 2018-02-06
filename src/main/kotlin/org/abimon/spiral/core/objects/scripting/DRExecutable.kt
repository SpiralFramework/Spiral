package org.abimon.spiral.core.objects.scripting

import java.io.File
import java.io.RandomAccessFile

abstract class DRExecutable(val file: File) {
    companion object {

    }

    val raf = RandomAccessFile(file, "rw")

    abstract val pakNames: Map<String, Array<String>>
}