package org.abimon.spiral.core.archives

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.images.*
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.spiral.modding.data.ModList
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class CPKArchive(override val archiveFile: File): IArchive {
    val cpk: CPK = CPK { FileInputStream(archiveFile) }

    override val archiveType: ArchiveType = ArchiveType.CPK
    override val fileEntries: List<Pair<String, () -> InputStream>> = cpk.files.map { file -> "${file.directoryName}/${file.fileName}" to file::inputStream }
    override val supportsCompilation: Boolean = false
    override val installedMods: ModList = run {
        val entry = cpk.files.firstOrNull { entry -> entry.fileName == SpiralData.SPIRAL_MOD_LIST } ?: return@run ModList()

        return@run SpiralData.MAPPER.readValue(entry.inputStream, ModList::class.java)
    }

    override val niceCompileFormats: Map<SpiralFormat, SpiralFormat> = mapOf(
            PNGFormat to TGAFormat,
            JPEGFormat to TGAFormat,
            SHTXFormat to TGAFormat,
            DDSFormat to TGAFormat
    )

    override fun compile(newEntries: List<Pair<String, () -> InputStream>>) {
        TODO("not implemented")
    }
}