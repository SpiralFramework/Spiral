package info.spiralframework.formats.common.scripting.exe

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.formats.common.withFormats

@ExperimentalUnsignedTypes
sealed class PEOptionalHeader {
    data class DataDirectory(val virtualAddress: Int, val size: Int)
    companion object {
        const val IMAGE_NT_OPTIONAL_HDR32_MAGIC = 0x10B
        const val IMAGE_NT_OPTIONAL_HDR64_MAGIC = 0x20B
        const val IMAGE_ROM_OPTIONAL_HDR_MAGIC = 0x107

        const val IMAGE_SUBSYSTEM_UNKNOWN = 0
        const val IMAGE_SUBSYSTEM_NATIVE = 1
        const val IMAGE_SUBSYSTEM_WINDOWS_GUI = 2
        const val IMAGE_SUBSYSTEM_WINDOWS_CUI = 3
        const val IMAGE_SUBSYSTEM_OS2_CUI = 5
        const val IMAGE_SUBSYSTEM_POSIX_CUI = 7
        const val IMAGE_SUBSYSTEM_WINDOWS_CE_GUI = 9
        const val IMAGE_SUBSYSTEM_EFI_APPLICATION = 10
        const val IMAGE_SUBSYSTEM_EFI_BOOT_SERVICE_DRIVER = 11
        const val IMAGE_SUBSYSTEM_EFI_RUNTIME_DRIVER = 12
        const val IMAGE_SUBSYSTEM_EFI_ROM = 13
        const val IMAGE_SUBSYSTEM_XBOX = 14
        const val IMAGE_SUBSYSTEM_WINDOWS_BOOT_APPLICATION = 16

        const val IMAGE_DLLCHARACTERISTICS_DYNAMIC_BASE = 0x0040
        const val IMAGE_DLLCHARACTERISTICS_FORCE_INTEGRITY = 0x0080
        const val IMAGE_DLLCHARACTERISTICS_NX_COMPAT = 0x0100
        const val IMAGE_DLLCHARACTERISTICS_NO_ISOLATION = 0x0200
        const val IMAGE_DLLCHARACTERISTICS_NO_SEH = 0x0400
        const val IMAGE_DLLCHARACTERISTICS_NO_BIND = 0x0800
        const val IMAGE_DLLCHARACTERISTICS_APPCONTAINER = 0x1000
        const val IMAGE_DLLCHARACTERISTICS_WDM_DRIVER = 0x2000
        const val IMAGE_DLLCHARACTERISTICS_TERMINAL_SERVER_AWARE = 0x8000

        const val PE32_MAGIC_NUMBER_LE = 0x10B
        const val PE64_MAGIC_NUMBER_LE = 0x20B

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): PEOptionalHeader? = dataSource.useInputFlow { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): PEOptionalHeader? {
            try {
                return unsafe(context, flow)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.exe.pe_opt_64.invalid", flow, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): PEOptionalHeader = requireNotNull(dataSource.useInputFlow { flow -> unsafe(context, flow) })
        suspend fun unsafe(context: SpiralContext, flow: InputFlow): PEOptionalHeader {
            withFormats(context) {
                val signature = requireNotNull(flow.readInt16LE()) { localise("formats.exe.pe_opt.not_enough_data") }

                when (signature) {
                    PE32_MAGIC_NUMBER_LE -> return PE32.unsafe(this, flow)
                    PE64_MAGIC_NUMBER_LE -> return PE64.unsafe(this, flow)
                    else -> throw IllegalArgumentException(localise("formats.exe.pe_opt.invalid_signature", signature))
                }
            }
        }
    }

    data class PE32(
            val majorLinkerVersion: Int,
            val minorLinkerVersion: Int,
            val sizeOfCode: Int,
            val sizeOfInitialisedData: Int,
            val sizeOfUninitialisedData: Int,
            val addressOfEntryPoint: Int,
            val baseOfCode: Int,
            val baseOfData: Int,
            val imageBase: Int,
            val sectionAlignment: Int,
            val fileAlignment: Int,
            val majorOSVersion: Int,
            val minorOSVersion: Int,
            val majorImageVersion: Int,
            val minorImageVersion: Int,
            val majorSubsystemVersion: Int,
            val minorSubsystemVersion: Int,
            val win32VersionValue: Int,
            val sizeOfImage: Int,
            val sizeOfHeaders: Int,
            val checksum: Int,
            val subsystem: Int,
            val dllCharacteristics: Int,
            val sizeOfStackReserve: Int,
            val sizeOfStackCommit: Int,
            val sizeOfHeapReserve: Int,
            val sizeOfHeapCommit: Int,
            val loaderFlags: Int,
            val numberOfRvaAndSizes: Int,
            val dataDirectory: Array<DataDirectory>
    ): PEOptionalHeader() {
        companion object {
            suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): PE32? = dataSource.useInputFlow { flow -> invoke(context, flow) }
            suspend operator fun invoke(context: SpiralContext, flow: InputFlow): PE32? {
                try {
                    return unsafe(context, flow)
                } catch (iae: IllegalArgumentException) {
                    withFormats(context) { debug("formats.exe.pe_opt_32.invalid", flow, iae) }

                    return null
                }
            }

            suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): PE32 = requireNotNull(dataSource.useInputFlow { flow -> unsafe(context, flow) })
            suspend fun unsafe(context: SpiralContext, flow: InputFlow): PE32 {
                withFormats(context) {
                    val notEnoughData: () -> Any = { localise("formats.exe.pe_opt_32.not_enough_data") }

                    val majorLinkerVersion = requireNotNull(flow.read(), notEnoughData)
                    val minorLinkerVersion = requireNotNull(flow.read(), notEnoughData)
                    val sizeOfCode = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfInitialisedData = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfUninitialisedData = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val addressOfEntryPoint = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val baseOfCode = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val baseOfData = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val imageBase = requireNotNull(flow.readInt32BE(), notEnoughData)
                    val sectionAlignment = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val fileAlignment = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val majorOSVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val minorOSVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val majorImageVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val minorImageVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val majorSubsystemVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val minorSubsystemVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val win32VersionValue = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfImage = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfHeaders = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val checksum = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val subsystem = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val dllCharacteristics = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val sizeOfStackReserve = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfStackCommit = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfHeapReserve = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfHeapCommit = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val loaderFlags = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val numberOfRvaAndSizes = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val dataDirectory = Array(numberOfRvaAndSizes) {
                        DataDirectory(requireNotNull(flow.readInt32LE(), notEnoughData), requireNotNull(flow.readInt32LE(), notEnoughData))
                    }

                    return PE32(majorLinkerVersion, minorLinkerVersion, sizeOfCode, sizeOfInitialisedData, sizeOfUninitialisedData, addressOfEntryPoint, baseOfCode, baseOfData, imageBase, sectionAlignment, fileAlignment, majorOSVersion, minorOSVersion, majorImageVersion, minorImageVersion, majorSubsystemVersion, minorSubsystemVersion, win32VersionValue, sizeOfImage, sizeOfHeaders, checksum, subsystem, dllCharacteristics, sizeOfStackReserve, sizeOfStackCommit, sizeOfHeapReserve, sizeOfHeapCommit, loaderFlags, numberOfRvaAndSizes, dataDirectory)
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as PE32

            if (majorLinkerVersion != other.majorLinkerVersion) return false
            if (minorLinkerVersion != other.minorLinkerVersion) return false
            if (sizeOfCode != other.sizeOfCode) return false
            if (sizeOfInitialisedData != other.sizeOfInitialisedData) return false
            if (sizeOfUninitialisedData != other.sizeOfUninitialisedData) return false
            if (addressOfEntryPoint != other.addressOfEntryPoint) return false
            if (baseOfCode != other.baseOfCode) return false
            if (imageBase != other.imageBase) return false
            if (sectionAlignment != other.sectionAlignment) return false
            if (fileAlignment != other.fileAlignment) return false
            if (majorOSVersion != other.majorOSVersion) return false
            if (minorOSVersion != other.minorOSVersion) return false
            if (majorImageVersion != other.majorImageVersion) return false
            if (minorImageVersion != other.minorImageVersion) return false
            if (majorSubsystemVersion != other.majorSubsystemVersion) return false
            if (minorSubsystemVersion != other.minorSubsystemVersion) return false
            if (win32VersionValue != other.win32VersionValue) return false
            if (sizeOfImage != other.sizeOfImage) return false
            if (sizeOfHeaders != other.sizeOfHeaders) return false
            if (checksum != other.checksum) return false
            if (subsystem != other.subsystem) return false
            if (dllCharacteristics != other.dllCharacteristics) return false
            if (sizeOfStackReserve != other.sizeOfStackReserve) return false
            if (sizeOfStackCommit != other.sizeOfStackCommit) return false
            if (sizeOfHeapReserve != other.sizeOfHeapReserve) return false
            if (sizeOfHeapCommit != other.sizeOfHeapCommit) return false
            if (loaderFlags != other.loaderFlags) return false
            if (numberOfRvaAndSizes != other.numberOfRvaAndSizes) return false
            if (!dataDirectory.contentEquals(other.dataDirectory)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = majorLinkerVersion
            result = 31 * result + minorLinkerVersion
            result = 31 * result + sizeOfCode
            result = 31 * result + sizeOfInitialisedData
            result = 31 * result + sizeOfUninitialisedData
            result = 31 * result + addressOfEntryPoint
            result = 31 * result + baseOfCode
            result = 31 * result + imageBase.hashCode()
            result = 31 * result + sectionAlignment
            result = 31 * result + fileAlignment
            result = 31 * result + majorOSVersion
            result = 31 * result + minorOSVersion
            result = 31 * result + majorImageVersion
            result = 31 * result + minorImageVersion
            result = 31 * result + majorSubsystemVersion
            result = 31 * result + minorSubsystemVersion
            result = 31 * result + win32VersionValue
            result = 31 * result + sizeOfImage
            result = 31 * result + sizeOfHeaders
            result = 31 * result + checksum
            result = 31 * result + subsystem
            result = 31 * result + dllCharacteristics
            result = 31 * result + sizeOfStackReserve.hashCode()
            result = 31 * result + sizeOfStackCommit.hashCode()
            result = 31 * result + sizeOfHeapReserve.hashCode()
            result = 31 * result + sizeOfHeapCommit.hashCode()
            result = 31 * result + loaderFlags
            result = 31 * result + numberOfRvaAndSizes
            result = 31 * result + dataDirectory.contentHashCode()
            return result
        }
    }

    data class PE64(
            val majorLinkerVersion: Int,
            val minorLinkerVersion: Int,
            val sizeOfCode: Int,
            val sizeOfInitialisedData: Int,
            val sizeOfUninitialisedData: Int,
            val addressOfEntryPoint: Int,
            val baseOfCode: Int,
            val imageBase: Long,
            val sectionAlignment: Int,
            val fileAlignment: Int,
            val majorOSVersion: Int,
            val minorOSVersion: Int,
            val majorImageVersion: Int,
            val minorImageVersion: Int,
            val majorSubsystemVersion: Int,
            val minorSubsystemVersion: Int,
            val win32VersionValue: Int,
            val sizeOfImage: Int,
            val sizeOfHeaders: Int,
            val checksum: Int,
            val subsystem: Int,
            val dllCharacteristics: Int,
            val sizeOfStackReserve: Long,
            val sizeOfStackCommit: Long,
            val sizeOfHeapReserve: Long,
            val sizeOfHeapCommit: Long,
            val loaderFlags: Int,
            val numberOfRvaAndSizes: Int,
            val dataDirectory: Array<DataDirectory>
    ): PEOptionalHeader() {
        companion object {
            suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): PE64? = dataSource.useInputFlow { flow -> invoke(context, flow) }
            suspend operator fun invoke(context: SpiralContext, flow: InputFlow): PE64? {
                try {
                    return unsafe(context, flow)
                } catch (iae: IllegalArgumentException) {
                    withFormats(context) { debug("formats.exe.pe_opt_64.invalid", flow, iae) }

                    return null
                }
            }

            suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): PE64 = requireNotNull(dataSource.useInputFlow { flow -> unsafe(context, flow) })
            suspend fun unsafe(context: SpiralContext, flow: InputFlow): PE64 {
                withFormats(context) {
                    val notEnoughData: () -> Any = { localise("formats.exe.pe_opt_64.not_enough_data") }

                    val majorLinkerVersion = requireNotNull(flow.read(), notEnoughData)
                    val minorLinkerVersion = requireNotNull(flow.read(), notEnoughData)
                    val sizeOfCode = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfInitialisedData = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfUninitialisedData = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val addressOfEntryPoint = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val baseOfCode = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val imageBase = requireNotNull(flow.readInt64BE(), notEnoughData)
                    val sectionAlignment = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val fileAlignment = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val majorOSVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val minorOSVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val majorImageVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val minorImageVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val majorSubsystemVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val minorSubsystemVersion = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val win32VersionValue = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfImage = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val sizeOfHeaders = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val checksum = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val subsystem = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val dllCharacteristics = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val sizeOfStackReserve = requireNotNull(flow.readInt64LE(), notEnoughData)
                    val sizeOfStackCommit = requireNotNull(flow.readInt64LE(), notEnoughData)
                    val sizeOfHeapReserve = requireNotNull(flow.readInt64LE(), notEnoughData)
                    val sizeOfHeapCommit = requireNotNull(flow.readInt64LE(), notEnoughData)
                    val loaderFlags = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val numberOfRvaAndSizes = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val dataDirectory = Array(numberOfRvaAndSizes) {
                        DataDirectory(requireNotNull(flow.readInt32LE(), notEnoughData), requireNotNull(flow.readInt32LE(), notEnoughData))
                    }

                    return PE64(majorLinkerVersion, minorLinkerVersion, sizeOfCode, sizeOfInitialisedData, sizeOfUninitialisedData, addressOfEntryPoint, baseOfCode, imageBase, sectionAlignment, fileAlignment, majorOSVersion, minorOSVersion, majorImageVersion, minorImageVersion, majorSubsystemVersion, minorSubsystemVersion, win32VersionValue, sizeOfImage, sizeOfHeaders, checksum, subsystem, dllCharacteristics, sizeOfStackReserve, sizeOfStackCommit, sizeOfHeapReserve, sizeOfHeapCommit, loaderFlags, numberOfRvaAndSizes, dataDirectory)
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as PE64

            if (majorLinkerVersion != other.majorLinkerVersion) return false
            if (minorLinkerVersion != other.minorLinkerVersion) return false
            if (sizeOfCode != other.sizeOfCode) return false
            if (sizeOfInitialisedData != other.sizeOfInitialisedData) return false
            if (sizeOfUninitialisedData != other.sizeOfUninitialisedData) return false
            if (addressOfEntryPoint != other.addressOfEntryPoint) return false
            if (baseOfCode != other.baseOfCode) return false
            if (imageBase != other.imageBase) return false
            if (sectionAlignment != other.sectionAlignment) return false
            if (fileAlignment != other.fileAlignment) return false
            if (majorOSVersion != other.majorOSVersion) return false
            if (minorOSVersion != other.minorOSVersion) return false
            if (majorImageVersion != other.majorImageVersion) return false
            if (minorImageVersion != other.minorImageVersion) return false
            if (majorSubsystemVersion != other.majorSubsystemVersion) return false
            if (minorSubsystemVersion != other.minorSubsystemVersion) return false
            if (win32VersionValue != other.win32VersionValue) return false
            if (sizeOfImage != other.sizeOfImage) return false
            if (sizeOfHeaders != other.sizeOfHeaders) return false
            if (checksum != other.checksum) return false
            if (subsystem != other.subsystem) return false
            if (dllCharacteristics != other.dllCharacteristics) return false
            if (sizeOfStackReserve != other.sizeOfStackReserve) return false
            if (sizeOfStackCommit != other.sizeOfStackCommit) return false
            if (sizeOfHeapReserve != other.sizeOfHeapReserve) return false
            if (sizeOfHeapCommit != other.sizeOfHeapCommit) return false
            if (loaderFlags != other.loaderFlags) return false
            if (numberOfRvaAndSizes != other.numberOfRvaAndSizes) return false
            if (!dataDirectory.contentEquals(other.dataDirectory)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = majorLinkerVersion
            result = 31 * result + minorLinkerVersion
            result = 31 * result + sizeOfCode
            result = 31 * result + sizeOfInitialisedData
            result = 31 * result + sizeOfUninitialisedData
            result = 31 * result + addressOfEntryPoint
            result = 31 * result + baseOfCode
            result = 31 * result + imageBase.hashCode()
            result = 31 * result + sectionAlignment
            result = 31 * result + fileAlignment
            result = 31 * result + majorOSVersion
            result = 31 * result + minorOSVersion
            result = 31 * result + majorImageVersion
            result = 31 * result + minorImageVersion
            result = 31 * result + majorSubsystemVersion
            result = 31 * result + minorSubsystemVersion
            result = 31 * result + win32VersionValue
            result = 31 * result + sizeOfImage
            result = 31 * result + sizeOfHeaders
            result = 31 * result + checksum
            result = 31 * result + subsystem
            result = 31 * result + dllCharacteristics
            result = 31 * result + sizeOfStackReserve.hashCode()
            result = 31 * result + sizeOfStackCommit.hashCode()
            result = 31 * result + sizeOfHeapReserve.hashCode()
            result = 31 * result + sizeOfHeapCommit.hashCode()
            result = 31 * result + loaderFlags
            result = 31 * result + numberOfRvaAndSizes
            result = 31 * result + dataDirectory.contentHashCode()
            return result
        }
    }
}