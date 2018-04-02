package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.WindowedInputStream
import org.abimon.spiral.core.utils.and
import org.abimon.spiral.core.utils.writeInt16LE
import org.abimon.spiral.core.utils.writeInt32LE
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class CustomSPC {
    companion object {
        val SPC_MAGIC = intArrayOf(0x43, 0x50, 0x53, 0x2E).map(Int::toByte).toByteArray()
        val SPC_MAGIC_PADDING = intArrayOf(
                0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF,
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        ).map(Int::toByte).toByteArray()

        val SPC_FILECOUNT_PADDING = intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(Int::toByte).toByteArray()

        val SPC_TABLE_MAGIC = intArrayOf(0x52, 0x6F, 0x6F, 0x74).map(Int::toByte).toByteArray()
        val SPC_TABLE_PADDING = intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(Int::toByte).toByteArray()

        val SPC_ENTRY_PADDING = intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(Int::toByte).toByteArray()
    }

    val files: MutableMap<String, Triple<Int, Pair<Long, Long>, () -> InputStream>> = HashMap()

    fun add(spc: SPC) {
        for (entry in spc.files)
            addCompressed(entry.name, entry.compressionFlag, entry.decompressedSize, entry.compressedSize) { WindowedInputStream(spc.dataSource(), entry.offset, entry.compressedSize) }
    }


    fun add(dir: File) = add(dir.absolutePath.length + 1, dir)
    fun add(parentLength: Int, dir: File) {
        for (subfile in dir.listFiles { file -> !file.isHidden && !file.name.startsWith(".") && !file.name.startsWith("__") })
            if (subfile.isDirectory)
                add(parentLength, subfile)
            else
                add(subfile.absolutePath.substring(parentLength), subfile)
    }

    fun add(name: String, data: File) = add(name, data.length()) { FileInputStream(data) }
    fun add(name: String, size: Long, supplier: () -> InputStream) {
        files[name.replace(File.separator, "/")] = 0x01 to (size to size) and supplier
    }

    fun addCompressed(name: String, compressionFlag: Int, decompressedSize: Long, data: File) = addCompressed(name, compressionFlag, data.length(), decompressedSize) { FileInputStream(data) }
    fun addCompressed(name: String, compressionFlag: Int, compressedSize: Long, decompressedSize: Long, supplier: () -> InputStream) {
        files[name.replace(File.separator, "/")] = compressionFlag to (compressedSize to decompressedSize) and supplier
    }


    fun compile(output: OutputStream) = compileWithProgress(output) { _, _ -> }

    fun compileWithProgress(output: OutputStream, progress: (String, Int) -> Unit) {
        output.write(SPC_MAGIC)
        output.write(SPC_MAGIC_PADDING)

        output.writeInt32LE(files.size)
        output.writeInt32LE(4)
        output.write(SPC_FILECOUNT_PADDING)

        output.write(SPC_TABLE_MAGIC)
        output.write(SPC_TABLE_PADDING)

        files.entries.forEachIndexed { index, (name, value) ->
            val (compressionFlag, sizes, source) = value
            val (compressedSize, decompressedSize) = sizes

            val entryNameBytes = name.toByteArray(Charsets.UTF_8)
            output.writeInt16LE(compressionFlag)
            output.writeInt16LE(0x04)
            output.writeInt32LE(compressedSize)
            output.writeInt32LE(decompressedSize)
            output.writeInt32LE(entryNameBytes.size)
            output.write(SPC_ENTRY_PADDING)
            output.write(entryNameBytes)
            output.write(0x00)

            for(i in 0 until (0x10 - (entryNameBytes.size + 1) % 0x10) % 0x10)
                output.write(0x00)

            source().use { stream -> stream.copyTo(output) }
            progress(name, index)

            for(i in 0 until (0x10 - compressedSize % 0x10) % 0x10)
                output.write(0x00)
        }
    }
}