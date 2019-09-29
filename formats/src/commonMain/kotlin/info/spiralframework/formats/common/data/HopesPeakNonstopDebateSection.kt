package info.spiralframework.formats.common.data

import info.spiralframework.base.common.foldToInt16LE

class HopesPeakNonstopDebateSection private constructor(override val data: IntArray): IntArrayDataStructure {
    companion object {
        fun fromData(data: ByteArray) = HopesPeakNonstopDebateSection(data.foldToInt16LE())
        fun fromData(data: IntArray) = HopesPeakNonstopDebateSection(data.copyOf())
    }

    constructor(size: Int): this(IntArray(size))

    operator fun get(index: Int): Int = data[index]
    operator fun set(index: Int, value: Int) { data[index] = value }

    var textID: Int by intIndex(0x00)
    var type: Int by intIndex(0x01)
    var shootWithEvidence: Int by intIndex(0x03)
    val shouldShootWithEvidence: Boolean by boolIndex(0x03, 0x0000, 0xFFFF)
    var hasWeakPoint: Boolean by boolIndex(0x06)
    var advance: Int by intIndex(0x07)
    var transition: Int by intIndex(0x0A)
    var fadeout: Int by intIndex(0x0B)
    var horizontal: Int by intIndex(0x0C)
    var vertical: Int by intIndex(0x0D)
    var angleAcceleration: Int by intIndex(0x0E)
    var angle: Int by intIndex(0x0F)

    var scale: Int by intIndex(0x10)
    var finalScale: Int by intIndex(0x11)
    var rotation: Int by intIndex(0x13)
    var rotationSpeed: Int by intIndex(0x14)
    var character: Int by intIndex(0x15)//21 / 42
    var sprite: Int by intIndex(0x16)
    var backgroundAnimation: Int by intIndex(0x17)
    var voice: Int by intIndex(0x19)
    var chapter: Int by intIndex(0x1B)

    operator fun component1(): Int = textID
    operator fun component2(): Int = type
    operator fun component3(): Int = shootWithEvidence
    operator fun component4(): Boolean = hasWeakPoint
    operator fun component5(): Int = advance
    operator fun component6(): Int = transition
    operator fun component7(): Int = fadeout
    operator fun component8(): Int = horizontal
    operator fun component9(): Int = vertical
    operator fun component10(): Int = angleAcceleration
    operator fun component11(): Int = angle
    operator fun component12(): Int = scale
    operator fun component13(): Int = finalScale
    operator fun component14(): Int = rotation
    operator fun component15(): Int = rotationSpeed
    operator fun component16(): Int = sprite
    operator fun component17(): Int = backgroundAnimation
    operator fun component18(): Int = voice
    operator fun component19(): Int = chapter
}