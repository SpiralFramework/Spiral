package org.abimon.spiral.headless

import org.abimon.external.TGAReader
import org.abimon.spiral.core.*
import org.abimon.visi.io.*
import org.abimon.visi.lang.toArrayString
import java.io.*
import java.nio.charset.Charset
import java.util.*
import java.util.zip.ZipInputStream

fun main(args: Array<String>) {
    println("Initialising SPIRAL Power...")

    val data = File("bustup_01_00.png")
    val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad")
    val backupWadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad copy")
    val tmpWadFile = File(wadFile.absolutePath + ".tmp")
    val wad = WAD(FileDataSource(backupWadFile))
    //wad.extractToDirectory(File("functional"))
    val customWad = customWad {
        major(1)
        minor(1)

        //directory(File("functional"))
        wad(wad)
    }
    customWad.compile(FileOutputStream(wadFile))
    wad.files.filter { file -> file.name.endsWith("dr1_voice_hca_us.awb.06547.ogg") }.first().getInputStream().use { inputStream -> inputStream.writeTo(FileOutputStream(File("dr1_voice_hca_us.awb.06547.ogg"))) }

    val wadIS = FileInputStream(wadFile);
    val backupIS = FileInputStream(backupWadFile)
/**
    wadIS.use {
        backupIS.use {
            val buffer = ByteArray(8192)
            val secondBuffer = ByteArray(8192)

            var read = 0
            var secondRead = 0
            var index = 0
            var secondIndex = 0

            while (read > -1 && secondRead > -1) {
                read = wadIS.read(buffer)
                secondRead = backupIS.read(secondBuffer)

                if (read < 0 && secondRead < 0) {
                    println("Successful!")
                    return
                }
                else if (read < 0 || secondRead < 0) {
                    println("Broke at $index and $secondIndex with invalid reads")
                    return
                }

                if (!Arrays.equals(buffer, secondBuffer)) {
                    println("Broke at $index and $secondIndex with different buffers:\n${buffer.toArrayString()}\n${secondBuffer.toArrayString()}")
                    return
                }

                index += read
                secondIndex += secondRead
            }

            if(!Arrays.equals(buffer, secondBuffer))
                return
        }
    }
*/
    //customWad.compile(FileOutputStream(tmpWadFile))
    //wadFile.delete()
    //tmpWadFile.renameTo(wadFile)
}