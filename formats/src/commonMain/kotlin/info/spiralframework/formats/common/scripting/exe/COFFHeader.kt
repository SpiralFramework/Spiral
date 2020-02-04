package info.spiralframework.formats.common.scripting.exe

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.useInputFlow

@ExperimentalUnsignedTypes
data class COFFHeader(val machine: MachineType, val numberOfSections: Int, val timeDateStamp: Int, val pointerToSymbolTable: Int, val numberOfSymbols: Int, val sizeOfOptionalHeader: Int, val characteristics: Int) {
    enum class MachineType(val hexValue: Int) {
        INTEL_386(0x14C),
        X64(0x8664),
        MIPS_R3000(0x162),
        MIPS_R10000(0x168),
        MIPS_LITTLE_ENDIAN_MCI_V2(0x169),
        OLD_ALPHA_AXP(0x183),
        HITACHI_SH3(0x1A2),
        HITACHI_SH3_DSP(0x1A3),
        HITACHI_SH4(0x1A6),
        HITACHI_SH5(0x1A8),
        ARM_LITTLE_ENDIAN(0x1C0),
        THUMB(0x1C2),
        ARM_V7(0x1C4),
        MATSUSHITA_AM33(0x1D3),
        POWERPC_LITTLE_ENDIAN(0x1F0),
        POWERPC_FLOATING_POINT_SUPPORT(0x1F1),
        INTEL_IA64(0x200),
        MIPS16(0x266),
        MOTOROLA_68000(0x268),
        ALPHA_AXP_64BIT(0x284),
        MIPS_WITH_FPU(0x366),
        MIPS16_WITH_FPU(0x466),
        EFI_BYTE_CODE(0xEBC),
        AMD_AMD64(0x8664),
        MITSUBISHI_M32R_LITTLE_ENDIAN(0x9041),
        ARM64_LITTLE_ENDIAN(0xAA64),
        CLR_PURE_MSIL(0xC0EE);

        companion object {
            fun valueOf(hexValue: Int): MachineType? = values().firstOrNull { type -> type.hexValue == hexValue }
        }
    }

    companion object {
        /** Relocation information was stripped from file */
        const val IMAGE_FILE_RELOCS_STRIPPED = 0x0001
        /** The file is executable */
        const val IMAGE_FILE_EXECUTABLE_IMAGE = 0x0002
        /** COFF line numbers were stripped from file */
        const val IMAGE_FILE_LINE_NUMS_STRIPPED = 0x0004
        /** COFF symbol table entries were stripped from file */
        const val IMAGE_FILE_LOCAL_SYMS_STRIPPED = 0x0008
        /** Aggressively trim the working set (obsolete) */
        const val IMAGE_FILE_AGGRESSIVE_WS_TRIM = 0x0010
        /** The application can handle addresses greater than 2 GB */
        const val IMAGE_FILE_LARGE_ADDRESS_AWARE = 0x0020
        /** The bytes of the word are reversed (obsolete) */
        const val IMAGE_FILE_BYTES_REVERSED_LO = 0x0080
        /** The computer supports 32-bit words */
        const val IMAGE_FILE_32BIT_MACHINE = 0x0100
        /** Debugging information was removed and stored separately in another file */
        const val IMAGE_FILE_DEBUG_STRIPPED = 0x0200
        /** If the image is on removable media, copy it to and run it from the swap file */
        const val IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP = 0x0400
        /** If the image is on the network, copy it to and run it from the swap file */
        const val IMAGE_FILE_NET_RUN_FROM_SWAP = 0x0800
        /** The image is a system file */
        const val IMAGE_FILE_SYSTEM = 0x1000
        /** The image is a DLL file */
        const val IMAGE_FILE_DLL = 0x2000
        /** The image should only be run on a single processor computer */
        const val IMAGE_FILE_UP_SYSTEM_ONLY = 0x4000
        /** The bytes of the word are reversed (obsolete) */
        const val IMAGE_FILE_BYTES_REVERSED_HI = 0x8000

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): COFFHeader? = dataSource.useInputFlow { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): COFFHeader? {
            try {
                return unsafe(context, flow)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.exe.coff.invalid", flow, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): COFFHeader = requireNotNull(dataSource.useInputFlow { flow -> unsafe(context, flow) })
        suspend fun unsafe(context: SpiralContext, flow: InputFlow): COFFHeader {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.exe.coff.not_enough_data") }

                val machineHexValue = requireNotNull(flow.readInt16LE(), notEnoughData)
                val machine = requireNotNull(MachineType.valueOf(machineHexValue)) { localise("formats.exe.coff.no_machine", machineHexValue) }
                val numberOfSections = requireNotNull(flow.readInt16LE(), notEnoughData)
                val timeDateStamp = requireNotNull(flow.readInt32LE(), notEnoughData)
                val pointerToSymbolTable = requireNotNull(flow.readInt32LE(), notEnoughData)
                val numberOfSymbols = requireNotNull(flow.readInt32LE(), notEnoughData)
                val sizeOfOptionalHeader = requireNotNull(flow.readInt16LE(), notEnoughData)
                val characteristics = requireNotNull(flow.readInt16LE(), notEnoughData)

                return COFFHeader(machine, numberOfSections, timeDateStamp, pointerToSymbolTable, numberOfSymbols, sizeOfOptionalHeader, characteristics)
            }
        }
    }
}