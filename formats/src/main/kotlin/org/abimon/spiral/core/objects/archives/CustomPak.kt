package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.copyFromStream
import org.abimon.spiral.core.utils.writeInt32LE
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.HashMap

class CustomPak: ICustomArchive {
    companion object {
        val DIGITS = "\\d+".toRegex()
    }

    val files: MutableMap<Int, Pair<Long, (OutputStream) -> Unit>> = HashMap()
    val mapping: MutableMap<String, Int> = HashMap()

    override val dataSize: Long
        get() = 4L + (4 * files.size) + (files.entries.sumBy { (_, value) -> value.first.toInt() })

    fun add(pak: Pak) {
        for (entry in pak.files)
            add(entry.index, entry.size.toLong(), entry::inputStream)
    }

    override fun add(dir: File) {
        for (subfile in dir.listFiles { file -> !file.isHidden && !file.name.startsWith(".") && !file.name.startsWith("__") })
            if (subfile.isDirectory) {
                val randomPakFile = File.createTempFile(UUID.randomUUID().toString(), ".pak")
                randomPakFile.deleteOnExit()

                val customPak = CustomPak()
                customPak.add(subfile)
                FileOutputStream(randomPakFile).use(customPak::compile)

                add(mapping[subfile.name] ?: subfile.name.toIntOrNull() ?: continue, randomPakFile)
            } else
                add(mapping[subfile.nameWithoutExtension] ?: subfile.nameWithoutExtension.toIntOrNull() ?: continue, subfile)
    }

    override fun addSink(name: String, size: Long, sink: (OutputStream) -> Unit) = add(name.substringBeforeLast('.').toIntOrNull() ?: getFirstFreeIndex(), size, sink)

    fun add(index: Int, data: File) = add(index, data.length(), data::inputStream)
    fun add(size: Long, supplier: () -> InputStream) = add(getFirstFreeIndex(), size, supplier)

    fun add(index: Int, size: Long, supplier: () -> InputStream) = add(index, size) { out -> supplier().use(out::copyFromStream) }
    fun add(index: Int, size: Long, sink: (OutputStream) -> Unit) {
        files[index] = size to sink
    }

    override fun compile(output: OutputStream) = compileWithProgress(output) { }

    fun compileWithProgress(output: OutputStream, progress: (Int) -> Unit) {
        output.writeInt32LE(files.size)

        var offset = 4L + (files.size * 4)

        val range = (files.keys.max() ?: -1) + 1

        for (index in 0 until range) {
            val size = files[index]?.first ?: continue

            output.writeInt32LE(offset)
            offset += size
        }

        for (index in 0 until range) {
            val sink = files[index]?.second ?: continue
            sink(output)

            progress(index)
        }
    }

    fun getFirstFreeIndex(): Int {
        var prev = 0
        for ((free) in files) {
            if(free > prev + 1)
                return prev + 1
            prev = free
        }

        return files.size
    }
}