package info.spiralframework.formats.common.text

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readDoubleByteNullTerminatedString
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.fauxSeekFromStart
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.use

@ExperimentalUnsignedTypes
abstract class STXContainer {
    @Suppress("SpellCheckingInspection")
    sealed class Language(open val languageID: Int) {
        companion object {
            private const val JPLL_MAGIC = 0x4C4C504A
            operator fun invoke(languageID: Int): Language =
                    when (languageID) {
                        JPLL_MAGIC -> JPLL
                        else -> Unknown(languageID)
                    }
        }

        object JPLL : Language(JPLL_MAGIC)
        data class Unknown(override val languageID: Int) : Language(languageID)
    }

    data class StringOffset(val stringID: Int, val stringOffset: Int) : Comparable<StringOffset> {
        override fun compareTo(other: StringOffset): Int = stringID.compareTo(other.stringID)
    }

    companion object {
        const val MAGIC_NUMBER_LE = 0x54585453
        const val PREFIX = "formats.stx"

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): STXContainer? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("$PREFIX.invalid", dataSource, iae) }

                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): STXContainer = withFormats(context) {
            val notEnoughData: () -> Any = { localise("$PREFIX.not_enough_data") }

            val flow = requireNotNull(dataSource.openInputFlow(), notEnoughData)
            use(flow) {
                val magic = requireNotNull(flow.readInt32LE(), notEnoughData)
                require(magic == MAGIC_NUMBER_LE) { localise("$PREFIX.invalid_magic", magic.toHexString(), MAGIC_NUMBER_LE.toHexString()) }

                val language = Language(requireNotNull(flow.readInt32LE(), notEnoughData))

                val unk = requireNotNull(flow.readInt32LE(), notEnoughData)
                if (unk != 1)
                    debug("$PREFIX.new_unk_value", unk)

                val tableOffset = requireNotNull(flow.readInt32LE(), notEnoughData)

                val unk2 = requireNotNull(flow.readInt32LE(), notEnoughData)
                if (unk2 != 8)
                    debug("$PREFIX.new_unk2_value", unk2)

                val count = requireNotNull(flow.readInt32LE(), notEnoughData)
                if (count == 0)
                    return@use EmptySTXContainer(language)
                require(count > 0) { localise("$PREFIX.invalid_string_count", count) }

                val stringOffsets = flow.fauxSeekFromStart(tableOffset.toULong(), dataSource) { tableFlow ->
                    Array(count) {
                        return@Array StringOffset(
                                requireNotNull(flow.readInt32LE(), notEnoughData), //String ID
                                requireNotNull(flow.readInt32LE(), notEnoughData)  //String Offset
                        )
                    }
                }
                requireNotNull(stringOffsets, notEnoughData)
                val maxStringID = requireNotNull(stringOffsets.max()).stringID

                if (maxStringID >= count) {
                    //We need to use a map
                    debug("$PREFIX.mapped_strings", maxStringID)

                    val strings: MutableMap<Int, String> = HashMap()

                    stringOffsets.sortedBy(StringOffset::stringOffset).forEach { (stringID, stringOffset) ->
                        requireNotNull(flow.fauxSeekFromStart(stringOffset.toULong(), dataSource) { stringFlow ->
                            strings[stringID] = stringFlow.readDoubleByteNullTerminatedString(encoding = TextCharsets.UTF_16LE)
                        })
                    }

                    return@use MapBackedSTXContainer(language, strings)
                } else {
                    val strings = arrayOfNulls<String>(count)

                    stringOffsets.sortedBy(StringOffset::stringOffset).forEach { (stringID, stringOffset) ->
                        requireNotNull(flow.fauxSeekFromStart(stringOffset.toULong(), dataSource) { stringFlow ->
                            strings[stringID] = stringFlow.readDoubleByteNullTerminatedString(encoding = TextCharsets.UTF_16LE)
                        })
                    }

                    return@use ArrayBackedSTXContainer(language, strings.requireNoNulls())
                }
            }
        }
    }

    data class EmptySTXContainer(override val language: Language) : STXContainer() {
        override val count: Int = 0
        override fun get(index: Int): String = throw IndexOutOfBoundsException()
        override fun strings(): Array<String?> = emptyArray()
    }
    class MapBackedSTXContainer(override val language: Language, private val backing: Map<Int, String>) : STXContainer() {
        override val count: Int = backing.size

        override fun get(index: Int): String = backing.getValue(index)
        override fun strings(): Array<String?> = Array(count, backing::get)
    }
    class ArrayBackedSTXContainer(override val language: Language, private val backing: Array<String>) : STXContainer() {
        override val count: Int = backing.size

        override fun get(index: Int): String = backing[index]
        @Suppress("UNCHECKED_CAST")
        override fun strings(): Array<String?> = backing.copyOf() as Array<String?>
    }

    abstract val language: Language

    abstract operator fun get(index: Int): String
    abstract val count: Int

    abstract fun strings(): Array<String?>
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.STXContainer(dataSource: DataSource<*>) = STXContainer(this, dataSource)
@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeSTXContainer(dataSource: DataSource<*>) = STXContainer.unsafe(this, dataSource)