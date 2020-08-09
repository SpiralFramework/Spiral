package info.spiralframework.formats.common.scripting.exe

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.extensions.*
import dev.brella.kornea.io.common.flow.int
import dev.brella.kornea.io.common.flow.withState
import dev.brella.kornea.toolkit.common.useAndFlatMap

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

        const val INVALID_SIGNATURE = 0x0000
        const val INVALID_SIGNATURE_KEY = "formats.exe.pe_opt.invalid_signature"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<PEOptionalHeader> = dataSource.useInputFlowForResult { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<PEOptionalHeader> {
            withFormats(context) {
                val flow = withState { int(flow) }
                val signature = flow.readInt16LE()
                        ?: return localisedNotEnoughData("formats.exe.pe_opt.not_enough_data")

                when (signature) {
                    PE32_MAGIC_NUMBER_LE -> return PE32(this, flow).cast()
                    PE64_MAGIC_NUMBER_LE -> return PE64(this, flow).cast()
                    else -> throw IllegalArgumentException(localise(INVALID_SIGNATURE_KEY, signature))
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
    ) : PEOptionalHeader() {
        companion object {
            const val NOT_ENOUGH_DATA_KEY = "formats.exe.pe_opt_32.not_enough_data"

            suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<PE32> = dataSource.openInputFlow().useAndFlatMap { flow -> invoke(context, flow) }
            suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<PE32> {
                withFormats(context) {
                    val flow = withState { int(flow) }
                    val majorLinkerVersion = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorLinkerVersion = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfCode = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfInitialisedData = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfUninitialisedData = flow.readInt32LE()
                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val addressOfEntryPoint = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val baseOfCode = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val baseOfData = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val imageBase = flow.readInt32BE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sectionAlignment = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val fileAlignment = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val majorOSVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorOSVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val majorImageVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorImageVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val majorSubsystemVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorSubsystemVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val win32VersionValue = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfImage = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfHeaders = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val checksum = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val subsystem = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val dllCharacteristics = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfStackReserve = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfStackCommit = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfHeapReserve = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfHeapCommit = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val loaderFlags = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val numberOfRvaAndSizes = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val dataDirectory = Array(numberOfRvaAndSizes) {
                        DataDirectory(
                                flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                                flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        )
                    }

                    return KorneaResult.success(PE32(majorLinkerVersion, minorLinkerVersion, sizeOfCode, sizeOfInitialisedData, sizeOfUninitialisedData, addressOfEntryPoint, baseOfCode, baseOfData, imageBase, sectionAlignment, fileAlignment, majorOSVersion, minorOSVersion, majorImageVersion, minorImageVersion, majorSubsystemVersion, minorSubsystemVersion, win32VersionValue, sizeOfImage, sizeOfHeaders, checksum, subsystem, dllCharacteristics, sizeOfStackReserve, sizeOfStackCommit, sizeOfHeapReserve, sizeOfHeapCommit, loaderFlags, numberOfRvaAndSizes, dataDirectory))
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
    ) : PEOptionalHeader() {
        companion object {
            const val NOT_ENOUGH_DATA_KEY = "formats.exe.pe_opt_64.not_enough_data"

            suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<PE64> = dataSource.openInputFlow().useAndFlatMap { flow -> invoke(context, flow) }
            suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<PE64> {
                withFormats(context) {
                    val flow = withState { int(flow) }
                    val majorLinkerVersion = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorLinkerVersion = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfCode = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfInitialisedData = flow.readInt32LE()
                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfUninitialisedData = flow.readInt32LE()
                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val addressOfEntryPoint = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val baseOfCode = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val imageBase = flow.readInt64BE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sectionAlignment = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val fileAlignment = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val majorOSVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorOSVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val majorImageVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorImageVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val majorSubsystemVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val minorSubsystemVersion = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val win32VersionValue = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfImage = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfHeaders = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val checksum = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val subsystem = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val dllCharacteristics = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfStackReserve = flow.readInt64LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfStackCommit = flow.readInt64LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfHeapReserve = flow.readInt64LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sizeOfHeapCommit = flow.readInt64LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val loaderFlags = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val numberOfRvaAndSizes = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val dataDirectory = Array(numberOfRvaAndSizes) {
                        DataDirectory(
                                flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                                flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        )
                    }

                    return KorneaResult.success(PE64(majorLinkerVersion, minorLinkerVersion, sizeOfCode, sizeOfInitialisedData, sizeOfUninitialisedData, addressOfEntryPoint, baseOfCode, imageBase, sectionAlignment, fileAlignment, majorOSVersion, minorOSVersion, majorImageVersion, minorImageVersion, majorSubsystemVersion, minorSubsystemVersion, win32VersionValue, sizeOfImage, sizeOfHeaders, checksum, subsystem, dllCharacteristics, sizeOfStackReserve, sizeOfStackCommit, sizeOfHeapReserve, sizeOfHeapCommit, loaderFlags, numberOfRvaAndSizes, dataDirectory))
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