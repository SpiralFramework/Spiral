package org.abimon.spiral.core.archives

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.images.*
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.spiral.modding.data.ModList
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FileDataSource
import java.io.File

class CPKArchive(override val archiveFile: File): IArchive {
    val cpk: CPK = CPK(FileDataSource(archiveFile))

    override val archiveType: ArchiveType = ArchiveType.CPK
    override val fileEntries: List<Pair<String, DataSource>> = cpk.fileTable.map { it.name to it }
    override val supportsCompilation: Boolean = false
    override val installedMods: ModList = run {
        val entry = cpk.fileTable.firstOrNull { entry -> entry.name == SpiralData.SPIRAL_MOD_LIST } ?: return@run ModList()

        return@run SpiralData.MAPPER.readValue(entry.inputStream, ModList::class.java)
    }

    override val niceCompileFormats: Map<SpiralFormat, SpiralFormat> = mapOf(
            PNGFormat to TGAFormat,
            JPEGFormat to TGAFormat,
            SHTXFormat to TGAFormat,
            DDSFormat to TGAFormat
    )

    override fun compile(newEntries: List<Pair<String, DataSource>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}