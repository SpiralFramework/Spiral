package info.spiralframework.formats.common.scripting.exe

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.archives.PakFileEntry
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.WindowedInputFlow

@ExperimentalUnsignedTypes
open class WindowsExecutable(val dosHeader: DosHeader, val stubProgram: ByteArray, val coffHeader: COFFHeader, val peOptionalHeader: PEOptionalHeader, val imageSectionHeaders: Array<ImageSectionHeader>, val dataSource: DataSource<*>) {
    companion object {
        const val PE_MAGIC_NUMBER_LE = 0x4550

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): WindowsExecutable? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.exe.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): WindowsExecutable {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.exe.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val dosHeader = DosHeader.unsafe(this, flow)

                    require(dosHeader.addressOfNewExeHeader != 0) { localise("formats.exe.invalid_new_exe_header", dosHeader.addressOfNewExeHeader) }

                    val stubProgram = flow.readNumBytes(dosHeader.addressOfNewExeHeader - 0x40)

                    val peSignature = requireNotNull(flow.readInt32LE(), notEnoughData)

                    require(peSignature == PE_MAGIC_NUMBER_LE) { localise("formats.exe.invalid_pe_signature", peSignature, PE_MAGIC_NUMBER_LE) }

                    val coffHeader = COFFHeader.unsafe(this, flow)

                    val peOptionalHeader = PEOptionalHeader.unsafe(this, flow)

                    val imageSectionHeaders = Array(coffHeader.numberOfSections) { ImageSectionHeader.unsafe(this, flow) }

                    return WindowsExecutable(dosHeader, stubProgram, coffHeader, peOptionalHeader, imageSectionHeaders, dataSource)
                }
            }
        }
    }

    operator fun get(name: String): ImageSectionHeader = imageSectionHeaders.first { section -> section.name == name }
    fun getOrNull(name: String): ImageSectionHeader? = imageSectionHeaders.firstOrNull { section -> section.name == name }

    suspend fun openSource(sectionHeader: ImageSectionHeader): DataSource<out InputFlow> = WindowedDataSource(dataSource, sectionHeader.pointerToRawData.toULong(), sectionHeader.sizeOfRawData.toULong(), closeParent = false)
    suspend fun openFlow(sectionHeader: ImageSectionHeader): InputFlow? {
        val parent = dataSource.openInputFlow() ?: return null
        return WindowedInputFlow(parent, sectionHeader.pointerToRawData.toULong(), sectionHeader.sizeOfRawData.toULong())
    }
}