package info.spiralframework.formats.common.data

import info.spiralframework.base.common.foldToInt16LE

public class DRv3NonstopDebateSection private constructor(override val data: IntArray) : IntArrayDataStructure {
    public companion object {
        public fun fromData(data: ByteArray): DRv3NonstopDebateSection = DRv3NonstopDebateSection(data.foldToInt16LE())
        public fun fromData(data: IntArray): DRv3NonstopDebateSection = DRv3NonstopDebateSection(data.copyOf())
    }

    public constructor(size: Int) : this(IntArray(size))

    public operator fun get(index: Int): Int = data[index]
    public operator fun set(index: Int, value: Int) {
        data[index] = value
    }

    public var textID: Int by intIndex(0x00)

    public var shootWithEvidence: Int by intIndex(0x10)
    public val shouldShootWithEvidence: Boolean by boolIndex(0x10)

    public var characterID: Int by intIndex(0xAB)
    public var modelID: Int by intIndex(0xAC)

//    var xPos: Int by intIndex(0x24)
}