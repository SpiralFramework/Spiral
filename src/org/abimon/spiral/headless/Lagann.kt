package org.abimon.spiral.headless

import org.abimon.spiral.core.*
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.io.writeTo
import org.abimon.visi.lang.SteamProtocol
import org.abimon.visi.lang.time
import java.io.File
import java.io.FileOutputStream

fun main(args: Array<String>) {
    println("Initialising SPIRAL Power...")

    //restore()
    patch()
    compare()

    //patch()

    val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad")
    val wad = WAD(FileDataSource(wadFile))
    wad.files.filter { (name) -> name.endsWith("bustup_00_00.tga") }.first().getInputStream().use { inputStream -> inputStream.writeTo(FileOutputStream(File("Test.tga"))) }

//    wad.files.filter { file -> file.name.endsWith("dr1_voice_hca_us.awb.06547.ogg") }.first().getInputStream().use { inputStream -> inputStream.writeTo(FileOutputStream(File("dr1_voice_hca_us.awb.06547.ogg"))) }
//
//    val wadIS = FileInputStream(wadFile);
//    val backupIS = FileInputStream(backupWadFile)

    SteamProtocol.openGame(STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC)
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

fun restore() {
    val time = time {
        val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad")
        val backupWadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad copy")

        val backupWad = WAD(FileDataSource(backupWadFile))

        val customWad = customWad {
            major(1)
            minor(1)

            wad(backupWad)
        }
        customWad.compile(FileOutputStream(wadFile))
    }
    println("Took $time ms")

    Thread.sleep(1000)
}

fun compare() {
    val time = time {
        val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad")
        val backupWadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad copy")

        val wad = WAD(FileDataSource(wadFile))
        val backupWad = WAD(FileDataSource(backupWadFile))

        println(wad.directories.count())
        println(backupWad.directories.count())

        println(wad.directories.flatMap(WADFileDirectory::subfiles).count())
        println(backupWad.directories.flatMap(WADFileDirectory::subfiles).count())

        println(wad.files.count())
        println(backupWad.files.count())

        wad.spiralHeader.ifPresent { header -> println(String(header)) }

//        wad.files
//                .filter { (name) -> backupWad.files.any { (backupName) -> backupName == name } }
//                .map { file -> Pair(file, backupWad.files.first { (name) -> name == file.name }) }
//                .filter { (file, backup) -> file.offset != backup.offset }
//                .forEach { (original, backup) ->
//                    println("${original.name}'s offset is not equal (${original.offset} ≠ ${backup.offset})")
//                }
//
//        wad.files
//                .filter { (name) -> backupWad.files.any { (backupName) -> backupName == name } }
//                .map { file -> Pair(file, backupWad.files.first { (name) -> name == file.name }) }
//                .filter { (file, backup) -> file.size != backup.size }
//                .forEach { (original, backup) ->
//                    println("${original.name}'s size is not equal (${original.size} ≠ ${backup.size})")
//                }
//
//        wad.directories
//                .filter { (name) -> backupWad.files.any { (backupName) -> backupName == name } }
//                .map { dir -> Pair(dir, backupWad.directories.first{ (name) -> name == dir.name }) }
//                .filter { (dir, backup) -> dir.subFiles.count() != backup.subFiles.count() }
//                .forEach { (dir, backup) ->
//                    println("${dir.name} has an inconsistent subfile count (${dir.subFiles.count()} ≠ ${backup.subFiles.count()}")
//                }

        if(wad.dataOffset != backupWad.dataOffset)
            println("Data offsets are not equal (${wad.dataOffset} ≠ ${backupWad.dataOffset})")
    }
    println("Took $time ms")

    Thread.sleep(1000)
}

fun patch() {
    val patchTime = time {
        val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad")
        val backupWadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad copy")
        val wad = WAD(FileDataSource(backupWadFile))
        //wad.extractToDirectory(File("functional"))
        val customWad = customWad {
            major(11037)
            minor(1)

            headerFile(File("/Users/undermybrella/Bee Movie Script.txt").readBytes())

            wad(wad)
            data("Dr1/data/all/cg/bustup_00_00.tga", SpiralFormats.convert(SpiralFormats.PNG, SpiralFormats.TGA, FileDataSource(File("Hajime.png"))))
            data("Dr1/data/all/cg/bustup_00_01.tga", SpiralFormats.convert(SpiralFormats.PNG, SpiralFormats.TGA, FileDataSource(File("HajimeAirGuitar.png"))))
        }
        customWad.compile(FileOutputStream(wadFile))
    }
    println("Patching took $patchTime ms")
}