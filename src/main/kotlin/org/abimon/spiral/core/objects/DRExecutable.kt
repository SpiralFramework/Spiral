package org.abimon.spiral.core.objects

import org.abimon.spiral.core.byteArrayOfInts
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readChunked
import org.abimon.visi.lang.EnumOS
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.*

class DRExecutable(val data: DataSource, val os: EnumOS) {
    companion object {
        val BEGIN_MARKERS = mapOf(
                EnumOS.MACOSX to byteArrayOfInts(0x4C, 0x41, 0x53, 0x54, 0x5F, 0x52, 0x45, 0x57, 0x41, 0x52, 0x44, 0x00, 0x5B, 0x47, 0x42, 0x5D, 0x5B, 0x57, 0x61, 0x72, 0x6E, 0x69, 0x6E, 0x67, 0x5D, 0x20, 0x55, 0x6E, 0x61, 0x62, 0x6C, 0x65, 0x20, 0x74, 0x6F, 0x20, 0x75, 0x6E, 0x6C, 0x6F, 0x63, 0x6B, 0x20, 0x61, 0x63, 0x68, 0x69, 0x65, 0x76, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x3A, 0x20, 0x25, 0x73, 0x00)
        )
        val END_MARKERS = mapOf(
                EnumOS.MACOSX to byteArrayOfInts(0x5B, 0x44, 0x52, 0x41, 0x70, 0x70, 0x6C, 0x69, 0x63, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x20, 0x49, 0x6E, 0x69, 0x74, 0x5D, 0x20, 0x49, 0x6E, 0x69, 0x74, 0x69, 0x61, 0x6C, 0x69, 0x7A, 0x69, 0x6E, 0x67, 0x20, 0x64, 0x65, 0x62, 0x75, 0x67, 0x20, 0x69, 0x6E, 0x66, 0x6F, 0x2E, 0x2E, 0x2E, 0x00)
        )
    }

    val startMarker: ByteArray = BEGIN_MARKERS[os] ?: "Hello, World!".toByteArray()
    val endMarker: ByteArray = END_MARKERS[os] ?: "Hello, World!".toByteArray()

    private val drData: ByteArray
    private val components: MutableList<ByteArray>

    var jpSaveFile: String
        get() = String(components[0])
        set(value) = if(value.toByteArray().size == components[0].size) components[0] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[0].size}")
    var chSaveFile: String
        get() = String(components[1])
        set(value) = if(value.toByteArray().size == components[1].size) components[1] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[1].size}")
    var enSaveFile: String
        get() = String(components[2])
        set(value) = if(value.toByteArray().size == components[2].size) components[2] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[2].size}")

    var patchingWad: String
        get() = String(components[3])
        set(value) = if(value.toByteArray().size == components[3].size) components[3] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[3].size}")
    var patchWad: String
        get() = String(components[4])
        set(value) = if(value.toByteArray().size == components[4].size) components[4] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[4].size}")
    var keyboardWad: String
        get() = String(components[5])
        set(value) = if(value.toByteArray().size == components[5].size) components[5] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[5].size}")

    var japaneseLang: String
        get() = String(components[6])
        set(value) = if(value.toByteArray().size == components[6].size) components[6] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[6].size}")
    var traditionalChineseLang: String
        get() = String(components[7])
        set(value) = if(value.toByteArray().size == components[7].size) components[7] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[7].size}")

    var archiveLoadingError: String
        get() = String(components[9])
        set(value) = if(value.toByteArray().size == components[9].size) components[9] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[9].size}")
    var patchingWadLog: String
        get() = String(components[11])
        set(value) = if(value.toByteArray().size == components[11].size) components[11] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[11].size}")
    var noPatchWarn: String
        get() = String(components[13])
        set(value) = if(value.toByteArray().size == components[13].size) components[13] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[13].size}")

    var shadersArchive: String
        get() = String(components[14])
        set(value) = if(value.toByteArray().size == components[14].size) components[14] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[14].size}")

    var localizationBin: String
        get() = String(components[17])
        set(value) = if(value.toByteArray().size == components[17].size) components[17] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[17].size}")
    var pathOfSorts: String
        get() = String(components[19])
        set(value) = if(value.toByteArray().size == components[19].size) components[19] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[19].size}")

    var defaultWad: String
        get() = String(components[20])
        set(value) = if(value.toByteArray().size == components[20].size) components[20] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[20].size}")

    var wadPrefix: String
        get() = String(components[21])
        set(value) = if(value.toByteArray().size == components[21].size) components[21] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[21].size}")

    var keyboardWadPrefix: String
        get() = String(components[22])
        set(value) = if(value.toByteArray().size == components[22].size) components[22] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[22].size}")

    var usWadSuffix: String
        get() = String(components[23])
        set(value) = if(value.toByteArray().size == components[23].size) components[23] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[23].size}")
    var usKeyboardWadSuffix: String
        get() = String(components[24])
        set(value) = if(value.toByteArray().size == components[24].size) components[24] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[24].size}")

    var jpWadSuffix: String
        get() = String(components[25])
        set(value) = if(value.toByteArray().size == components[25].size) components[25] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[25].size}")
    var jpKeyboardWadSuffix: String
        get() = String(components[26])
        set(value) = if(value.toByteArray().size == components[26].size) components[26] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[26].size}")

    var chWadSuffix: String
        get() = String(components[27])
        set(value) = if(value.toByteArray().size == components[27].size) components[27] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[27].size}")
    var chKeyboardWadSuffix: String
        get() = String(components[28])
        set(value) = if(value.toByteArray().size == components[28].size) components[28] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[28].size}")

    var patchingWadLogAgain: String
        get() = String(components[30])
        set(value) = if(value.toByteArray().size == components[30].size) components[30] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[30].size}")
    var noPatchWarnAgain: String
        get() = String(components[32])
        set(value) = if(value.toByteArray().size == components[32].size) components[32] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[32].size}")

    var mountPointError: String
        get() = String(components[34])
        set(value) = if(value.toByteArray().size == components[34].size) components[34] = value.toByteArray() else throw IllegalArgumentException("$value.toByteArray().size ≠ ${components[34].size}")

    fun changeBaseWad(newWadName: String) {
        val replacing = wadPrefix

        wadPrefix = newWadName
        patchingWad = patchingWad.replace(replacing, newWadName)
        defaultWad = defaultWad.replace(replacing, newWadName)
    }

    fun componentsToByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()
        components.forEach { baos.write(it); baos.write(0x00) }
        if(baos.size() != drData.size) throw IllegalArgumentException("Total size of components is ${baos.size()}, which is not equal to the original data size of ${drData.size}!")
        return baos.toByteArray()
    }

    fun compile(output: OutputStream) {
        when(os) {
            EnumOS.MACOSX -> {
                var startMarkerPos = 0
                var endMarkerPos = 0

                data.use { stream -> stream.readChunked { chunk ->
                    chunk.forEach { byte ->
                        if(startMarkerPos < 0) {
                            if(endMarker[endMarkerPos] == byte) {
                                endMarkerPos++
                                if(endMarkerPos >= endMarker.size) {
                                    output.write(componentsToByteArray())
                                    output.write(endMarker)
                                    startMarkerPos = 0
                                    endMarkerPos = 0
                                }
                            } else
                                endMarkerPos = 0
                        } else {
                            output.write(byte.toInt())
                            if (startMarker[startMarkerPos] == byte) {
                                startMarkerPos++
                                if (startMarkerPos >= startMarker.size)
                                    startMarkerPos = -1
                            } else {
                                startMarkerPos = 0
                            }
                        }
                    }
                } }
            }
            else -> throw IllegalArgumentException("$os is not supported (yet!)")
        }
    }

    init {
        when(os) {
            EnumOS.MACOSX -> {
                var startMarkerPos = 0
                var endMarkerPos = 0
                val wadData = ByteArrayOutputStream()

                data.use { stream -> stream.readChunked { chunk ->
                    chunk.forEach { byte ->
                        if(startMarkerPos < 0) {
                            wadData.write(byte.toInt())
                            if(endMarker[endMarkerPos] == byte) {
                                endMarkerPos++
                                if(endMarkerPos >= endMarker.size) {
                                    startMarkerPos = 0
                                    endMarkerPos = 0
                                }
                            } else
                                endMarkerPos = 0
                        } else if(startMarker[startMarkerPos] == byte) {
                            startMarkerPos++
                            if(startMarkerPos >= startMarker.size)
                                startMarkerPos = -1
                        } else {
                            startMarkerPos = 0
                        }
                    }
                } }

                drData = wadData.toByteArray().sliceArray(0 until wadData.size() - endMarker.size)
                components = drData.split(0x00).toMutableList()
            }
            else -> throw IllegalArgumentException("$os is not supported (yet!)")
        }
    }

    fun ByteArray.split(delimiter: Byte): List<ByteArray> {
        val list = ArrayList<ByteArray>()

        var prevIndex = 0
        var index = this.indexOf(delimiter)
        while(index >= 0) {
            if(index == 0) {
                prevIndex++
                index++
                continue
            }

            list.add(sliceArray(prevIndex until prevIndex + index))
            prevIndex += index + 1
            index = sliceArray(prevIndex until size).indexOf(delimiter)
        }

        return list
    }
}