package info.spiralframework.formats.common.scripting.exe

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readAsciiString
import info.spiralframework.base.common.trimNulls
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.useInputFlow

@ExperimentalUnsignedTypes
data class ImageSectionHeader(val name: String, val virtualSize: Int, val virtualAddress: Int, val sizeOfRawData: Int, val pointerToRawData: Int, val pointerToRelocations: Int, val pointerToLineNumbers: Int, val numberOfRelocations: Int, val numberOfLineNumbers: Int, val characteristics: Int) {
    companion object {
        const val IMAGE_SCN_TYPE_NO_PAD             = 0x00000008
        const val IMAGE_SCN_CNT_CODE                = 0x00000020
        const val IMAGE_SCN_CNT_INITIALIZED_DATA    = 0x00000040
        const val IMAGE_SCN_CNT_UNINITIALIZED_DATA  = 0x00000080
        const val IMAGE_SCN_LNK_OTHER               = 0x00000100
        const val IMAGE_SCN_LNK_INFO                = 0x00000200
        const val IMAGE_SCN_LNK_REMOVE              = 0x00000800
        const val IMAGE_SCN_LNK_COMDAT              = 0x00001000
        const val IMAGE_SCN_NO_DEFER_SPEC_EXC       = 0x00004000
        const val IMAGE_SCN_GPREL                   = 0x00008000
        const val IMAGE_SCN_MEM_PURGEABLE           = 0x00020000
        const val IMAGE_SCN_MEM_LOCKED              = 0x00040000
        const val IMAGE_SCN_MEM_PRELOAD             = 0x00080000
        const val IMAGE_SCN_ALIGN_1BYTES            = 0x00100000
        const val IMAGE_SCN_ALIGN_2BYTES            = 0x00200000
        const val IMAGE_SCN_ALIGN_4BYTES            = 0x00300000
        const val IMAGE_SCN_ALIGN_8BYTES            = 0x00400000
        const val IMAGE_SCN_ALIGN_16BYTES           = 0x00500000
        const val IMAGE_SCN_ALIGN_32BYTES           = 0x00600000
        const val IMAGE_SCN_ALIGN_64BYTES           = 0x00700000
        const val IMAGE_SCN_ALIGN_128BYTES          = 0x00800000
        const val IMAGE_SCN_ALIGN_256BYTES          = 0x00900000
        const val IMAGE_SCN_ALIGN_512BYTES          = 0x00A00000
        const val IMAGE_SCN_ALIGN_1024BYTES         = 0x00B00000
        const val IMAGE_SCN_ALIGN_2048BYTES         = 0x00C00000
        const val IMAGE_SCN_ALIGN_4096BYTES         = 0x00D00000
        const val IMAGE_SCN_ALIGN_8192BYTES         = 0x00E00000
        const val IMAGE_SCN_LNK_NRELOC_OVFL         = 0x01000000
        const val IMAGE_SCN_MEM_DISCARDABLE         = 0x02000000
        const val IMAGE_SCN_MEM_NOT_CACHED          = 0x04000000
        const val IMAGE_SCN_MEM_NOT_PAGED           = 0x08000000
        const val IMAGE_SCN_MEM_SHARED              = 0x10000000
        const val IMAGE_SCN_MEM_EXECUTE             = 0x20000000
        const val IMAGE_SCN_MEM_READ                = 0x40000000
        const val IMAGE_SCN_MEM_WRITE               = 0x80000000

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): ImageSectionHeader? = dataSource.useInputFlow { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): ImageSectionHeader? {
            try {
                return unsafe(context, flow)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.exe.pe_opt_64.invalid", flow, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): ImageSectionHeader = requireNotNull(dataSource.useInputFlow { flow -> unsafe(context, flow) })
        suspend fun unsafe(context: SpiralContext, flow: InputFlow): ImageSectionHeader {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.exe.image_section.not_enough_data") }

                val name = requireNotNull(flow.readAsciiString(8), notEnoughData).trimNulls()
//                val physicalAddress =
                val virtualSize = requireNotNull(flow.readInt32LE(), notEnoughData)
                val virtualAddress = requireNotNull(flow.readInt32LE(), notEnoughData)
                val sizeOfRawData = requireNotNull(flow.readInt32LE(), notEnoughData)
                val pointerToRawData = requireNotNull(flow.readInt32LE(), notEnoughData)
                val pointerToRelocations = requireNotNull(flow.readInt32LE(), notEnoughData)
                val pointerToLineNumbers = requireNotNull(flow.readInt32LE(), notEnoughData)
                val numberOfRelocations = requireNotNull(flow.readInt16LE(), notEnoughData)
                val numberOfLineNumbers = requireNotNull(flow.readInt16LE(), notEnoughData)
                val characteristics = requireNotNull(flow.readInt32LE(), notEnoughData)
                return ImageSectionHeader(name, virtualSize, virtualAddress, sizeOfRawData, pointerToRawData, pointerToRelocations, pointerToLineNumbers, numberOfRelocations, numberOfLineNumbers, characteristics)
            }
        }
    }
}