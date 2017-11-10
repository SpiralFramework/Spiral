package org.abimon.spiral.core.archives

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.images.*
import org.abimon.spiral.core.objects.archives.CustomWAD
import org.abimon.spiral.core.objects.archives.WAD
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.lang.make
import java.io.File
import java.io.FileOutputStream
import java.util.*

class WADArchive(override val archiveFile: File): IArchive {
    val wad: WAD = WAD(FileDataSource(archiveFile))

    override val archiveType: ArchiveType = ArchiveType.WAD
    override val fileEntries: List<Pair<String, DataSource>> = wad.files.map { it.name to it }
    override val niceCompileFormats: Map<SpiralFormat, SpiralFormat> = mapOf(
            PNGFormat to TGAFormat,
            JPEGFormat to TGAFormat,
            SHTXFormat to TGAFormat,
            DDSFormat to TGAFormat
    )
    override val supportsCompilation: Boolean = true

    override fun compile(newEntries: List<Pair<String, DataSource>>) {
        //check if can patch
        val customWad = make<CustomWAD> {
            wad(wad)
            newEntries.forEach { (name, data) -> this.data(name, data) }
        }

        val tmp = File.createTempFile(UUID.randomUUID().toString(), ".wad")
        tmp.deleteOnExit()

        try {
            FileOutputStream(tmp).use(customWad::compile)
            archiveFile.delete()
            tmp.renameTo(archiveFile)
        } finally {
            tmp.delete()
        }
    }
}