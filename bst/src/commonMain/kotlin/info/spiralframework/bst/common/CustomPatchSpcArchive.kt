package info.spiralframework.bst.common

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.archives.CustomSpcArchive
import info.spiralframework.formats.common.archives.SpcArchive

public class CustomPatchSpcArchive(public val baseSpc: SpcArchive) : CustomSpcArchive() {
    public suspend fun SpiralContext.addAllBaseFiles() {
        baseSpc.files.forEach { entry ->
            this@CustomPatchSpcArchive[entry.name, entry.compressionFlag, entry.compressedSize, entry.decompressedSize] =
                baseSpc.openRawSource(entry)
        }
    }
}

public suspend fun CustomPatchSpcArchive.addAllBaseFiles(context: SpiralContext): Unit = context.addAllBaseFiles()