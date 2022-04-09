package info.spiralframework.formats.common.scripting.exe

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.extensions.readAsciiString
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.useInputFlowForResult
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats

public data class ImageSectionHeader(
    val name: String,
    val virtualSize: Int,
    val virtualAddress: Int,
    val sizeOfRawData: Int,
    val pointerToRawData: Int,
    val pointerToRelocations: Int,
    val pointerToLineNumbers: Int,
    val numberOfRelocations: Int,
    val numberOfLineNumbers: Int,
    val characteristics: Int
) {
    public companion object {
        public const val IMAGE_SCN_TYPE_NO_PAD: Int = 0x00000008
        public const val IMAGE_SCN_CNT_CODE: Int = 0x00000020
        public const val IMAGE_SCN_CNT_INITIALIZED_DATA: Int = 0x00000040
        public const val IMAGE_SCN_CNT_UNINITIALIZED_DATA: Int = 0x00000080
        public const val IMAGE_SCN_LNK_OTHER: Int = 0x00000100
        public const val IMAGE_SCN_LNK_INFO: Int = 0x00000200
        public const val IMAGE_SCN_LNK_REMOVE: Int = 0x00000800
        public const val IMAGE_SCN_LNK_COMDAT: Int = 0x00001000
        public const val IMAGE_SCN_NO_DEFER_SPEC_EXC: Int = 0x00004000
        public const val IMAGE_SCN_GPREL: Int = 0x00008000
        public const val IMAGE_SCN_MEM_PURGEABLE: Int = 0x00020000
        public const val IMAGE_SCN_MEM_LOCKED: Int = 0x00040000
        public const val IMAGE_SCN_MEM_PRELOAD: Int = 0x00080000
        public const val IMAGE_SCN_ALIGN_1BYTES: Int = 0x00100000
        public const val IMAGE_SCN_ALIGN_2BYTES: Int = 0x00200000
        public const val IMAGE_SCN_ALIGN_4BYTES: Int = 0x00300000
        public const val IMAGE_SCN_ALIGN_8BYTES: Int = 0x00400000
        public const val IMAGE_SCN_ALIGN_16BYTES: Int = 0x00500000
        public const val IMAGE_SCN_ALIGN_32BYTES: Int = 0x00600000
        public const val IMAGE_SCN_ALIGN_64BYTES: Int = 0x00700000
        public const val IMAGE_SCN_ALIGN_128BYTES: Int = 0x00800000
        public const val IMAGE_SCN_ALIGN_256BYTES: Int = 0x00900000
        public const val IMAGE_SCN_ALIGN_512BYTES: Int = 0x00A00000
        public const val IMAGE_SCN_ALIGN_1024BYTES: Int = 0x00B00000
        public const val IMAGE_SCN_ALIGN_2048BYTES: Int = 0x00C00000
        public const val IMAGE_SCN_ALIGN_4096BYTES: Int = 0x00D00000
        public const val IMAGE_SCN_ALIGN_8192BYTES: Int = 0x00E00000
        public const val IMAGE_SCN_LNK_NRELOC_OVFL: Int = 0x01000000
        public const val IMAGE_SCN_MEM_DISCARDABLE: Int = 0x02000000
        public const val IMAGE_SCN_MEM_NOT_CACHED: Int = 0x04000000
        public const val IMAGE_SCN_MEM_NOT_PAGED: Int = 0x08000000
        public const val IMAGE_SCN_MEM_SHARED: Int = 0x10000000
        public const val IMAGE_SCN_MEM_EXECUTE: Int = 0x20000000
        public const val IMAGE_SCN_MEM_READ: Int = 0x40000000
        public const val IMAGE_SCN_MEM_WRITE: Long = 0x80000000

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.exe.image_section.not_enough_data"

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>
        ): KorneaResult<ImageSectionHeader> = dataSource.useInputFlowForResult { flow -> invoke(context, flow) }

        public suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<ImageSectionHeader> {
            withFormats(context) {
                val name = flow.readAsciiString(8)?.trim() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
//                val physicalAddress =
                val virtualSize = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val virtualAddress = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val sizeOfRawData = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val pointerToRawData = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val pointerToRelocations = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val pointerToLineNumbers = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val numberOfRelocations = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val numberOfLineNumbers = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val characteristics = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                return KorneaResult.success(
                    ImageSectionHeader(
                        name,
                        virtualSize,
                        virtualAddress,
                        sizeOfRawData,
                        pointerToRawData,
                        pointerToRelocations,
                        pointerToLineNumbers,
                        numberOfRelocations,
                        numberOfLineNumbers,
                        characteristics
                    )
                )
            }
        }
    }
}