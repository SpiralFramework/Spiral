package org.abimon.spiral.core.objects.images

data class GXTTexture(
        val textureOffset: Long,
        val textureSize: Long,
        val paletteIndex: Long,
        val textureFlags: Long,
        val textureType: Long,
        val textureBaseFormat: GXTBaseFormat,
        val textureColouring: GXTByteColourOrder,
        val width: Int,
        val height: Int,
        val mipmaps: Int
)