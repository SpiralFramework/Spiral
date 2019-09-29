package info.spiralframework.formats.common.data

import info.spiralframework.base.common.foldToInt16LE

class V3NonstopDebateSection private constructor(override val data: IntArray): IntArrayDataStructure {
    companion object {
        fun fromData(data: ByteArray) = V3NonstopDebateSection(data.foldToInt16LE())
        fun fromData(data: IntArray) = V3NonstopDebateSection(data.copyOf())
    }

    constructor(size: Int): this(IntArray(size))

    operator fun get(index: Int): Int = data[index]
    operator fun set(index: Int, value: Int) { data[index] = value }

    var textID: Int by intIndex(0x00)

    var shootWithEvidence: Int by intIndex(0x10)
    val shouldShootWithEvidence: Boolean by boolIndex(0x10)

//    var xPos: Int by intIndex(0x24)
}