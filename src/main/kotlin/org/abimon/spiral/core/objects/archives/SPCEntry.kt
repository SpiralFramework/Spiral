package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.objects.compression.SPCCompression
import org.abimon.spiral.core.utils.CacheFile
import org.abimon.spiral.core.utils.WindowedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

data class SPCEntry(val compressionFlag: Int, val unknownFlag: Int, val compressedSize: Long, val decompressedSize: Long, val name: String, val offset: Long, val spc: SPC) {
    private val decompressedData: File by lazy {
        val cacheFile = CacheFile()
        WindowedInputStream(spc.dataSource(), offset, compressedSize).use { baseStream ->
            FileOutputStream(cacheFile).use { outStream ->
                SPCCompression.decompressToPipe(compressionFlag, baseStream, outStream)
            }
        }

        return@lazy cacheFile
    }

    val inputStream: InputStream
        get() = FileInputStream(decompressedData)
}