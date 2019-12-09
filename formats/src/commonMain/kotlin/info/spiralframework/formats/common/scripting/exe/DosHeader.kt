package info.spiralframework.formats.common.scripting.exe

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.formats.common.withFormats

@ExperimentalUnsignedTypes
data class DosHeader(
        val usedBytesInTheLastPage: Int,
        val fileSizeInPages: Int,
        val numberOfRelocationItems: Int,
        val headerSizeInParagraphs: Int,
        val minimumExtraParagraphs: Int,
        val maximumExtraParagraphs: Int,
        val initialRelativeSS: Int,
        val initialSP: Int,
        val checksum: Int,
        val initialIP: Int,
        val initialRelativeCS: Int,
        val addressOfRelocationTable: Int,
        val overlayNumber: Int,
        val reserved: IntArray,
        val oemID: Int,
        val oemInfo: Int,
        val reserved2: IntArray,
        val addressOfNewExeHeader: Int
) {
    companion object {
        val MAGIC_NUMBER_LE = 0x5A4D

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): DosHeader? = dataSource.useInputFlow { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): DosHeader? {
            try {
                return unsafe(context, flow)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.exe.dos.invalid", flow, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): DosHeader = requireNotNull(dataSource.useInputFlow { flow -> unsafe(context, flow) })
        suspend fun unsafe(context: SpiralContext, flow: InputFlow): DosHeader {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.exe.dos.not_enough_data") }

                val mzSignature = requireNotNull(flow.readInt16LE(), notEnoughData)
                require(mzSignature == MAGIC_NUMBER_LE) { localise("formats.exe.dos.invalid_signature", mzSignature, MAGIC_NUMBER_LE) }

                val usedBytesInTheLastPage = requireNotNull(flow.readInt16LE(), notEnoughData)
                val fileSizeInPages = requireNotNull(flow.readInt16LE(), notEnoughData)
                val numberOfRelocationItems = requireNotNull(flow.readInt16LE(), notEnoughData)
                val headerSizeInParagraphs = requireNotNull(flow.readInt16LE(), notEnoughData)
                val minimumExtraParagraphs = requireNotNull(flow.readInt16LE(), notEnoughData)
                val maximumExtraParagraphs = requireNotNull(flow.readInt16LE(), notEnoughData)
                val initialRelativeSS = requireNotNull(flow.readInt16LE(), notEnoughData)
                val initialSP = requireNotNull(flow.readInt16LE(), notEnoughData)
                val checksum = requireNotNull(flow.readInt16LE(), notEnoughData)
                val initialIP = requireNotNull(flow.readInt16LE(), notEnoughData)
                val initialRelativeCS = requireNotNull(flow.readInt16LE(), notEnoughData)
                val addressOfRelationTable = requireNotNull(flow.readInt16LE(), notEnoughData)
                val overlayNumber = requireNotNull(flow.readInt16LE(), notEnoughData)
                val reserved = IntArray(4) { requireNotNull(flow.readInt16LE(), notEnoughData) }
                val oemID = requireNotNull(flow.readInt16LE(), notEnoughData)
                val oemInfo = requireNotNull(flow.readInt16LE(), notEnoughData)
                val reserved2 = IntArray(10) { requireNotNull(flow.readInt16LE(), notEnoughData) }
                val addressOfNewExeHeader = requireNotNull(flow.readInt32LE(), notEnoughData)

                return DosHeader(usedBytesInTheLastPage, fileSizeInPages, numberOfRelocationItems, headerSizeInParagraphs, minimumExtraParagraphs, maximumExtraParagraphs, initialRelativeSS, initialSP, checksum, initialIP, initialRelativeCS, addressOfRelationTable, overlayNumber, reserved, oemID, oemInfo, reserved2, addressOfNewExeHeader)
            }
        }
    }
}