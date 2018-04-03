package org.abimon.spiral.core.objects.archives

import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface ICustomArchive {
    fun add(dir: File)
    fun add(name: String, data: File) = add(name, data.length(), data::inputStream)
    fun add(name: String, size: Long, supplier: () -> InputStream)

    fun compile(output: OutputStream)
}