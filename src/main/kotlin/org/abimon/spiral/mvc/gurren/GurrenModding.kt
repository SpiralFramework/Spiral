package org.abimon.spiral.mvc.gurren

import org.abimon.spiral.core.archives.IArchive
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.spiral.mvc.SpiralModel.confirm
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.question
import java.io.File
import java.util.zip.ZipFile

object GurrenModding {
    val operatingArchive: IArchive
        get() = IArchive(SpiralModel.operating ?: throw IllegalStateException("Attempt to get the archive while operating is null, this is a bug!")) ?: throw IllegalStateException("Attempts to create an archive return null, this is a bug!")
    val operatingName: String
        get() = SpiralModel.operating?.nameWithoutExtension ?: ""


    val prepareV3 = SpiralModel.Command("prepare_v3", "default") { (params) ->

    }

    val restoreFromBackup = Command("restore_from_backup", "modding") { (_) ->
        val backupFile = File((SpiralModel.operating ?: return@Command errPrintln("Error: SpiralModel#operating is null, this is a bug!")).absolutePath.replaceAfterLast('.', ".zip"))
        val backupZip = ZipFile(backupFile)

        val archive = operatingArchive
        val archiveFiles = archive.fileEntries.map { (name) -> name }
        val backupEntries = backupZip.entries().toList().filter { zipEntry -> zipEntry.name in archiveFiles }.map { zipEntry ->
            val (out, ds) = CacheHandler.cacheStream()

            backupZip.getInputStream(zipEntry).use { stream -> out.use { outStream -> stream.copyTo(outStream) } }

            return@map zipEntry.name to ds
        }

        val proceed = confirm {

            return@confirm question("[$operatingName] Proceed with restoration (Y/n)? ", "Y")
        }

        archive.compile(backupEntries)

        println("Restored ${backupEntries.size} from backup")
    }

    val modArchive = Command("mod", "default") { (params) ->
        if (SpiralModel.archives.isEmpty())
            return@Command errPrintln("Error: No archives registered")
        if (params.size > 1) {
            for (i in 1 until params.size) {
                val archiveName = params[i]
                val archive = SpiralModel.archives.firstOrNull { file -> file.nameWithoutExtension == archiveName || file.absolutePath == archiveName }
                if (archive == null)
                    println("Invalid archive $archiveName")
                else {
                    SpiralModel.operating = archive
                    SpiralModel.scope = "[Modding ${archive.nameWithoutExtension}]|> " to "mod"
                    println("Now modding ${archive.nameWithoutExtension}")

                    return@Command
                }
            }
        }

        println("Select an archive to mod")
        println(SpiralModel.archives.joinToPrefixedString("\n", "\t") { "$nameWithoutExtension ($absolutePath)" })
        while (true) {
            print("[mod] > ")
            val archiveName = readLine() ?: break
            if (archiveName == "exit")
                break

            val archive = SpiralModel.archives.firstOrNull { file -> file.nameWithoutExtension == archiveName || file.absolutePath == archiveName }
            if (archive == null)
                println("Invalid archive $archiveName")
            else {
                SpiralModel.operating = archive
                SpiralModel.scope = "[Modding ${archive.nameWithoutExtension}]|> " to "mod"
                println("Now modding ${archive.nameWithoutExtension}")

                break
            }
        }
    }
}