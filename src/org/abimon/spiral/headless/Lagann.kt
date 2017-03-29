package org.abimon.spiral.headless

import org.abimon.spiral.core.*
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.*
import org.abimon.visi.lang.*
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.util.*

fun main(args: Array<String>) {
    setHeadless()
    println("Initialising SPIRAL Power...")

    val os = EnumOS.determineOS()
    val wads = HashSet<File>()
    headless@while(true) {
        Thread.sleep(100)
        println("What would you like to do?")
        print("> ")
        val operation = readLine()?.splitOutsideGroup("\\s+") ?: break
        when(operation[0].toLowerCase()) {
            "help" -> {
                println("SPIRAL v0.1.0 - Headless")
                println("Help")
                println("\tCommands can be run just by entering the command, and parameters are supplied between speech marks")
                println("\tFor instance: command \"Parameter 1\" \"Parameter 2\"")
                println("Commands")
                println("\thelp - Display this message")
                println("\tfind - Tries to find the WAD files for DR1/2, if they're in their normal locations")
                println("\tlocate [directory] - Searches a directory for all WAD files. Note: Will take a fair time, also will require confirmation for each")
                println("\tregister [wad] - Register an individual WAD file.")
                println("\tregistered - Displays the registered WAD files")
                println("\toperate - Operate on a WAD file (Extract files, list, etc)")
                println("\texit - Exits the program")
            }

            "find" -> {
                when(os) {
                    EnumOS.WINDOWS -> {
                        for(root in File.listRoots()) {
                            for(programFolder in arrayOf(File(root, "Program Files (x86)"), File(root, "Program Files"))) {
                                val steamFolder = File(programFolder, "Steam")
                                if(steamFolder.exists()) {
                                    val common = File(steamFolder, "steamapps${File.separator}common")
                                    for(game in common.listFiles { file -> file.isDirectory && file.name.contains("Danganronpa") })
                                        wads.addAll(game.listFiles { file -> file.isFile && file.extension == "wad" && !file.name.contains(".backup") })
                                }
                            }
                        }
                    }
                    EnumOS.MACOSX -> {
                        val steamFolder = os.getStorageLocation("Steam")
                        if(steamFolder.exists()) {
                            val common = File(steamFolder, "steamapps${File.separator}common")
                            for(game in common.listFiles { file -> file.isDirectory && file.name.contains("Danganronpa") }) {
                                val appDirs = game.listFiles { file -> file.isDirectory && file.extension == "app" }
                                if (appDirs.isNotEmpty())
                                    wads.addAll(
                                            appDirs.flatMap<File, File> { app ->
                                                File(app, "Contents${File.separator}Resources").listFiles { file ->
                                                    file.isFile && file.extension == "wad" && !file.name.contains(".backup")
                                                }.toList()
                                            }
                                    )
                            }
                        }
                    }
                    else -> println("No behaviour defined for $os!")
                }

                if(wads.isEmpty())
                    errPrintln("Error: No WAD files detected! You can manually add them via the register command, or by running the locate command!")
                else
                    println("WADs: ${wads.joinToPrefixedString("", "\n\t")}")
            }
            "locate" -> {
                if(operation.size == 1) {
                    errPrintln("Error: No directory provided!")
                    continue@headless
                }

                val dir = File(operation.copyFrom(1).joinToString(" "))
                if(!dir.exists()) {
                    errPrintln("Error: $dir does not exist!")
                    continue@headless
                }

                if(question("Warning: This operation will take quite some time. Do you wish to proceed to scan $dir (Y/N)? ", "Y")) {
                    val time = time {
                        dir.iterate(filters = arrayOf(
                                FileFilter { file -> !file.name.startsWith(".") },
                                FileFilter { file -> file.isDirectory || (file.isFile && file.extension == "wad" && !file.name.contains(".backup")) }
                        )).forEach { wad ->
                            if(question("WAD Found ($wad). Would you like to add this to the internal registry (Y/N)? ", "Y"))
                                wads.add(wad)
                        }
                    }
                    println("Took $time ms.")
                    if(wads.isEmpty())
                        errPrintln("Error: No WAD files detected! You can manually add them via the register command, or by running the locate command!")
                    else
                        println(wads.joinToString("\n"))
                }
            }

            "register" -> {
                if(operation.size == 1) {
                    errPrintln("Error: No file provided!")
                    continue@headless
                }

                val wad = File(operation.copyFrom(1).joinToString(" "))
                if(!wad.exists()) {
                    errPrintln("Error: $wad does not exist!")
                    continue@headless
                }

                if(!wad.isFile) {
                    errPrintln("Error: $wad is not a file!")
                    continue@headless
                }

                if(wad.extension != "wad") {
                    errPrintln("Error: $wad is not a .wad file!")
                    continue@headless
                }

                wads.add(wad)
                println("Registered $wad!")
            }
            "registered" -> println("Registered WADs: ${wads.joinToPrefixedString("", "\n\t")}")

            "operate" -> {
                println("Select a WAD file to operate on (or type exit to return to the previous menu)")
                println(wads.joinToPrefixedString("\n", "\t") { "$nameWithoutExtension ($absolutePath)" })
                print("> ")
                var wad = readLine() ?: break@headless
                if(wad == "exit")
                    continue@headless
                while(wads.none { file -> file.nameWithoutExtension == wad || file.name == wad || file.absolutePath.endsWith(wad) }) {
                    println("$wad is an invalid WAD file")
                    print("> ")
                    wad = readLine() ?: break@headless
                    if(wad == "exit")
                        continue@headless
                }

                val wadFile = WAD(FileDataSource(wads.first { file -> file.nameWithoutExtension == wad || file.name == wad || file.absolutePath.endsWith(wad) }))
                println("Now operating on $wad")

                operate@while(true) {
                    try {
                        Thread.sleep(100)
                        println("[$wad] What would you like to do?")
                        print("[$wad] > ")
                        val wadOperation = readLine()?.splitOutsideGroup("\\s+") ?: break

                        when (wadOperation[0].toLowerCase()) {
                            "help" -> {
                                println("[$wad] WAD Operations")
                                println("[$wad] Help")
                                println("[$wad] Help")
                                println("[$wad]\tCommands can be run just by entering the command, and parameters are supplied between speech marks")
                                println("[$wad]\tFor instance: command \"Parameter 1\" \"Parameter 2\"")
                                println("[$wad] Commands")
                                println("[$wad]\thelp - Display this message")
                                println("[$wad]\tlist [regex] - List all files that match the provided regex")
                                println("[$wad]\textract [dir] [regex] - Extract all files that match the provided regex to the provided directory")
                                println("[$wad]\textrat_nicely [dir] [regex] - List all files that match the provided regex to the provided directory, and convert them to nicer formats")
                                println("[$wad]\texit - Return to the previous menu")
                            }

                            "list" -> {
                                val pattern = (if (wadOperation.size == 1) ".*" else wadOperation.copyFrom(1).joinToString(" "))
                                if(pattern.isRegex()) {
                                    val regex = pattern.toRegex()
                                    println("[$wad] Files in $wad that match the regex $regex:")
                                    wadFile.files.filter { (name) -> name.matches(regex) }.forEach { (name, size, offset) ->
                                        println("[$wad] $name ($size bytes, $offset bytes from the start)")
                                    }
                                }
                                else {
                                    val semiRegex = pattern.replace(".", "\\.").replace("*", ".*")
                                    if(semiRegex.isRegex()) {
                                        val regex = semiRegex.toRegex()
                                        println("[$wad] Files in $wad that match the regex $regex:")
                                        wadFile.files.filter { (name) -> name.matches(regex) }.forEach { (name, size, offset) ->
                                            println("[$wad] $name ($size bytes, $offset bytes from the start)")
                                        }
                                    }
                                    else {
                                        println("[$wad] Files in $wad that end with $pattern:")
                                        wadFile.files.filter { (name) -> name.endsWith(pattern) }.forEach { (name, size, offset) ->
                                            println("[$wad] $name ($size bytes, $offset bytes from the start)")
                                        }
                                    }
                                }
                            }
                            "extract" -> {
                                if(wadOperation.size == 1) {
                                    errPrintln("[$wad] Error: No directory path provided!")
                                    continue@operate
                                }

                                val dir = File(wadOperation[1])
                                if(!dir.exists()) {
                                    errPrintln("[$wad] $dir does not exist, creating...")
                                    if(!dir.mkdirs()) {
                                        println("[$wad] An error occurred while creating $dir")
                                        continue@operate
                                    }
                                }
                                if(!dir.isDirectory) {
                                    errPrintln("[$wad] Error: $dir is not a directory!")
                                    continue@operate
                                }

                                val pattern = (if (wadOperation.size == 2) ".*" else wadOperation.copyFrom(2).joinToString(" "))
                                val extracting = ArrayList<WADFile>()
                                if(pattern.isRegex()) {
                                    val regex = pattern.toRegex()
                                    println("[$wad] Extracting files in $wad to $dir that match the regex $regex:")
                                    extracting.addAll(wadFile.files.filter { (name) -> name.matches(regex) })
                                }
                                else {
                                    val semiRegex = pattern.replace(".", "\\.").replace("*", ".*")
                                    if(semiRegex.isRegex()) {
                                        val regex = semiRegex.toRegex()
                                        println("[$wad] Extracting files in $wad to $dir that match the regex $regex:")
                                        extracting.addAll(wadFile.files.filter { (name) -> name.matches(regex) })
                                    }
                                    else {
                                        println("[$wad] Extracting files in $wad to $dir that end in $pattern:")
                                        extracting.addAll(wadFile.files.filter { (name) -> name.endsWith(pattern) })
                                    }
                                }

                                val totalTime = time {
                                    extracting.forEach files@ { file ->
                                        val parentDirs = File(dir, file.name.getParents())
                                        if (!parentDirs.exists()) {
                                            if (!parentDirs.mkdirs()) {
                                                errPrintln("[$wad] An error occurred while creating $parentDirs")
                                                return@files
                                            }
                                        }

                                        val extractLocation = File(dir, file.name)
                                        val time = time {
                                            val fileOutput = FileOutputStream(extractLocation)
                                            file.getInputStream().writeTo(fileOutput, closeAfter = true)
                                            fileOutput.close()
                                        }

                                        println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms")
                                    }
                                }
                                println("[$wad] Finished extracting ${extracting.count()} files to $dir, took $totalTime ms")
                            }
                            "extract_nicely" -> {
                                if(wadOperation.size == 1) {
                                    errPrintln("[$wad] Error: No directory path provided!")
                                    continue@operate
                                }

                                val dir = File(wadOperation[1])
                                if(!dir.exists()) {
                                    errPrintln("[$wad] $dir does not exist, creating...")
                                    if(!dir.mkdirs()) {
                                        println("[$wad] An error occurred while creating $dir")
                                        continue@operate
                                    }
                                }
                                if(!dir.isDirectory) {
                                    errPrintln("[$wad] Error: $dir is not a directory!")
                                    continue@operate
                                }

                                val pattern = (if (wadOperation.size == 2) ".*" else wadOperation.copyFrom(2).joinToString(" "))
                                val extracting = ArrayList<WADFile>()
                                if(pattern.isRegex()) {
                                    val regex = pattern.toRegex()
                                    println("[$wad] Extracting files in $wad to $dir that match the regex $regex:")
                                    extracting.addAll(wadFile.files.filter { (name) -> name.matches(regex) })
                                }
                                else {
                                    val semiRegex = pattern.replace(".", "\\.").replace("*", ".*")
                                    if(semiRegex.isRegex()) {
                                        val regex = semiRegex.toRegex()
                                        println("[$wad] Extracting files in $wad to $dir that match the regex $regex:")
                                        extracting.addAll(wadFile.files.filter { (name) -> name.matches(regex) })
                                    }
                                    else {
                                        println("[$wad] Extracting files in $wad to $dir that end in $pattern:")
                                        extracting.addAll(wadFile.files.filter { (name) -> name.endsWith(pattern) })
                                    }
                                }

                                val totalTime = time {
                                    extracting.forEach files@ { file ->
                                        val parentDirs = File(dir, file.name.getParents())
                                        if (!parentDirs.exists()) {
                                            if (!parentDirs.mkdirs()) {
                                                errPrintln("[$wad] An error occurred while creating $parentDirs")
                                                return@files
                                            }
                                        }

                                        when(file.name.getExtension()) {
                                            "tga" -> {
                                                if(SpiralFormats.TGA.isFormat(file)) {
                                                    val extractLocation = File(dir, file.name.replace(".tga", ".png"))
                                                    val time = time {
                                                        val fileOutput = FileOutputStream(extractLocation)
                                                        SpiralFormats.TGA.convert(SpiralFormats.PNG, file, fileOutput)
                                                        fileOutput.close()
                                                    }

                                                    println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms, converted from TGA to PNG)")
                                                }
                                                else {
                                                    val extractLocation = File(dir, file.name)
                                                    val time = time {
                                                        val fileOutput = FileOutputStream(extractLocation)
                                                        file.getInputStream().writeTo(fileOutput, closeAfter = true)
                                                        fileOutput.close()
                                                    }

                                                    println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms, was not converted from TGA due to a formatting error)")
                                                }
                                            }
                                            "pak" -> {
                                                if(SpiralFormats.PAK.isFormat(file)) {
                                                    val extractLocation = File(dir, file.name.replace(".pak", ".zip"))
                                                    val time = time {
                                                        val fileOutput = FileOutputStream(extractLocation)
                                                        SpiralFormats.PAK.convert(SpiralFormats.ZIP, file, fileOutput)
                                                        fileOutput.close()
                                                    }

                                                    println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms, converted from PAK to ZIP)")
                                                }
                                                else {
                                                    val extractLocation = File(dir, file.name)
                                                    val time = time {
                                                        val fileOutput = FileOutputStream(extractLocation)
                                                        file.getInputStream().writeTo(fileOutput, closeAfter = true)
                                                        fileOutput.close()
                                                    }

                                                    println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms, was not converted from PAK due to a formatting error)")
                                                }
                                            }
                                            else -> {
                                                val extractLocation = File(dir, file.name)
                                                val time = time {
                                                    val fileOutput = FileOutputStream(extractLocation)
                                                    file.getInputStream().writeTo(fileOutput, closeAfter = true)
                                                    fileOutput.close()
                                                }

                                                println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms)")
                                            }
                                        }


                                    }
                                }
                                println("[$wad] Finished extracting ${extracting.count()} files to $dir, took $totalTime ms")
                            }

                            "exit" -> {
                                println("[$wad] Returning to previous menu...")
                                break@operate
                            }
                        }
                    }
                    catch(th: Throwable) {
                        errPrintln("An unexpected error occurred: \n${th.exportStackTrace()}")
                    }
                }
            }

            "convert" -> {
                if(operation.size == 1) {
                    errPrintln("Error: No file provided!")
                    continue@headless
                }

                if(operation.size == 2) {
                    errPrintln("Error: No format to convert from was provided!")
                    continue@headless
                }

                if(operation.size == 3) {
                    errPrintln("Error: No format to convert to was provided!")
                    continue@headless
                }

                val file = File(operation[1])
                if(!file.exists()) {
                    errPrintln("Error: $file does not exist!")
                    continue@headless
                }
                if(!file.isFile) {
                    errPrintln("Error: $file is not a file!")
                    continue@headless
                }

                val from = SpiralFormats.formatForName(operation[2])
                val to = SpiralFormats.formatForName(operation[3])

                if(from.isEmpty) {
                    errPrintln("Error: No such format with the name ${operation[2]} could be found to convert from!")
                    continue@headless
                }

                if(to.isEmpty) {
                    errPrintln("Error: No such format with the name ${operation[3]} could be found to convert to!")
                    continue@headless
                }

                if(!from().canConvert(to())) {
                    errPrintln("Error: You can't convert from ${from().getName()} to ${to().getName()}!")
                    continue@headless
                }

                val outputFile = if(operation.size == 4) File(file.absolutePath.replaceLast(from().getExtension(), to().getExtension())) else File(operation[4])
                FileOutputStream(outputFile).use { from().convert(to(), FileDataSource(file), it) }
                println("Converted $file from ${from().getName()} to ${to().getName()}, new file is located at $outputFile")
            }
            "formats" -> {
                println("SPIRAL Formats and Conversion")
                println("--------------")
                println("WAD")
                println("|--> ZIP")
                println("PAK")
                println("|--> ZIP")
                println("TGA")
                println("|--> PNG")
                println("|--> JPG")
                println("PNG")
                println("|--> JPG")
                println("|--> TGA")
                println("JPG")
                println("|--> PNG")
                println("|--> TGA")
            }

            "os" -> println(os)
            "roots" -> println(File.listRoots().joinToString())
            "in" -> {
                if(operation.size == 1) {
                    errPrintln("Error: No directory provided!")
                    continue@headless
                }

                val dir = File(operation.copyFrom(1).joinToString(" "))
                if(!dir.exists()) {
                    errPrintln("Error: $dir does not exist!")
                    continue@headless
                }

                println(dir.listFiles().joinToString())
            }
            "exit" -> {
                println("Bye!")
                break@headless
            }
            else -> println("Command not found (${operation.joinToString(" ")})")
        }
    }
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