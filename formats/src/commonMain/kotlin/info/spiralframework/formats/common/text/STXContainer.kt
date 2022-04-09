package info.spiralframework.formats.common.text

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.getOrThrow
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.TextCharsets
import dev.brella.kornea.io.common.fauxSeekFromStartForResult
import dev.brella.kornea.io.common.flow.extensions.readDoubleByteNullTerminatedString
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.fauxSeekFromStart
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.withFormats
import kotlin.collections.set

public abstract class STXContainer {
    @Suppress("SpellCheckingInspection")
    public sealed class Language(public open val languageID: Int) {
        public companion object {
            private const val JPLL_MAGIC = 0x4C4C504A

            public operator fun invoke(languageID: Int): Language =
                when (languageID) {
                    JPLL_MAGIC -> JPLL
                    else -> Unknown(languageID)
                }
        }

        public object JPLL : Language(JPLL_MAGIC)
        public data class Unknown(override val languageID: Int) : Language(languageID)
    }

    public data class StringOffset(val stringID: Int, val stringOffset: Int) : Comparable<StringOffset> {
        override fun compareTo(other: StringOffset): Int = stringID.compareTo(other.stringID)
    }

    public companion object {
        public const val MAGIC_NUMBER_LE: Int = 0x54585453
        public const val PREFIX: String = "formats.stx"

        public const val INVALID_MAGIC: Int = 0x0000
        public const val INVALID_STRING_COUNT: Int = 0x0001

        public const val NOT_ENOUGH_DATA_KEY: String = "$PREFIX.not_enough_data"
        public const val INVALID_MAGIC_KEY: String = "$PREFIX.invalid_magic"
        public const val NEW_UNK_VALUE_KEY: String = "$PREFIX.new_unk_value"
        public const val NEW_UNK2_VALUE_KEY: String = "$PREFIX.new_unk2_value"
        public const val INVALID_STRING_COUNT_KEY: String = "$PREFIX.invalid_string_count"
        public const val MAPPED_STRINGS_KEY: String = "$PREFIX.mapped_strings"

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>
        ): KorneaResult<STXContainer> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_MAGIC,
                            localise(INVALID_MAGIC_KEY, magic.toHexString(), MAGIC_NUMBER_LE.toHexString())
                        )
                    }

                    val languageID = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val language = Language(languageID)

                    val unk = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (unk != 1)
                        debug(NEW_UNK_VALUE_KEY, unk)

                    val tableOffset =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val unk2 = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (unk2 != 8)
                        debug(NEW_UNK2_VALUE_KEY, unk2)

                    val count = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (count == 0)
                        return@closeAfter KorneaResult.success(EmptySTXContainer(language))

                    if (count <= 0) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_STRING_COUNT,
                            localise(INVALID_STRING_COUNT_KEY, count)
                        )
                    }

                    val stringOffsets: Array<StringOffset> =
                        flow.fauxSeekFromStartForResult(tableOffset.toULong(), dataSource) { tableFlow ->
                            KorneaResult.success(Array(count) {
                                //NOTE: These variables cannot be inlined without crashing the Kotlin compiler.
                                val id = tableFlow.readInt32LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) //String ID

                                val offset = tableFlow.readInt32LE()
                                    ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) //String Offset

                                StringOffset(id, offset)
                            })
                        }.getOrBreak { return@closeAfter it.cast() }

                    val maxStringID = stringOffsets.maxOrNull()?.stringID
                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    if (maxStringID >= count) {
                        //We need to use a map
                        debug(MAPPED_STRINGS_KEY, maxStringID)

                        val strings: MutableMap<Int, String> = HashMap()

                        stringOffsets.sortedBy(StringOffset::stringOffset).forEach { (stringID, stringOffset) ->
                            val string = flow.fauxSeekFromStart(stringOffset.toULong(), dataSource) { stringFlow ->
                                stringFlow.readDoubleByteNullTerminatedString(encoding = TextCharsets.UTF_16LE)
                            }.getOrBreak { return@closeAfter it.cast() }

                            strings[stringID] = string
                        }

                        return@closeAfter KorneaResult.success(MapBackedSTXContainer(language, strings))
                    } else {
                        val strings = arrayOfNulls<String>(count)

                        stringOffsets.sortedBy(StringOffset::stringOffset).forEach { (stringID, stringOffset) ->
                            val string = flow.fauxSeekFromStart(stringOffset.toULong(), dataSource) { stringFlow ->
                                stringFlow.readDoubleByteNullTerminatedString(encoding = TextCharsets.UTF_16LE)
                            }.getOrBreak { return@closeAfter it.cast() }

                            strings[stringID] = string
                        }

                        return@closeAfter KorneaResult.success(
                            ArrayBackedSTXContainer(
                                language,
                                strings.requireNoNulls()
                            )
                        )
                    }
                }
            }
    }

    public data class EmptySTXContainer(override val language: Language) : STXContainer() {
        override val count: Int = 0
        override fun get(index: Int): String = throw IndexOutOfBoundsException()
        override fun strings(): Array<String?> = emptyArray()
    }

    public class MapBackedSTXContainer(
        override val language: Language,
        private val backing: Map<Int, String>
    ) : STXContainer() {
        override val count: Int = backing.size

        override fun get(index: Int): String = backing.getValue(index)
        override fun strings(): Array<String?> = Array(count, backing::get)
    }

    public class ArrayBackedSTXContainer(
        override val language: Language,
        private val backing: Array<String>
    ) : STXContainer() {
        override val count: Int = backing.size

        override fun get(index: Int): String = backing[index]

        @Suppress("UNCHECKED_CAST")
        override fun strings(): Array<String?> = backing.copyOf() as Array<String?>
    }

    public abstract val language: Language

    public abstract operator fun get(index: Int): String
    public abstract val count: Int

    public abstract fun strings(): Array<String?>
}

@Suppress("FunctionName")
public suspend fun SpiralContext.STXContainer(dataSource: DataSource<*>): KorneaResult<STXContainer> = STXContainer(this, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeSTXContainer(dataSource: DataSource<*>): STXContainer = STXContainer(this, dataSource).getOrThrow()