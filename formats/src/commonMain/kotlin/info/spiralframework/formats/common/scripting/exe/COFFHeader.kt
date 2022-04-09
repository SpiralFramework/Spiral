package info.spiralframework.formats.common.scripting.exe

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.useInputFlowForResult
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.withFormats

public data class COFFHeader(
    val machine: MachineType,
    val numberOfSections: Int,
    val timeDateStamp: Int,
    val pointerToSymbolTable: Int,
    val numberOfSymbols: Int,
    val sizeOfOptionalHeader: Int,
    val characteristics: Int
) {
    public enum class MachineType(public val hexValue: Int) {
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

        public companion object {
            public fun valueOf(hexValue: Int): MachineType? = values().firstOrNull { type -> type.hexValue == hexValue }
        }
    }

    public companion object {
        /** Relocation information was stripped from file */
        public const val IMAGE_FILE_RELOCS_STRIPPED: Int = 0x0001

        /** The file is executable */
        public const val IMAGE_FILE_EXECUTABLE_IMAGE: Int = 0x0002

        /** COFF line numbers were stripped from file */
        public const val IMAGE_FILE_LINE_NUMS_STRIPPED: Int = 0x0004

        /** COFF symbol table entries were stripped from file */
        public const val IMAGE_FILE_LOCAL_SYMS_STRIPPED: Int = 0x0008

        /** Aggressively trim the working set (obsolete) */
        public const val IMAGE_FILE_AGGRESSIVE_WS_TRIM: Int = 0x0010

        /** The application can handle addresses greater than 2 GB */
        public const val IMAGE_FILE_LARGE_ADDRESS_AWARE: Int = 0x0020

        /** The bytes of the word are reversed (obsolete) */
        public const val IMAGE_FILE_BYTES_REVERSED_LO: Int = 0x0080

        /** The computer supports 32-bit words */
        public const val IMAGE_FILE_32BIT_MACHINE: Int = 0x0100

        /** Debugging information was removed and stored separately in another file */
        public const val IMAGE_FILE_DEBUG_STRIPPED: Int = 0x0200

        /** If the image is on removable media, copy it to and run it from the swap file */
        public const val IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP: Int = 0x0400

        /** If the image is on the network, copy it to and run it from the swap file */
        public const val IMAGE_FILE_NET_RUN_FROM_SWAP: Int = 0x0800

        /** The image is a system file */
        public const val IMAGE_FILE_SYSTEM: Int = 0x1000

        /** The image is a DLL file */
        public const val IMAGE_FILE_DLL: Int = 0x2000

        /** The image should only be run on a single processor computer */
        public const val IMAGE_FILE_UP_SYSTEM_ONLY: Int = 0x4000

        /** The bytes of the word are reversed (obsolete) */
        public const val IMAGE_FILE_BYTES_REVERSED_HI: Int = 0x8000

        public const val NO_MACHINE: Int = 0x0000

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.exe.coff.not_enough_data"
        public const val NO_MACHINE_KEY: String = "formats.exe.coff.no_machine"

        public suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<COFFHeader> =
            dataSource.useInputFlowForResult { flow -> invoke(context, flow) }

        public suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<COFFHeader> {
            withFormats(context) {
                val machineHexValue = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val machine = MachineType.valueOf(machineHexValue) ?: return KorneaResult.errorAsIllegalArgument(
                    NO_MACHINE,
                    localise(NO_MACHINE_KEY, machineHexValue.toHexString())
                )
                val numberOfSections = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val timeDateStamp = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val pointerToSymbolTable = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val numberOfSymbols = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val sizeOfOptionalHeader = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val characteristics = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                return KorneaResult.success(
                    COFFHeader(
                        machine,
                        numberOfSections,
                        timeDateStamp,
                        pointerToSymbolTable,
                        numberOfSymbols,
                        sizeOfOptionalHeader,
                        characteristics
                    )
                )
            }
        }
    }
}