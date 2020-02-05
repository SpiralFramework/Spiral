package info.spiralframework.formats.common.data

import info.spiralframework.base.common.foldToInt16LE

class DRv3NonstopDebateSection private constructor(override val data: IntArray): IntArrayDataStructure {
    companion object {
        fun fromData(data: ByteArray) = DRv3NonstopDebateSection(data.foldToInt16LE())
        fun fromData(data: IntArray) = DRv3NonstopDebateSection(data.copyOf())
    }

    constructor(size: Int): this(IntArray(size))

    operator fun get(index: Int): Int = data[index]
    operator fun set(index: Int, value: Int) { data[index] = value }

    var textID: Int by intIndex(0x00)

    var shootWithEvidence: Int by intIndex(0x10)
    val shouldShootWithEvidence: Boolean by boolIndex(0x10)

    var characterID: Int by intIndex(0xAB)
    var modelID: Int by intIndex(0xAC)

//    var xPos: Int by intIndex(0x24)
}