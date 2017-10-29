package org.abimon.spiral.core.archives

import org.abimon.spiral.core.formats.*
import org.abimon.spiral.core.formats.images.*
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FileDataSource
import java.io.File
import java.io.OutputStream

class CPKArchive(override val archiveFile: File): IArchive {
    val cpk: CPK = CPK(FileDataSource(archiveFile))

    override val archiveType: ArchiveType = ArchiveType.WAD
    override val fileEntries: List<Pair<String, DataSource>> = cpk.fileTable.map { it.name to it }
    override val niceCompileFormats: Map<SpiralFormat, SpiralFormat> = mapOf(
            PNGFormat to TGAFormat,
            JPEGFormat to TGAFormat,
            SHTXFormat to TGAFormat,
            DDSFormat to TGAFormat
    )

    override fun compile(newEntries: List<Pair<String, DataSource>>, outputStream: OutputStream) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}