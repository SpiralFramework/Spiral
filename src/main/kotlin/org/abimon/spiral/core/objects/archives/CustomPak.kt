package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.WindowedInputStream
import org.abimon.spiral.core.utils.writeInt32LE
import java.io.*
import java.util.*
import kotlin.collections.HashMap

class CustomPak: ICustomArchive {
    companion object {
        val DIGITS = "\\d+".toRegex()
    }

    val files: MutableMap<Int, Pair<Long, () -> InputStream>> = HashMap()
    val mapping: MutableMap<String, Int> = HashMap()

    fun add(pak: Pak) {
        for (entry in pak.files)
            add(entry.index, entry.size.toLong()) { WindowedInputStream(pak.dataSource(), entry.offset.toLong(), entry.size.toLong()) }
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

    override fun add(name: String, data: File) = add(name.substringBeforeLast('.').toIntOrNull() ?: getFirstFreeIndex(), data.length(), data::inputStream)
    override fun add(name: String, size: Long, supplier: () -> InputStream) = add(name.substringBeforeLast('.').toIntOrNull() ?: getFirstFreeIndex(), size, supplier)

    fun add(index: Int, data: File) = add(index, data.length()) { FileInputStream(data) }
    fun add(size: Long, supplier: () -> InputStream) = add(getFirstFreeIndex(), size, supplier)

    fun add(index: Int, size: Long, supplier: () -> InputStream) {
        files[index] = size to supplier
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
            val source = files[index]?.second ?: continue

            source().use { stream -> stream.copyTo(output) }
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