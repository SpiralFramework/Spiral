package info.spiralframework.formats.common.scripting.exe

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.WindowedDataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readNumBytes
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats

public open class WindowsExecutable(
    public val dosHeader: DosHeader,
    public val stubProgram: ByteArray,
    public val coffHeader: COFFHeader,
    public val peOptionalHeader: PEOptionalHeader,
    public val imageSectionHeaders: Array<ImageSectionHeader>,
    public val dataSource: DataSource<*>
) {
    public companion object {
        public const val PE_MAGIC_NUMBER_LE: Int = 0x4550

        public const val INVALID_NEW_EXE_HEADER: Int = 0x0000
        public const val INVALID_PE_SIGNATURE: Int = 0x0001

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.exe.not_enough_data"
        public const val INVALID_NEW_EXE_HEADER_KEY: String = "formats.exe.invalid_new_exe_header"
        public const val INVALID_PE_SIGNATURE_KEY: String = "formats.exe.invalid_pe_signature"

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>
        ): KorneaResult<WindowsExecutable> =
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.exe.not_enough_data") }

                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val dosHeader = DosHeader(this, flow).getOrBreak { return@closeAfter it.cast() }

                    if (dosHeader.addressOfNewExeHeader <= 0) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_NEW_EXE_HEADER,
                            localise(INVALID_NEW_EXE_HEADER_KEY, dosHeader.addressOfNewExeHeader)
                        )
                    }

                    val stubProgram = flow.readNumBytes(dosHeader.addressOfNewExeHeader - 0x40)

                    val peSignature =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    if (peSignature != PE_MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_PE_SIGNATURE,
                            localise(INVALID_PE_SIGNATURE_KEY, peSignature, PE_MAGIC_NUMBER_LE)
                        )
                    }

                    val coffHeader = COFFHeader(this, flow).getOrBreak { return@closeAfter it.cast() }

                    val peOptionalHeader = PEOptionalHeader(this, flow).getOrBreak { return@closeAfter it.cast() }

                    val imageSectionHeaders = Array(coffHeader.numberOfSections) {
                        ImageSectionHeader(
                            this,
                            flow
                        ).getOrBreak { return@closeAfter it.cast() }
                    }

                    return@closeAfter KorneaResult.success(
                        WindowsExecutable(
                            dosHeader,
                            stubProgram,
                            coffHeader,
                            peOptionalHeader,
                            imageSectionHeaders,
                            dataSource
                        )
                    )
                }
            }
    }

    public operator fun get(name: String): ImageSectionHeader = imageSectionHeaders.first { section -> section.name == name }
    public fun getOrNull(name: String): ImageSectionHeader? =
        imageSectionHeaders.firstOrNull { section -> section.name == name }

    public fun openSource(sectionHeader: ImageSectionHeader): DataSource<InputFlow> = WindowedDataSource(
        dataSource,
        sectionHeader.pointerToRawData.toULong(),
        sectionHeader.sizeOfRawData.toULong(),
        closeParent = false
    )

    public suspend fun openFlow(sectionHeader: ImageSectionHeader): KorneaResult<InputFlow> =
        dataSource.openInputFlow().map { parent ->
            WindowedInputFlow(
                parent,
                sectionHeader.pointerToRawData.toULong(),
                sectionHeader.sizeOfRawData.toULong()
            )
        }
}

@Suppress("FunctionName")
public suspend fun SpiralContext.WindowsExecutable(dataSource: DataSource<*>): KorneaResult<WindowsExecutable> = WindowsExecutable(this, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeWindowsExecutable(dataSource: DataSource<*>): WindowsExecutable = WindowsExecutable(this, dataSource).getOrThrow()