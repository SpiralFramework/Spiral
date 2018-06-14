package org.abimon.spiral.core.objects.text

import org.abimon.spiral.core.objects.ICompilable
import org.abimon.spiral.core.utils.writeInt16LE
import org.abimon.spiral.core.utils.writeInt32LE
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class CustomSTXT: ICompilable {
    val strings: MutableMap<Int, String> = HashMap()

    private var _dataSize: Long = 32
    override val dataSize: Long
        get() = _dataSize

    var languageID: Int = STXT.Language.JPLL.langID
    var language: STXT.Language
        get() = STXT.Language.languageFor(languageID) ?: STXT.Language.UNK
        set(value) {
            languageID = value.langID
        }

    fun add(id: Int, string: String) {
        strings[id] = string
        recalculateSize()
    }

    operator fun set(id: Int, string: String) = add(id, string)

    override fun compile(output: OutputStream) {
        val sorted = strings.toSortedMap()
        val links = strings.entries.groupBy { (_, str) -> str }.mapValues { (_, list) -> list.minBy { (index) -> index }!!.key }

        output.writeInt32LE(STXT.MAGIC_NUMBER)
        output.writeInt32LE(languageID)

        output.writeInt32LE(1) //unk
        output.writeInt32LE(32) //Table Offset



        output.writeInt32LE(8) //unk2
        output.writeInt32LE(sorted.size) //Table Size

        output.writeInt32LE(0)
        output.writeInt32LE(0)


        // Write String Data
        val stringOffsets = HashMap<Int, Int>()
        val stringData = ByteArrayOutputStream()

        sorted.forEach { index, string ->
            val linkedIndex = links[string]

            if (linkedIndex == null || linkedIndex == index || stringOffsets[linkedIndex] == null) {
                stringOffsets[index] = stringData.size()

                stringData.write(string.toByteArray(Charsets.UTF_16LE))
                stringData.writeInt16LE(0x00)
            } else {
                stringOffsets[index] = stringOffsets[linkedIndex]!!
            }
        }

        //Write Table Data
        sorted.forEach { index, _ ->
            output.writeInt32LE(index)
            output.writeInt32LE(32 + (sorted.size * 8) + (stringOffsets[index] ?: 0))
        }

        //Write the strings
        stringData.writeTo(output)

        //And we're done!
    }

    private fun recalculateSize() {
        _dataSize = 32L + (strings.size * 8) + strings.values.fold(0) { size, str -> size + str.toByteArray(Charsets.UTF_16LE).size + 2 }
    }
}