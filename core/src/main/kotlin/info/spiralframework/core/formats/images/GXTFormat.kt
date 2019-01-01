package info.spiralframework.core.formats.images

//object GXTFormat: SpiralImageFormat {
//    override val name: String = "GXT"
//    override val extension: String? = "gxt"
//    override val conversions: Array<SpiralFormat> = arrayOf(PNGFormat, JPEGFormat, TGAFormat, SHTXFormat, DDSFormat)
//    val HEADER = byteArrayOf(0x47, 0x58, 0x54, 0x00)
//    val VERSION = byteArrayOf(0x03, 0x00, 0x00, 0x10)
//
//    val PALETTE_BGRA = byteArrayOf(0x00, 0x10, 0x00, 0x95.toByte())
//    val LINEAR_TEXTURE = byteArrayOf(0x00, 0x00, 0x00, 0x60)
//
//    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean = dataSource().use { stream -> stream.read(4) contentEquals HEADER }
//
//    override fun toBufferedImage(name: String?, dataSource: () -> InputStream): BufferedImage {
//        var stream = dataSource()
//
//        try {
//            val magic = stream.read(4)
//            val version = stream.read(4)
//            val numTextures = stream.readUnsignedLittleInt()
//            val headerSize = stream.readUnsignedLittleInt()
//            val totalTextureSize = stream.readUnsignedLittleInt()
//            val numP4Palettes = stream.readUnsignedLittleInt()
//            val numP8Palettes = stream.readUnsignedLittleInt()
//            stream.skip(4) //Padding
//
//            val textures = Array<GXTTexture>(numTextures.toInt()) {
//                val textureOffset = stream.readUnsignedLittleInt()
//                val textureSize = stream.readUnsignedLittleInt()
//                val paletteIndex = stream.readUnsignedLittleInt()
//                val textureFlags = stream.readUnsignedLittleInt()
//                val textureType = stream.readUnsignedLittleInt() shr 24
//                val textureBaseFormat = stream.readUnsignedLittleInt()
//                val width = stream.readShort(true, true)
//                val height = stream.readShort(true, true)
//                val mipmaps = stream.readShort(true, true)
//                stream.skip(2)
//
//                val baseFormat = GXTBaseFormat[textureBaseFormat]!!
//                val colourOrdering = GXTByteColourOrder[baseFormat.hex, textureBaseFormat]!!
//
//                return@Array GXTTexture(
//                        textureOffset, textureSize, paletteIndex,
//                        textureFlags, textureType, baseFormat, colourOrdering,
//                        width, height, mipmaps
//                )
//            }
//
//            val paletteOffset = headerSize + totalTextureSize - (numP8Palettes * 256 * 4 + numP4Palettes * 16 * 4)
//
//            stream = dataSource()
//            stream.skip(paletteOffset)
//
//            val p4Palettes = Array<Array<IntArray>>(numP4Palettes.toInt()) { Array<IntArray>(16) { intArrayOf(stream.read(), stream.read(), stream.read(), stream.read()) } }
//            val p8Palettes = Array<Array<IntArray>>(numP8Palettes.toInt()) { Array<IntArray>(256) { intArrayOf(stream.read(), stream.read(), stream.read(), stream.read()) } }
//
//            val texture = textures[0]
//            val img = BufferedImage(texture.width, texture.height, BufferedImage.TYPE_INT_ARGB)
//
//            stream = dataSource()
//            stream.skip(texture.textureOffset)
//
//            when (texture.textureBaseFormat) {
//                GXTBaseFormat.P4 -> TODO()
//                GXTBaseFormat.P8 -> {
//                    for (y in 0 until texture.height)
//                        for (x in 0 until texture.width)
//                            img.setRGB(x, y, p8Palettes[texture.paletteIndex.toInt()][stream.read()].swizzle(texture.textureColouring))
//                }
//            }
//
//            return img
//        } finally {
//            stream.close()
//        }
//    }
//}