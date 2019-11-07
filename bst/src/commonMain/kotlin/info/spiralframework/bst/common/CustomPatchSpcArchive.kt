package info.spiralframework.bst.common

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.archives.CustomSpcArchive
import info.spiralframework.formats.common.archives.SpcArchive
import info.spiralframework.formats.common.archives.openRawSource

@ExperimentalUnsignedTypes
class CustomPatchSpcArchive(val baseSpc: SpcArchive): CustomSpcArchive() {
    suspend fun SpiralContext.addAllBaseFiles() {
        baseSpc.files.forEach { entry ->
            this@CustomPatchSpcArchive[entry.name, entry.compressionFlag, entry.compressedSize, entry.decompressedSize] = baseSpc.openRawSource(this, entry)
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun CustomPatchSpcArchive.addAllBaseFiles(context: SpiralContext) = context.addAllBaseFiles()