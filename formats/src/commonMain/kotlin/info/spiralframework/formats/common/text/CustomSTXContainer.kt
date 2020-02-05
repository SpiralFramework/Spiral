package info.spiralframework.formats.common.text

import info.spiralframework.base.binding.encodeToUTF16LEByteArray
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.common.writeInt16LE
import org.abimon.kornea.io.common.writeInt32LE

@ExperimentalUnsignedTypes
class CustomSTXContainer {
    private val _strings: MutableMap<Int, String> = HashMap()
    val strings: Map<Int, String>
        get() = _strings

    var language: STXContainer.Language = STXContainer.Language.JPLL
    var languageID: Int
        get() = language.languageID
        set(value) {
            language = STXContainer.Language(value)
        }

    var performExistingStringOptimisationOnAdd = true
    var performExistingStringOptimisationOnCompile = true

    fun add(string: String): Int {
        //First, check if the string already exists
        if (performExistingStringOptimisationOnAdd) {
            _strings.forEach { (existingID, str) ->
                if (string == str) {
                    return existingID
                }
            }
        }

        //IntelliJ Bug
        @Suppress("ReplaceManualRangeWithIndicesCalls")
        for (i in 0 until _strings.size) {
            if (i !in _strings) {
                _strings[i] = string
                return i
            }
        }
        val id = _strings.size
        _strings[id] = string
        return id
    }

    fun add(stringID: Int, string: String) = set(stringID, string)
    operator fun set(stringID: Int, string: String) {
        _strings[stringID] = string
    }

    fun addAll(newStrings: Array<String>): IntArray {
        val stringIDs = IntArray(newStrings.size) { -1 }

        //First, check if the strings already exists
        if (performExistingStringOptimisationOnAdd) {
            _strings.forEach { (existingID, str) ->
                newStrings.forEachIndexed { index, newStr ->
                    if (newStr == str) {
                        stringIDs[index] = existingID
                    }
                }
            }
        }

        var previousIndex = -1

        newStrings.forEachIndexed { index, newStr ->
            if (stringIDs[index] != -1)
                return@forEachIndexed

            while (++previousIndex < _strings.size) {
                if (previousIndex !in _strings) {
                    break
                }
            }

            _strings[previousIndex] = newStr
            stringIDs[index] = previousIndex
        }

        return stringIDs
    }

    fun addAll(newStrings: Collection<String>): IntArray {
        val stringIDs = IntArray(newStrings.size) { -1 }

        //First, check if the strings already exists
        if (performExistingStringOptimisationOnAdd) {
            _strings.forEach { (existingID, str) ->
                newStrings.forEachIndexed { index, newStr ->
                    if (newStr == str) {
                        stringIDs[index] = existingID
                    }
                }
            }
        }

        var previousIndex = -1

        newStrings.forEachIndexed { index, newStr ->
            if (stringIDs[index] != -1)
                return@forEachIndexed

            if (performExistingStringOptimisationOnAdd) {
                val prevStrIndex = newStrings.indexOf(newStr)
                if (prevStrIndex < index) {
                    stringIDs[index] = stringIDs[prevStrIndex]
                    return@forEachIndexed
                }
            }

            while (++previousIndex < _strings.size) {
                if (previousIndex !in _strings) {
                    break
                }
            }

            _strings[previousIndex] = newStr
            stringIDs[index] = previousIndex
        }

        return stringIDs
    }

    @ExperimentalStdlibApi
    suspend fun compile(output: OutputFlow) {
        val sortedStrings = strings
                .mapValues { (_, str) -> str.encodeToUTF16LEByteArray() }
                .entries
                .sortedBy(Map.Entry<Int, ByteArray>::key)
        output.writeInt32LE(STXContainer.MAGIC_NUMBER_LE)
        output.writeInt32LE(languageID)

        output.writeInt32LE(1)                  // Unk
        output.writeInt32LE(32)                 // Table Offset

        output.writeInt32LE(8)                  // Unk2
        output.writeInt32LE(sortedStrings.size)      // Table Size

        output.writeInt32LE(0)
        output.writeInt32LE(0)

        val linkOffsets: MutableMap<Int, Int> = HashMap()
        val links: MutableMap<Int, Int> = HashMap()

        if (performExistingStringOptimisationOnCompile) {
            _strings.entries.forEach { (index, str) ->
                _strings.filterValues { otherStr -> otherStr == str }
                        .takeIf { map -> map.size > 1 }
                        ?.forEach { (otherIndex) -> if (otherIndex != index && otherIndex !in links) links[otherIndex] = index }
            }
        }

        //Write String Data
        var offset = 32 + (sortedStrings.size * 8)
        sortedStrings.forEach { (index, string) ->
            output.writeInt32LE(index)

            if (index in links && links[index] in linkOffsets) {
                output.writeInt32LE(linkOffsets.getValue(links.getValue(index)))
            } else {
                output.writeInt32LE(offset)
                linkOffsets[index] = offset

                offset += string.size + 2
            }
        }

        sortedStrings.forEach { (_, string) ->
            output.write(string)
            output.writeInt16LE(0x00)
        }
    }
}