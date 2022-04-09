package info.spiralframework.formats.common.data

import info.spiralframework.base.common.foldToInt16LE

public class LinNonstopDebateSection private constructor(override val data: IntArray): IntArrayDataStructure {
    public companion object {
        public fun fromData(data: ByteArray): LinNonstopDebateSection = LinNonstopDebateSection(data.foldToInt16LE())
        public fun fromData(data: IntArray): LinNonstopDebateSection = LinNonstopDebateSection(data.copyOf())

        public fun wrap(data: IntArray): LinNonstopDebateSection = LinNonstopDebateSection(data)
    }

    public constructor(size: Int): this(IntArray(size))

    public operator fun get(index: Int): Int = data[index]
    public operator fun set(index: Int, value: Int) { data[index] = value }

    public var textID: Int by intIndex(0x00)
    public var type: Int by intIndex(0x01)
    public var shootWithEvidence: Int by intIndex(0x03)
    public val shouldShootWithEvidence: Boolean by boolIndex(0x03, 0x0000, 0xFFFF)
    public var hasWeakPoint: Boolean by boolIndex(0x06)
    public var advance: Int by intIndex(0x07)
    public var transition: Int by intIndex(0x0A)
    public var fadeout: Int by intIndex(0x0B)
    public var horizontal: Int by intIndex(0x0C)
    public var vertical: Int by intIndex(0x0D)
    public var angleAcceleration: Int by intIndex(0x0E)
    public var angle: Int by intIndex(0x0F)

    public var scale: Int by intIndex(0x10)
    public var finalScale: Int by intIndex(0x11)
    public var rotation: Int by intIndex(0x13)
    public var rotationSpeed: Int by intIndex(0x14)
    public var character: Int by intIndex(0x15)//21 / 42
    public var sprite: Int by intIndex(0x16)
    public var backgroundAnimation: Int by intIndex(0x17)
    public var voice: Int by intIndex(0x19)
    public var chapter: Int by intIndex(0x1B)

    public operator fun component1(): Int = textID
    public operator fun component2(): Int = type
    public operator fun component3(): Int = shootWithEvidence
    public operator fun component4(): Boolean = hasWeakPoint
    public operator fun component5(): Int = advance
    public operator fun component6(): Int = transition
    public operator fun component7(): Int = fadeout
    public operator fun component8(): Int = horizontal
    public operator fun component9(): Int = vertical
    public operator fun component10(): Int = angleAcceleration
    public operator fun component11(): Int = angle
    public operator fun component12(): Int = scale
    public operator fun component13(): Int = finalScale
    public operator fun component14(): Int = rotation
    public operator fun component15(): Int = rotationSpeed
    public operator fun component16(): Int = sprite
    public operator fun component17(): Int = backgroundAnimation
    public operator fun component18(): Int = voice
    public operator fun component19(): Int = chapter
}