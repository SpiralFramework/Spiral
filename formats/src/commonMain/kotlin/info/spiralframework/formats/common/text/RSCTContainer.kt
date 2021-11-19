package info.spiralframework.formats.common.text

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.consumeAndGetOrBreak
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.TextCharsets
import dev.brella.kornea.io.common.flow.extensions.readDoubleByteNullTerminatedString
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.fauxSeekFromStart
import dev.brella.kornea.toolkit.common.mapToArray
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.withFormats

@ExperimentalUnsignedTypes
abstract class RSCTContainer {
    data class StringOffset(val unknown: Int, val stringOffset: Int) : Comparable<StringOffset> {
        override fun compareTo(other: StringOffset): Int = stringOffset.compareTo(other.stringOffset)
    }

    companion object {
        const val MAGIC_NUMBER_LE = 0x54435352
        const val PREFIX = "formats.rsct"

        const val INVALID_MAGIC = 0x0000
        const val INVALID_STRING_COUNT = 0x0001

        const val NOT_ENOUGH_DATA_KEY = "$PREFIX.not_enough_data"
        const val INVALID_MAGIC_KEY = "$PREFIX.invalid_magic"
        const val NEW_UNK_VALUE_KEY = "$PREFIX.new_unk_value"
        const val NEW_UNK2_VALUE_KEY = "$PREFIX.new_unk2_value"
        const val INVALID_STRING_COUNT_KEY = "$PREFIX.invalid_string_count"
        const val MAPPED_STRINGS_KEY = "$PREFIX.mapped_strings"

        private inline fun <reified T> SpiralLocale.notEnoughData() = localisedNotEnoughData<T>(NOT_ENOUGH_DATA_KEY)

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<RSCTContainer> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .consumeAndGetOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter notEnoughData()
                    if (magic != MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_MAGIC, localise(INVALID_MAGIC_KEY, magic.toHexString(), MAGIC_NUMBER_LE.toHexString()))
                    }

                    val padding = flow.readInt32LE() ?: return@closeAfter notEnoughData()
                    val stringCount = flow.readInt32LE() ?: return@closeAfter notEnoughData()
                    val unknown0C = flow.readInt32LE() ?: return@closeAfter notEnoughData()
                    if (unknown0C != 0x14) debug(NEW_UNK_VALUE_KEY, unknown0C)
                    val stringStart = flow.readInt32LE() ?: return@closeAfter notEnoughData()

                    if (stringCount <= 0) return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_STRING_COUNT, localise(INVALID_STRING_COUNT_KEY, stringCount))

                    val stringOffsets = Array(stringCount) {
                        StringOffset(
                            flow.readInt32LE() ?: return@closeAfter notEnoughData(), //String ID
                            flow.readInt32LE() ?: return@closeAfter notEnoughData() //String Offset
                        )
                    }

                    val strings: Array<String?> = arrayOfNulls(stringCount)
                    stringOffsets.withIndex()
                        .sortedBy { (_, offset) -> offset.stringOffset }
                        .forEach { (i, offset) ->
                            strings[i] = flow.fauxSeekFromStart(offset.stringOffset.toULong(), dataSource) { stringFlow ->
                                val stringLength = stringFlow.readInt32LE() //Why include the length and null termination? who knows!
                                stringFlow.readDoubleByteNullTerminatedString(encoding = TextCharsets.UTF_16LE)
                            }.getOrBreak { return@closeAfter it.cast() }
                        }

                    return@closeAfter KorneaResult.success(ArrayBackedRSCTContainer(strings as Array<String>, stringOffsets.mapToArray(StringOffset::unknown)))
                }
            }
    }

    class ArrayBackedRSCTContainer(private val backing: Array<String>, private val unknowns: Array<Int>) : RSCTContainer() {
        override val count: Int = backing.size

        override fun get(index: Int): String = backing[index]

        @Suppress("UNCHECKED_CAST")
        override fun strings(): Array<String?> = backing.copyOf() as Array<String?>
    }

    abstract operator fun get(index: Int): String
    abstract val count: Int

    abstract fun strings(): Array<String?>
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.RSCTContainer(dataSource: DataSource<*>) = RSCTContainer(this, dataSource)

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeRSCTContainer(dataSource: DataSource<*>) = RSCTContainer(this, dataSource).get()