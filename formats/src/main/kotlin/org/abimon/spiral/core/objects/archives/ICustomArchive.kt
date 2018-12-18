package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.objects.ICompilable
import org.abimon.spiral.core.utils.copyFromStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

interface ICustomArchive: ICompilable {
    fun add(dir: File)
    fun add(name: String, data: File): Unit = addSink(name, data.length()) { out -> FileInputStream(data).use(out::copyFromStream) }
    fun add(name: String, size: Long, supplier: () -> InputStream): Unit = addSink(name, size) { out -> supplier().use(out::copyFromStream) }

    fun addSink(name: String, compilable: ICompilable): Unit = addSink(name, compilable.dataSize, compilable::compile)
    fun addSink(name: String, size: Long, sink: (OutputStream) -> Unit)

    override fun compile(output: OutputStream)
}