package info.spiralframework.formats.common.archives

import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.copyFrom
import dev.brella.kornea.io.common.flow.extensions.writeInt16LE
import dev.brella.kornea.io.common.flow.extensions.writeInt32LE
import dev.brella.kornea.io.common.useInputFlow
import dev.brella.kornea.toolkit.common.byteArrayOfHex
import info.spiralframework.base.common.alignmentNeededFor
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@ExperimentalUnsignedTypes
public open class CustomSpcArchive {
    public companion object {
        public val SPC_MAGIC_PADDING: ByteArray = byteArrayOfHex(
            0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        )

        public val SPC_FILECOUNT_PADDING: ByteArray = byteArrayOfHex(
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00
        )

        public val SPC_TABLE_PADDING: ByteArray =
            byteArrayOfHex(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

        public val SPC_ENTRY_PADDING: ByteArray = byteArrayOfHex(
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00
        )

        public val HEADER_SIZE: Int = 16 + SPC_MAGIC_PADDING.size + SPC_FILECOUNT_PADDING.size + SPC_TABLE_PADDING.size
    }

    public data class CustomSpcEntry(
        val compressionFlag: Int,
        val compressedSize: Long,
        val decompressedSize: Long,
        val dataSource: DataSource<*>
    )

    private val _files: MutableMap<String, CustomSpcEntry> = LinkedHashMap()
    public val files: List<Map.Entry<String, CustomSpcEntry>>
        get() = _files.entries.toList()

    public fun add(spc: SpcArchive) {
        spc.files.forEach { entry ->
            this@CustomSpcArchive[entry.name] = CustomSpcEntry(
                entry.compressionFlag,
                entry.compressedSize,
                entry.decompressedSize,
                spc.openRawSource(entry)
            )
        }
    }

    public operator fun set(name: String, source: DataSource<*>): Unit =
        set(name, CustomSpcEntry(0x00, source.dataSize!!.toLong(), source.dataSize!!.toLong(), source))

    public operator fun set(name: String, compressionFlag: Int, source: DataSource<*>): Unit =
        set(name, CustomSpcEntry(compressionFlag, source.dataSize!!.toLong(), source.dataSize!!.toLong(), source))

    public operator fun set(
        name: String,
        compressionFlag: Int,
        compressedSize: Long,
        decompressedSize: Long,
        dataSource: DataSource<*>
    ): Unit = set(name, CustomSpcEntry(compressionFlag, compressedSize, decompressedSize, dataSource))

    public operator fun set(name: String, entry: CustomSpcEntry) {
        require(entry.compressedSize == entry.dataSource.dataSize?.toLong())
        _files[name] = entry
    }

    public suspend fun compile(output: OutputFlow) {
        output.writeInt32LE(SpcArchive.SPC_MAGIC_NUMBER_LE)
        output.write(SPC_MAGIC_PADDING)

        output.writeInt32LE(files.size)
        output.writeInt32LE(4)
        output.write(SPC_FILECOUNT_PADDING)

        output.writeInt32LE(SpcArchive.TABLE_MAGIC_NUMBER_LE)
        output.write(SPC_TABLE_PADDING)

        files.forEachIndexed { _, (name, entry) ->
            val entryNameBytes = name.encodeToByteArray()
            output.writeInt16LE(entry.compressionFlag)
            output.writeInt16LE(0x04)
            output.writeInt32LE(entry.compressedSize)
            output.writeInt32LE(entry.decompressedSize)
            output.writeInt32LE(entryNameBytes.size)
            output.write(SPC_ENTRY_PADDING)
            output.write(entryNameBytes)
            output.write(0x00)

            output.write(ByteArray(((entryNameBytes.size + 1) alignmentNeededFor 0x10)))
            entry.dataSource.useInputFlow { output.copyFrom(it) }
            output.write(ByteArray(entry.compressedSize alignmentNeededFor 0x10))
        }
    }
}

public inline fun buildSpcArchive(block: CustomSpcArchive.() -> Unit): CustomSpcArchive {
    val spc = CustomSpcArchive()
    spc.block()
    return spc
}

public suspend inline fun OutputFlow.compileSpcArchive(block: CustomSpcArchive.() -> Unit) {
    val spc = CustomSpcArchive()
    spc.block()
    spc.compile(this)
}