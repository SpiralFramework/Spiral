package info.spiralframework.formats.common.scripting.exe

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.base.common.useAndFlatMap
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.useInputFlow

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

        const val INVALID_SIGNATURE = 0x0000

        const val NOT_ENOUGH_DATA_KEY = "formats.exe.dos.not_enough_data"
        const val INVALID_SIGNATURE_KEY = "formats.exe.dos.invalid_signature"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<DosHeader> = dataSource.openInputFlow().useAndFlatMap { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<DosHeader> {
            withFormats(context) {
                val mzSignature = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                if (mzSignature != MAGIC_NUMBER_LE) {
                    return KorneaResult.Error(INVALID_SIGNATURE, localise(INVALID_SIGNATURE_KEY, mzSignature.toHexString(), MAGIC_NUMBER_LE.toHexString()))
                }

                val usedBytesInTheLastPage = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val fileSizeInPages = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val numberOfRelocationItems = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val headerSizeInParagraphs = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val minimumExtraParagraphs = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val maximumExtraParagraphs = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val initialRelativeSS = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val initialSP = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val checksum = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val initialIP = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val initialRelativeCS = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val addressOfRelationTable = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val overlayNumber = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val reserved = IntArray(4) { flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) }
                val oemID = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val oemInfo = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val reserved2 = IntArray(10) {
                    flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                }
                val addressOfNewExeHeader = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                return KorneaResult.Success(DosHeader(usedBytesInTheLastPage, fileSizeInPages, numberOfRelocationItems, headerSizeInParagraphs, minimumExtraParagraphs, maximumExtraParagraphs, initialRelativeSS, initialSP, checksum, initialIP, initialRelativeCS, addressOfRelationTable, overlayNumber, reserved, oemID, oemInfo, reserved2, addressOfNewExeHeader))
            }
        }
    }
}