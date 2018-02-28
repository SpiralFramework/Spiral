package org.abimon.spiral.core.archives

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.images.*
import org.abimon.spiral.core.objects.archives.CustomWAD
import org.abimon.spiral.core.objects.archives.WAD
import org.abimon.spiral.modding.BackupManager
import org.abimon.spiral.modding.data.ModList
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.util.OffsetInputStream
import org.abimon.spiral.util.trace
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readChunked
import org.abimon.visi.lang.make
import java.io.*
import java.util.*

class WADArchive(override val archiveFile: File): IArchive {
    val wad: WAD = WAD { FileInputStream(archiveFile) }

    override val archiveType: ArchiveType = ArchiveType.WAD
    override val fileEntries: List<Pair<String, () -> InputStream>> = wad.files.map { entry -> entry.name to { OffsetInputStream(wad.dataSource(), wad.dataOffset + entry.offset, entry.size) } }
    override val niceCompileFormats: Map<SpiralFormat, SpiralFormat> = mapOf(
            PNGFormat to TGAFormat,
            JPEGFormat to TGAFormat,
            SHTXFormat to TGAFormat,
            DDSFormat to TGAFormat
    )
    override val installedMods: ModList = run {
        val entry = wad.files.firstOrNull { entry -> entry.name == SpiralData.SPIRAL_MOD_LIST } ?: return@run ModList()

        return@run OffsetInputStream(wad.dataSource(), wad.dataOffset + entry.offset, entry.size).use { stream ->
            SpiralData.MAPPER.readValue(stream, ModList::class.java)
        }
    }
    override val supportsCompilation: Boolean = true

    override fun compile(newEntries: List<Pair<String, DataSource>>) {
        BackupManager.backupOverridingEntries(this, newEntries)

        //Check if can patch
        if (newEntries.all { (name, data) -> wad.files.any { entry -> entry.name == name && entry.size == data.size } }) {
            val wadFile = RandomAccessFile(archiveFile, "rw")
            val per = 100.0 / newEntries.size

            newEntries.forEachIndexed { index, (name, data) ->
                if(SpiralModel.printCompilationPercentage)
                    println("Compilation Percentage: ${per * index}%")

                val wadEntry = wad.files.first { entry -> entry.name == name && entry.size == data.size }
                wadFile.seek(wad.dataOffset + wadEntry.offset)
                data.use { stream -> stream.readChunked(processChunk = wadFile::write) }
            }

            wadFile.close()
            trace("Patched!")
        } else {
            var compiled = false

//            run reorganise@{
//                if (wad.files.map { (name) -> name }.containsAll(newEntries.map { (name) -> name })) { //Ensure there's no new files
//                    //If there's no new files, we can reorganise the WAD file based on priority.
//                    //We won't do most of it here, but we do do priority checks here
//
//                    val priorities = wad.spiralPriorityList
//
//                    val highestPriority = priorities.map { (_, priority) -> priority }.max() ?: 0
//                    val lowestPriority = newEntries.map { (name) -> priorities[name] ?: 0 }.min() ?: 0
//
//                    if (lowestPriority == 0 && highestPriority == 0) //All the files are here, but this is a fresh recompile.
//                        return@reorganise
//                    else if (lowestPriority > (highestPriority / 2)) { //Within "acceptable bounds"
////                    val customWad = make<Custom> {
////                        wad(wad)
////                        newEntries.forEach { (name, data) -> this.data(name, data, prioritise = true) }
////                    }
//
//                        val customWad = make<CustomPatchableWAD>(archiveFile) { newEntries.forEach { (name, data) -> this.data(name, data) } }
//                        customWad.patch()
//
//                        compiled = true
//                    } else
//                        return@reorganise
//                }
//            }

            if (!compiled) {
                val customWad = make<CustomWAD> {
                    add(wad)
                    newEntries.forEach { (name, data) -> add(name, data.size, data::inputStream) }
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
    }
}