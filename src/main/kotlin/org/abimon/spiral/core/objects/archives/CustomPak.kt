package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.OffsetInputStream
import org.abimon.spiral.core.utils.writeInt32LE
import java.io.*
import java.util.*
import kotlin.collections.HashMap

class CustomPak {
    companion object {
        val DIGITS = "\\d+".toRegex()
    }

    val files: MutableMap<Int, Pair<Long, () -> InputStream>> = HashMap()
    val mapping: MutableMap<String, Int> = HashMap()

    fun add(pak: Pak) {
        for (entry in pak.files)
            add(entry.index, entry.size.toLong()) { OffsetInputStream(pak.dataSource(), entry.offset.toLong(), entry.size.toLong()) }
    }

    fun add(dir: File) {
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

    fun add(index: Int, data: File) = add(index, data.length()) { FileInputStream(data) }
    fun add(index: Int, size: Long, supplier: () -> InputStream) = files.put(index, size to supplier)
    fun add(size: Long, supplier: () -> InputStream) = files.put(getFirstFreeIndex(), size to supplier)

    fun compile(outputStream: OutputStream) {
        outputStream.writeInt32LE(files.size)

        var offset = 4L + (files.size * 4)

        val range = (files.keys.max() ?: -1) + 1

        for (index in 0 until range) {
            val size = files[index]?.first ?: continue

            outputStream.writeInt32LE(offset)
            offset += size
        }

        for (index in 0 until range) {
            val source = files[index]?.second ?: continue
            source().use { stream -> stream.copyTo(outputStream) }
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