package org.abimon.spiral.core.archives

import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.images.*
import org.abimon.spiral.core.objects.UnsafeWAD
import org.abimon.spiral.core.objects.archives.WAD
import org.abimon.spiral.core.objects.customWAD
import org.abimon.spiral.core.utils.DelegatedInputStream
import org.abimon.spiral.core.utils.WindowedInputStream
import org.abimon.spiral.modding.BackupManager
import org.abimon.spiral.modding.data.ModList
import org.abimon.spiral.mvc.gurren.Gurren
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class WADArchive(override val archiveFile: File) : IArchive {
    val openStreams = AtomicInteger(0)
    val openedStreams = AtomicInteger(0)

    val totalReadOperations = AtomicInteger(0)
    val totalReadBytes = AtomicInteger(0)

    val wad: WAD = UnsafeWAD {
        openStreams.incrementAndGet()
        openedStreams.incrementAndGet()

        try {
            return@UnsafeWAD object : DelegatedInputStream(FileInputStream(archiveFile)) {
                override fun close() {
                    super.close()
                    openStreams.decrementAndGet()
                }

                override fun read(): Int {
                    totalReadOperations.incrementAndGet()
                    val read = super.read()
                    if (read != -1)
                        totalReadBytes.incrementAndGet()
                    return read
                }

                override fun read(b: ByteArray): Int {
                    val readBytes = super.read(b)
                    totalReadOperations.incrementAndGet()
                    if (readBytes != -1)
                        totalReadBytes.addAndGet(readBytes)
                    return readBytes
                }

                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    val readBytes = super.read(b, off, len)
                    totalReadOperations.incrementAndGet()
                    if (readBytes != -1)
                        totalReadBytes.addAndGet(readBytes)
                    return readBytes
                }
            }
        } catch (fnf: FileNotFoundException) {
            throw fnf
        }
    }

    override val archiveType: ArchiveType = ArchiveType.WAD
    override val fileEntries: List<Pair<String, () -> InputStream>> = wad.files.map { entry -> entry.name to { WindowedInputStream(wad.dataSource(), wad.dataOffset + entry.offset, entry.size) } }
    override val niceCompileFormats: Map<SpiralFormat, SpiralFormat> = mapOf(
            PNGFormat to TGAFormat,
            JPEGFormat to TGAFormat,
            SHTXFormat to TGAFormat,
            DDSFormat to TGAFormat
    )
    override val installedMods: ModList = run {
        val entry = wad.files.firstOrNull { entry -> entry.name == SpiralData.SPIRAL_MOD_LIST } ?: return@run ModList()

        return@run WindowedInputStream(wad.dataSource(), wad.dataOffset + entry.offset, entry.size).use { stream ->
            SpiralData.MAPPER.readValue(stream, ModList::class.java)
        }
    }
    override val supportsCompilation: Boolean = true

    override fun compile(newEntries: List<Pair<String, () -> InputStream>>) {
        BackupManager.backupOverridingEntries(this, newEntries)

        val cacheFiles = ArrayList<File>()
        val customWad = customWAD {
            add(wad)
            newEntries.forEach { (name, data) ->
                val cacheFile = CacheHandler.newCacheFile()
                data().use { dataStream -> FileOutputStream(cacheFile).use { outStream -> dataStream.copyTo(outStream) } }
                add(name, cacheFile)
            }
        }

        val tmp = CacheHandler.newCacheFile()

        try {
            FileOutputStream(tmp).use(customWad::compile)
            Files.copy(tmp.toPath(), archiveFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } finally {
            tmp.delete()
            cacheFiles.forEach { file -> file.delete() }
        }
    }

    override fun clear() {
        val customWad = customWAD {
            val info = "Compiled with SPIRAL v${Gurren.version}".toByteArray(Charsets.UTF_8)
            add("Info.txt", info.size.toLong(), info::inputStream)
        }

        FileOutputStream(archiveFile).use(customWad::compile)
    }
}