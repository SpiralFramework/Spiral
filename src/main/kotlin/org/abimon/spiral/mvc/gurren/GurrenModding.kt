package org.abimon.spiral.mvc.gurren

import org.abimon.spiral.core.archives.IArchive
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.errPrintln

object GurrenModding {
    val operatingArchive: IArchive
        get() = IArchive(SpiralModel.operating ?: throw IllegalStateException("Attempt to get the archive while operating is null, this is a bug!")) ?: throw IllegalStateException("Attempts to create an archive return null, this is a bug!")
    val operatingName: String
        get() = SpiralModel.operating?.nameWithoutExtension ?: ""


    val prepareV3 = SpiralModel.Command("prepare_v3", "default") { (params) ->

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