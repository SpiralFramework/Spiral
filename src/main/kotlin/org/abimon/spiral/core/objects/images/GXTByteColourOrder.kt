package org.abimon.spiral.core.objects.images

enum class GXTByteColourOrder(val hex: Long) {
    ABGR(0x0000),
    BGRA(0x1000), //NOTE: This is different from Scarlet, but practical testing shows this to be right
    RGBA(0x2000),
    ARGB(0x3000), //NOTE: This is different from Scarlet, but practical testing shows this to be right

    _1BGR(0x4000),
    _1RGB(0x5000),

    BGR(0x0000),
    RGB(0x1000),

    GR(0x0000),
    GRRR(0x2000),
    RGGG(0x3000),
    GRGR(0x4000);

    companion object {
        fun valueOf(baseHex: Long, hex: Long): GXTByteColourOrder? = values().firstOrNull { format -> (baseHex or format.hex) == hex }
        operator fun get(baseHex: Long, hex: Long): GXTByteColourOrder? = valueOf(baseHex, hex)
    }
}