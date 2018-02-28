package org.abimon.spiral.core.archives

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.modding.data.ModList
import org.abimon.visi.io.DataSource
import java.io.File
import java.io.InputStream

interface IArchive {
    val archiveType: ArchiveType
    val archiveFile: File

    val fileEntries: List<Pair<String, () -> InputStream>>
    val niceCompileFormats: Map<SpiralFormat, SpiralFormat>
    val installedMods: ModList

    val supportsCompilation: Boolean

    fun compile(newEntries: List<Pair<String, DataSource>>)

    companion object {
        private val archiveExtensions: Map<String, (File) -> IArchive> = mapOf(
                "wad" to ::WADArchive,
                "cpk" to ::CPKArchive
        )
        val EXTENSIONS: Set<String> = archiveExtensions.keys

        operator fun invoke(file: File): IArchive? = if(file.isDirectory) FlatFileArchive(file) else archiveExtensions[file.extension.toLowerCase()]?.invoke(file)
    }
}