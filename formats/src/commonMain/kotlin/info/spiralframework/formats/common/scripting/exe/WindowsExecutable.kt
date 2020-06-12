package info.spiralframework.formats.common.scripting.exe

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.archives.PakFileEntry
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.erorrs.common.cast
import org.abimon.kornea.erorrs.common.doOnFailure
import org.abimon.kornea.erorrs.common.map
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.WindowedInputFlow

@ExperimentalUnsignedTypes
open class WindowsExecutable(val dosHeader: DosHeader, val stubProgram: ByteArray, val coffHeader: COFFHeader, val peOptionalHeader: PEOptionalHeader, val imageSectionHeaders: Array<ImageSectionHeader>, val dataSource: DataSource<*>) {
    companion object {
        const val PE_MAGIC_NUMBER_LE = 0x4550

        const val INVALID_NEW_EXE_HEADER = 0x0000
        const val INVALID_PE_SIGNATURE = 0x0001

        const val NOT_ENOUGH_DATA_KEY = "formats.exe.not_enough_data"
        const val INVALID_NEW_EXE_HEADER_KEY = "formats.exe.invalid_new_exe_header"
        const val INVALID_PE_SIGNATURE_KEY = "formats.exe.invalid_pe_signature"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<WindowsExecutable> {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.exe.not_enough_data") }

                val flow = dataSource.openInputFlow().doOnFailure { return it.cast() }

                use(flow) {
                    val dosHeader = DosHeader(this, flow).doOnFailure { return it.cast() }

                    if (dosHeader.addressOfNewExeHeader <= 0) {
                        return KorneaResult.Error(INVALID_NEW_EXE_HEADER, localise(INVALID_NEW_EXE_HEADER_KEY, dosHeader.addressOfNewExeHeader))
                    }

                    val stubProgram = flow.readNumBytes(dosHeader.addressOfNewExeHeader - 0x40)

                    val peSignature = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    if (peSignature != PE_MAGIC_NUMBER_LE) {
                        return KorneaResult.Error(INVALID_PE_SIGNATURE, localise(INVALID_PE_SIGNATURE_KEY, peSignature, PE_MAGIC_NUMBER_LE))
                    }

                    val coffHeader = COFFHeader(this, flow).doOnFailure { return it.cast() }

                    val peOptionalHeader = PEOptionalHeader(this, flow).doOnFailure { return it.cast() }

                    val imageSectionHeaders = Array(coffHeader.numberOfSections) { ImageSectionHeader(this, flow).doOnFailure { return it.cast() } }

                    return KorneaResult.Success(WindowsExecutable(dosHeader, stubProgram, coffHeader, peOptionalHeader, imageSectionHeaders, dataSource))
                }
            }
        }
    }

    operator fun get(name: String): ImageSectionHeader = imageSectionHeaders.first { section -> section.name == name }
    fun getOrNull(name: String): ImageSectionHeader? = imageSectionHeaders.firstOrNull { section -> section.name == name }

    suspend fun openSource(sectionHeader: ImageSectionHeader): DataSource<out InputFlow> = WindowedDataSource(dataSource, sectionHeader.pointerToRawData.toULong(), sectionHeader.sizeOfRawData.toULong(), closeParent = false)
    suspend fun openFlow(sectionHeader: ImageSectionHeader): KorneaResult<InputFlow> =
            dataSource.openInputFlow().map { parent -> WindowedInputFlow(parent, sectionHeader.pointerToRawData.toULong(), sectionHeader.sizeOfRawData.toULong()) }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.WindowsExecutable(dataSource: DataSource<*>) = WindowsExecutable(this, dataSource)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeWindowsExecutable(dataSource: DataSource<*>) = WindowsExecutable(this, dataSource).get()