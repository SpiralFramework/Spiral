package info.spiralframework.formats.common.scripting.exe

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.archives.PakFileEntry
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.toolkit.common.closeAfter
import info.spiralframework.base.common.io.readNumBytes

@ExperimentalUnsignedTypes
open class WindowsExecutable(val dosHeader: DosHeader, val stubProgram: ByteArray, val coffHeader: COFFHeader, val peOptionalHeader: PEOptionalHeader, val imageSectionHeaders: Array<ImageSectionHeader>, val dataSource: DataSource<*>) {
    companion object {
        const val PE_MAGIC_NUMBER_LE = 0x4550

        const val INVALID_NEW_EXE_HEADER = 0x0000
        const val INVALID_PE_SIGNATURE = 0x0001

        const val NOT_ENOUGH_DATA_KEY = "formats.exe.not_enough_data"
        const val INVALID_NEW_EXE_HEADER_KEY = "formats.exe.invalid_new_exe_header"
        const val INVALID_PE_SIGNATURE_KEY = "formats.exe.invalid_pe_signature"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<WindowsExecutable> =
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.exe.not_enough_data") }

                val flow = dataSource.openInputFlow()
                    .mapWithState(InputFlowStateSelector::int)
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val dosHeader = DosHeader(this, flow).getOrBreak { return@closeAfter it.cast() }

                    if (dosHeader.addressOfNewExeHeader <= 0) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_NEW_EXE_HEADER, localise(INVALID_NEW_EXE_HEADER_KEY, dosHeader.addressOfNewExeHeader))
                    }

                    val stubProgram = flow.readNumBytes(dosHeader.addressOfNewExeHeader - 0x40)

                    val peSignature = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    if (peSignature != PE_MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_PE_SIGNATURE, localise(INVALID_PE_SIGNATURE_KEY, peSignature, PE_MAGIC_NUMBER_LE))
                    }

                    val coffHeader = COFFHeader(this, flow).getOrBreak { return@closeAfter it.cast() }

                    val peOptionalHeader = PEOptionalHeader(this, flow).getOrBreak { return@closeAfter it.cast() }

                    val imageSectionHeaders = Array(coffHeader.numberOfSections) { ImageSectionHeader(this, flow).getOrBreak { return@closeAfter it.cast() } }

                    return@closeAfter KorneaResult.success(WindowsExecutable(dosHeader, stubProgram, coffHeader, peOptionalHeader, imageSectionHeaders, dataSource))
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