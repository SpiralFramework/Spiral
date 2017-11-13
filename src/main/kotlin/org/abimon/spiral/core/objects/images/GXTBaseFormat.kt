package org.abimon.spiral.core.objects.images

enum class GXTBaseFormat(val hex: Long) {
    P4(0x94000000),
    P8(0x95000000);

    companion object {
        fun valueOf(hex: Long): GXTBaseFormat? = values().firstOrNull { format -> format.hex shr 24 == hex shr 24 }
        operator fun get(hex: Long): GXTBaseFormat? = valueOf(hex)
    }
}