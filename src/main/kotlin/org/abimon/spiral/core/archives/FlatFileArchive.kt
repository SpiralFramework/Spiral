package org.abimon.spiral.core.archives

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.archives.SPCFormat
import org.abimon.spiral.core.formats.archives.ZIPFormat
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.io.iterate
import org.abimon.visi.io.relativePathFrom
import java.io.File

/** Note: This is for V3, as the other games probably won't work with this */
class FlatFileArchive(val dir: File): IArchive {
    override val archiveType: ArchiveType = ArchiveType.FLAT_FILE
    override val archiveFile: File = dir
    override val fileEntries: List<Pair<String, DataSource>>
        get() = dir.iterate().map { file -> (file relativePathFrom dir).replace(File.separator, "/") to FileDataSource(file) }
    override val supportsCompilation: Boolean = true

    override val niceCompileFormats: Map<SpiralFormat, SpiralFormat> = mapOf(
            ZIPFormat to SPCFormat //That's... that's it. "Nice" compiling won't really work with V3 for now
    )

    override fun compile(newEntries: List<Pair<String, DataSource>>) {
        for((name, data) in newEntries) {
            val file = File(dir, name)
            file.parentFile.mkdirs()

            file.outputStream().use(data::pipe)
        }
    }
}