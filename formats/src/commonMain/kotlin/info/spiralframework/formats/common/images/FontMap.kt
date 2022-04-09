package info.spiralframework.formats.common.images

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.fauxSeekFromStartForResult
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32BE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import kotlin.collections.set

public class FontMap(
    public val unk1: Int,
    public val unk2: Int,
    public val mappingTable: Map<Char, Int>,
    public val glyphs: Array<Glyph>
) {
    public data class Glyph(
        val character: Char,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val unk1: Int,
        val unk2: Int,
        val unk3: Int
    )

    public companion object {
        public const val PREFIX: String = "formats.font_map.dr1"

        public const val INVALID_MAGIC: Int = 0x0000

        public const val NOT_ENOUGH_DATA_KEY: String = "$PREFIX.not_enough_data"
        public const val INVALID_MAGIC_KEY: String = "$PREFIX.invalid_magic"

        public const val MAGIC_NUMBER_BE: Int = 0x74467053
        public val VALID_GLYPH_RANGE: IntRange = 0 until Short.MAX_VALUE

        public suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<FontMap> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return it.cast() }

                closeAfter(flow) {
                    val magicNumber =
                        flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magicNumber != MAGIC_NUMBER_BE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_MAGIC,
                            localise(
                                INVALID_MAGIC_KEY,
                                "0x${magicNumber.toString(16)}",
                                "0x${MAGIC_NUMBER_BE.toString(16)}"
                            )
                        )
                    }

                    val version = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    trace("$PREFIX.version", version)

                    val fontTableEntryCount =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val fontTableStart =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val mappingTableEntryCount = flow.readInt32LE()
                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val mappingTableStart =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val unk1 = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk2 = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val mappingTable =
                        flow.fauxSeekFromStartForResult(mappingTableStart.toULong(), dataSource) { mappingTableFlow ->
                            val mappingTable: MutableMap<Char, Int> = HashMap()

                            for (i in 0 until mappingTableEntryCount) {
                                val glyphIndex = mappingTableFlow.readInt16LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                                if (glyphIndex in VALID_GLYPH_RANGE) {
                                    mappingTable[i.toChar()] = glyphIndex
                                }
                            }

                            KorneaResult.success(mappingTable)
                        }

                    val glyphs =
                        flow.fauxSeekFromStartForResult(fontTableStart.toULong(), dataSource) { fontTableFlow ->
                            KorneaResult.success(Array(fontTableEntryCount) {
                                val character = fontTableFlow.readInt16LE()?.toChar()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                val x = fontTableFlow.readInt16LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                val y = fontTableFlow.readInt16LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                val width = fontTableFlow.readInt16LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                val height = fontTableFlow.readInt16LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                                //leftSpacing?
                                val glyphUnk1 = fontTableFlow.readInt16LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                //rightSpacing?
                                val glyphUnk2 = fontTableFlow.readInt16LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                //verticalSpacing?
                                val glyphUnk3 = fontTableFlow.readInt16LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                                return@Array Glyph(character, x, y, width, height, glyphUnk1, glyphUnk2, glyphUnk3)
                            })
                        }

                    @Suppress("NAME_SHADOWING")
                    return@closeAfter mappingTable.flatMap { mappingTable ->
                        glyphs.map { glyphs -> FontMap(unk1, unk2, mappingTable, glyphs) }
                    }
                }
            }
    }
}

@Suppress("FunctionName")
public suspend fun SpiralContext.FontMap(dataSource: DataSource<*>): KorneaResult<FontMap> = FontMap(this, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeFontMap(dataSource: DataSource<*>): FontMap = FontMap(this, dataSource).getOrThrow()