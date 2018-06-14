package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class CustomSPC : ICustomArchive {
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

        val HEADER_SIZE = SPC_MAGIC.size + SPC_MAGIC_PADDING.size + 8 + SPC_FILECOUNT_PADDING.size + SPC_TABLE_MAGIC.size + SPC_TABLE_PADDING.size
    }

    val files: MutableMap<String, Triple<Int, Pair<Long, Long>, (OutputStream) -> Unit>> = HashMap()

    override val dataSize: Long
        get() = (HEADER_SIZE + files.entries.sumBy { (name, value) ->
            val (compressedSize) = value.second
            val entryNameBytes = name.toByteArray(Charsets.UTF_8)

            return@sumBy (16 + SPC_ENTRY_PADDING.size + entryNameBytes.size + 1 + ((0x10 - (entryNameBytes.size + 1) % 0x10) % 0x10) + compressedSize + (0x10 - compressedSize % 0x10) % 0x10).toInt()
        }).toLong()

    fun add(spc: SPC) {
        for (entry in spc.files)
            addCompressed(entry.name, entry.compressionFlag, entry.compressedSize, entry.decompressedSize) { WindowedInputStream(spc.dataSource(), entry.offset, entry.compressedSize) }
    }

    override fun add(dir: File) = add(dir.absolutePath.length + 1, dir)
    fun add(parentLength: Int, dir: File) {
        for (subfile in dir.listFiles { file -> !file.isHidden && !file.name.startsWith(".") && !file.name.startsWith("__") })
            if (subfile.isDirectory)
                add(parentLength, subfile)
            else
                add(subfile.absolutePath.substring(parentLength), subfile)
    }

    override fun addSink(name: String, size: Long, sink: (OutputStream) -> Unit) {
        files[name.replace(File.separator, "/")] = 0x01 to (size to size) and sink
    }

    fun addCompressed(name: String, compressionFlag: Int, decompressedSize: Long, data: File) = addCompressed(name, compressionFlag, data.length(), decompressedSize) { FileInputStream(data) }
    fun addCompressed(name: String, compressionFlag: Int, compressedSize: Long, decompressedSize: Long, supplier: () -> InputStream) =
            addCompressedSink(name, compressionFlag, compressedSize, decompressedSize) { out -> supplier().use(out::copyFromStream) }

    fun addCompressedSink(name: String, compressionFlag: Int, compressedSize: Long, decompressedSize: Long, sink: (OutputStream) -> Unit) {
        files[name.replace(File.separator, "/")] = compressionFlag to (compressedSize to decompressedSize) and sink
    }


    override fun compile(output: OutputStream) = compileWithProgress(output) { _, _ -> }

    fun compileWithProgress(output: OutputStream, progress: (String, Int) -> Unit) {
        output.write(SPC_MAGIC)
        output.write(SPC_MAGIC_PADDING)

        output.writeInt32LE(files.size)
        output.writeInt32LE(4)
        output.write(SPC_FILECOUNT_PADDING)

        output.write(SPC_TABLE_MAGIC)
        output.write(SPC_TABLE_PADDING)

        files.entries.forEachIndexed { index, (name, value) ->
            val (compressionFlag, sizes, sink) = value
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

            for (i in 0 until (0x10 - (entryNameBytes.size + 1) % 0x10) % 0x10)
                output.write(0x00)

            sink(output)
            progress(name, index)

            for (i in 0 until (0x10 - compressedSize % 0x10) % 0x10)
                output.write(0x00)
        }
    }
}