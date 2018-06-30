package org.abimon.spiral.core.objects.scripting

class NonstopDebateSection(val data: IntArray) {
    constructor(size: Int): this(IntArray(size))

    operator fun get(index: Int): Int = data[index]
    operator fun set(index: Int, value: Int) { data[index] = value }

    var textID: Int
        get()       = data[0x00]
        set(value)  { data[0x00] = value }

    var type: Int
        get()       = data[0x01]
        set(value)  { data[0x01] = value }

    var shootWithEvidence: Int
        get()       = data[0x03]
        set(value)  { data[0x03] = value }

    var shouldShootWithEvidence: Boolean
        get()       = data[0x03] != 65535
        set(value)  { if(!value) data[0x03] = 65535 }

    var hasWeakPoint: Boolean
        get()       = data[0x06] > 0
        set(value)  { data[0x06] = (if(value) 1 else 0) }

    var advance: Int
        get()       = data[0x07]
        set(value)  { data[0x07] = value }

    var transition: Int
        get()       = data[0x0A]
        set(value)  { data[0x0A] = value }

    var fadeout: Int
        get()       = data[0x0B]
        set(value)  { data[0x0B] = value }

    var horizontal: Int
        get()       = data[0x0C]
        set(value)  { data[0x0C] = value }

    var vertical: Int
        get()       = data[0x0D]
        set(value)  { data[0x0D] = value }

    var angleAcceleration: Int
        get()       = data[0x0E]
        set(value)  { data[0x0E] = value }

    var angle: Int
        get()       = data[0x0F]
        set(value)  { data[0x0F] = value }

    var scale: Int
        get()       = data[0x10]
        set(value)  { data[0x10] = value }

    var finalScale: Int
        get()       = data[0x11]
        set(value)  { data[0x11] = value }

    var rotation: Int
        get()       = data[0x13]
        set(value)  { data[0x13] = value }

    var rotationSpeed: Int
        get()       = data[0x14]
        set(value)  { data[0x14] = value }

    var character: Int
        get()       = data[0x15] //21 / 42
        set(value)  { data[0x15] = value }

    var sprite: Int
        get()       = data[0x16] //22 / 44
        set(value)  { data[0x16] = value }

    var backgroundAnimation: Int
        get()       = data[0x17]
        set(value)  { data[0x17] = value }

    var voice: Int
        get()       = data[0x19] //25 / 50
        set(value)  { data[0x19] = value }

    var chapter: Int
        get()       = data[0x1B]
        set(value)  { data[0x1B] = value }

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