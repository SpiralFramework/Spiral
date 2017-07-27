package org.abimon.spiral.headless

import org.abimon.spiral.core.*
import org.abimon.spiral.core.drills.DrillHead
import org.abimon.spiral.core.formats.*
import org.abimon.spiral.core.lin.LinScript
import org.abimon.spiral.core.objects.*
import org.abimon.visi.collections.asBase
import org.abimon.visi.collections.copyFrom
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.collections.pass
import org.abimon.visi.io.*
import org.abimon.visi.lang.*
import org.parboiled.errors.ErrorUtils
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    setHeadless()
    println("Initialising SPIRAL Power...")
    if (args.any { it == "-debug" }) {
        isDebug = true
        println("Debug mode engaged")
    }

    //holdIt()
    //twoDimensionsSucks()
    //patch()
    //udg()
    menu()
    //dumpScriptEntries()
    Thread.sleep(1000)
}

fun holdIt() {
    val nonstop = NonstopDebate(FileDataSource(File("processing/dr1_us_all_formats/Dr1/data/us/bin/nonstop_01_001.dat")))
    val customNonstop = make<CustomNonstopDebate> {
        this.secondsForDebate = nonstop.secondsForDebate
        nonstop.sections.forEach { section(it) }
    }

    FileOutputStream(File("processing/dr1_us_all_formats/Dr1/data/us/bin/nonstop_01_001_1.dat")).use { customNonstop.compile(it) }
}

fun twoDimensionsSucks() {
    FileInputStream(File("processing/2d.gmo")).use { stream ->
        val magic = stream.read(16)
        println(String(magic))

        val fileID = stream.readNumber(2, unsigned = true)
        val dataHeaderSize = stream.readNumber(2, unsigned = true)
        val dataSize = stream.readUnsignedLittleInt()
        val dataHeader = stream.read(dataHeaderSize.toInt() - 8)

        println("File ID: $fileID")
        println("Data Header Size: $dataHeaderSize")
        println("Data Size: $dataSize")
        println("Data Header: ${dataHeader asBase 16}")

        var read = dataHeaderSize
        while(read < dataSize) {
            val chunkType = stream.readNumber(2, unsigned = true)
            val chunkHeaderSize = stream.readNumber(2, unsigned = true)
            val chunkSize = stream.readUnsignedLittleInt()

            read += 8

            println("Type: $chunkType")
            println("Header Size: $chunkHeaderSize")
            println("Size: $chunkSize")

            when(chunkType) {
                0x3L -> {
                    val header = stream.read(chunkHeaderSize.toInt())
                    println("Header: ${header asBase 16}")
                    read += chunkHeaderSize
                }
                else -> {
                    val header = stream.read(chunkHeaderSize.toInt())
                    val chunk = stream.read(chunkSize.toInt())
                    println("Header: ${header asBase 16}")
                    println("Chunk: ${chunk asBase 16}")

                    read += chunkHeaderSize
                    read += chunkSize
                }
            }
        }
    }
}
fun extractText() {
    //    val dr1 = arrayOf("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad", "/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data_us.wad")
//    val dr2 = arrayOf("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa 2 Goodbye Despair/Danganronpa2.app/Contents/Resources/dr2_data.wad", "/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa 2 Goodbye Despair/Danganronpa2.app/Contents/Resources/dr2_data_us.wad")
//    for(fileName in dr2) {
//        val backupWadFile = File(fileName)
//
//        val textDir = File("processing/text")
//        if (!textDir.exists())
//            textDir.mkdir()
//
//        val people = mapOf(0 to "Makoto Naegi",
//                1 to "Kiyotaka Ishimaru",
//                2 to "Byakuya Togami",
//                3 to "Mondo Owada",
//                4 to "Leon Kuwata",
//                5 to "Hifumi Yamada",
//                6 to "Yasuhiro Hagakure",
//                7 to "Sayaka Maizono",
//                8 to "Kyoko Kirigi",
//                9 to "Aoi Asahina",
//                10 to "Toko Fukawa",
//                11 to "Sakura Ogami",
//                12 to "Celeste",
//                13 to "Junko Enoshima",
//                14 to "Chihiro Fujisaki",
//                15 to "Monokuma",
//                16 to "Real Junko Enoshima",
//                17 to "Alter Ego",
//                18 to "Genocider Syo",
//                19 to "Jin Kirigiri",
//                20 to "Makoto's Mum",
//                21 to "Makoto's Dad",
//                22 to "Komaru Naegi",
//                23 to "Kiyondo Ishida",
//                24 to "Daiya Owada",
//                30 to "???")
//
//        val backupWad = WAD(FileDataSource(backupWadFile))
////    backupWad.files.filter { (name) -> name.endsWith(".lin") }.forEach { file ->
////        val lin = Lin(file)
////        val textFile = File(textDir, file.name.child.replace(".lin", ".txt"))
////        val printStream = PrintStream(textFile)
////        var speaking = 0
////        lin.entries.forEach { script ->
////            if (script is TextEntry) {
////                var text = script.text
////                var clt = ""
////                for (word in script.text.replace("<", "`").replace(">", "`").splitOutsideGroup(group = StringGroup.TILDE)) {
////                    for(component in word.split("`")) {
////                        if (component.contains("CLT 4")) {
////                            clt = "*"
////                            text = text.replaceFirst("<CLT 4>", "*")
////                        } else if (component.contains("CLT 3")) {
////                            clt = "**"
////                            text = text.replaceFirst("<CLT 3>", "**")
////                        } else if (component.contains("CLT")) {
////                            text = text.replaceFirst("<CLT>", clt)
////                            clt = ""
////                        }
////                    }
////                }
////
////                text = text.replace("\n*", "*").replace(0.toChar().toString(), "")
////                val leadingWhitespace = Pattern.compile("([^*])(\\s+)\\*").matcher(text)
////                text = leadingWhitespace.replaceAll("$1*$2")
////                val followingWhitespace = Pattern.compile("\\*(\\s+)([^*])").matcher(text)
////                text = followingWhitespace.replaceAll("$1*$2")
////                printStream.println("${people[speaking] ?: "???"}: $text")
////            } else if (script is UnknownEntry && script.op == 0x21)
////                speaking = script.arguments[0]
////        }
////        println("Finished $textFile")
////        if(textFile.length() == 0L)
////            textFile.delete()
////    }
//
//        val dir = File("dr sprites")
//        backupWad.files.filter { wadFile -> SpiralFormats.TGA.isFormat(wadFile) }.forEach { file ->
//            val parentDir = File(dir, file.name.parents)
//            parentDir.mkdirs()
//
//            SpiralFormats.TGA.convert(SpiralFormats.PNG, file, FileOutputStream(File(parentDir, file.name.child.replace(".tga", ".png"))))
//        }
//        backupWad.files.filter { wadFile -> SpiralFormats.PAK.isFormat(wadFile) }.forEach { file ->
//            val parentDir = File(dir, file.name.replace(".pak", ""))
//            parentDir.mkdirs()
//            Pak(file).files.filter { pakFile -> SpiralFormats.TGA.isFormat(pakFile) }.forEach { pakFile -> SpiralFormats.TGA.convert(SpiralFormats.PNG, pakFile, FileOutputStream(File(parentDir, pakFile.name + ".png"))) }
//        }
//    }
}
fun dumpScriptEntries() {
    val wad = WAD(FileDataSource(File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data_us.wad")))
    val parentDir = File("processing/lin dump")
    if (!parentDir.exists())
        parentDir.mkdirs()

    wad.files.filter { (name) -> name.endsWith(".lin") }.flatMap { Lin(it).entries.map { script -> script to it } }.groupBy { (script) -> script.getOpCode() }.toSortedMap(Comparator { o1, o2 -> o1.compareTo(o2) }).forEach { code, scripts ->
        val file = File(parentDir, "0x${code.toString(16)}.txt")
        val out = PrintStream(file)

        println(scripts[0].first)
        val padLength = (scripts.filter { (entry) -> entry.getRawArguments().isNotEmpty() }.takeIf { it.isNotEmpty() }?.flatMap { (entry) -> entry.getRawArguments().map { "$it".length } }?.pass { a, b -> a.coerceAtLeast(b) } ?: 0) + 1
        scripts.forEach { (entry, location) -> out.println("${location.name.child}|${entry.getRawArguments().joinToString { it.toPaddedString(padLength) }}") }
        out.close()
    }
}
fun currentYear() {
    run findSize@ {
        val stream = FileInputStream(File("processing/genocider.btx"))
        val shtx = stream.readString(4)
        val version = stream.readPartialBytes(2, 2)

        when (String(version)) {
            "Fs" -> {
                val width = stream.readNumber(2, unsigned = true).toInt()
                val height = stream.readNumber(2, unsigned = true).toInt()
                val unknown = stream.readNumber(2, unsigned = true)

                val palette = ArrayList<Color>()

                for (i in 0 until 256)
                    palette.add(Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF))

                if (palette.all { it.red == 0 && it.green == 0 && it.blue == 0 && it.alpha == 0 }) ;
                else {
                    val pixelList = ArrayList<Color>()
                    var x = Int.MAX_VALUE
                    var y = Int.MAX_VALUE

                    stream.readChunked { pixels -> pixels.forEach { index -> pixelList.add(palette[index.toInt() and 0xFF]) } }

                    pixelList.forEachIndexed { index, color ->
                        if (color.red == 0 && color.green == 0 && color.blue == 1) {
                            val thix = (index % width)
                            val thiy = (index / width)

                            if (thix == 0 && thiy < y)
                                y = thiy
                            if (thiy == 0 && thix < x)
                                x = thix
                        }
                    }

                    println("Images are ${x}x${y}")
                }
            }
            else -> {
            }
        }
    }

    run Ff@ {
        val stream = FileInputStream(File("processing/special.btx"))
        val shtx = stream.readString(4)
        val version = stream.readPartialBytes(2, 2)

        when (String(version)) {
            "Ff" -> {
                val width = stream.readNumber(2, unsigned = true).toInt()
                val height = stream.readNumber(2, unsigned = true).toInt()
                val unknown = stream.readNumber(2, unsigned = true)

                val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                for (y in 0 until height)
                    for (x in 0 until width)
                        img.setRGB(x, y, Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF).rgb)

                ImageIO.write(img, "PNG", File("processing/special.png"))
            }
            else -> println("*Sigh*")
        }
    }
}

fun udg() {
    //currentYear()

    println("PNG images take ${measureTimeMillis {ImageIO.read(File("processing/mono.png")) } }")

    val btx = File("processing/udg busts btx")
    val png = File("processing/udg busts png")

//    btx.iterate(filters = arrayOf(FileFilter { it.name.matches("bustup_\\d+_\\d+\\.btx".toRegex()) })).forEach { file ->
//        val data = FileDataSource(file)
//        if(SHTXFormat.isFormat(data)) {
//            val comps = file.name.split('_')
//            val outputDir = File(png, comps[1])
//            if(!outputDir.exists())
//                outputDir.mkdirs()
//            val output = File(outputDir, "${comps[2].split('.')[0]}.png")
//            val img = ImageIO.read(ByteArrayInputStream(SpiralFormats.convert(SHTXFormat, PNGFormat, data)))
//            ImageIO.write(img.getSubimage(0, 0, 1937, 1112), "PNG", output)
//            println("Converted $file")
//        }
//    }

    val dir = File("processing/a2.cpk_unpacked")
//    dir.iterate(false).filter { it.extension == "btx" && SHTXFormat.isFormat(FileDataSource(it)) }.forEach { file ->
//        val outputDir = File("processing/udg${file.absolutePath.replace(dir.absolutePath, "").substringBeforeLast('/')}")
//        outputDir.mkdirs()
//
//        val output = File(outputDir, file.nameWithoutExtension + ".png")
//        FileOutputStream(output).use { SHTXFormat.convert(PNGFormat, FileDataSource(file), it) }
//    }

//    dir.iterate(false).filter { it.extension == "btx" && DRVitaCompressionFormat.isFormat(FileDataSource(it)) }.forEach { file ->
//        println("Decompressing $file")
//
//        val outputDir = File("processing/udg${file.absolutePath.replace(dir.absolutePath, "").substringBeforeLast('/')}")
//        outputDir.mkdirs()
//
//        val output = File(outputDir, file.nameWithoutExtension + ".unk")
//        FileOutputStream(output).use { DRVitaCompressionFormat.convert(BinaryFormat, FileDataSource(file), it) }
//    }

    val exec = Executors.newFixedThreadPool(8)
    File("processing/udg").iterate(false).filter { it.extension == "unk"  && DDS1DDSFormat.isFormat(FileDataSource(it)) }.forEach { file ->
        println("DDoSing $file")

        FileOutputStream(file.absolutePath.replaceLast(".unk", ".png")).use { DDS1DDSFormat.convert(PNGFormat, FileDataSource(file), it) }
    }

    //FileOutputStream(File("processing/mono.png")).use { DDS1DDSFormat.convert(PNGFormat, FileDataSource(File("processing/mono.dds")), it) }
}

//fun sfxb() {
//    val wad = WAD(FileDataSource(File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data_us.wad")))
//    val sfxB = ArrayList<SoundEffectEntryB>()
//
//    wad.files.filter { (name) -> name.endsWith(".lin") }.forEach {
//        val text = String(SpiralFormats.convert(SpiralFormats.LIN, SpiralFormats.TXT, it))
//        val result = SpiralDrill.runner.run(text)
//        if (result.parseErrors.isNotEmpty())
//            println(ErrorUtils.printParseError(result.parseErrors[0]))
//        else {
//            result.valueStack.forEach value@{ value -> sfxB.addAll((if (value is List<*>) (value[0] as DrillHead).formScript(value.subList(1, value.size).filterNotNull().toTypedArray()) as? SoundEffectEntryB else null) ?: return@value)}
//        }
//    }
//
//    println("\n" * 100)
//
//    println(sfxB.groupBy { (arg1) -> arg1 }.mapValues { (_, num) -> num.size }.toList().joinToString("\n") { (arg1, amount) -> "[$arg1] $amount entries"})
//}

fun process(name: String, data: DataSource, parent: File, dr1: Boolean = true) {
    val format = SpiralFormats.formatForData(data, SpiralFormats.drWadFormats) ?: run {
        val extractLocation = File(parent, if (name.lastIndexOf('.') == -1) name + ".unk" else name)
        val time = measureTimeMillis { FileOutputStream(extractLocation).use { data.use { inputStream -> inputStream.writeTo(it) } } }

        println("[Unknown] Extracted $name to $extractLocation (${data.size} bytes), took $time ms")
        return@process
    }

    when (format) {
        is PAKFormat -> {
            val pakDir = File(parent, name.replace(".pak", ""))
            pakDir.mkdirs()

            val extractLocation = File(parent, name.replace(".pak", "") + ".zip")
            val time = measureTimeMillis {
                val fileOutput = FileOutputStream(extractLocation)
                SpiralFormats.PAK.convert(SpiralFormats.ZIP, data, fileOutput)
                fileOutput.close()
            }

            println("[Unknown] Extracted $name to $extractLocation (${data.size} bytes), took $time ms, converted from PAK to ZIP")

            val pak = Pak(data)
            pak.files.forEach { pakFile -> process(pakFile.name, pakFile, pakDir, dr1) }

            val extractLocationOrig = File(parent, if (name.lastIndexOf('.') == -1) name + ".pak" else name)
            val timeOrig = measureTimeMillis { FileOutputStream(extractLocationOrig).use { data.use { inputStream -> inputStream.writeTo(it) } } }
            println("[Unknown] Extracted $name to $extractLocationOrig (${data.size} bytes), took $timeOrig ms")
        }
        is LINFormat -> {
            val convertingTo = format.preferredConversions.firstOrNull()
            if (convertingTo != null) {
                val extractLocation = File(parent, name.replace(".lin", "") + ".${convertingTo.extension ?: "unk"}")
                val time = measureTimeMillis {
                    val fileOutput = FileOutputStream(extractLocation)
                    LINFormat.convert(convertingTo, data, fileOutput, dr1)
                    fileOutput.close()
                }

                println("[Unknown] Extracted $name to $extractLocation (${data.size} bytes), took $time ms, converted from LIN to ${convertingTo.name})")

                val extractLocationOrig = File(parent, if (name.lastIndexOf('.') == -1) name + ".lin" else name)
                val timeOrig = measureTimeMillis { FileOutputStream(extractLocationOrig).use { data.use { inputStream -> inputStream.writeTo(it) } } }
                println("[Unknown] Extracted $name to $extractLocationOrig (${data.size} bytes), took $timeOrig ms)")
            } else {
                val extractLocation = File(parent, if (name.lastIndexOf('.') == -1) name + ".lin" else name)
                val time = measureTimeMillis { FileOutputStream(extractLocation).use { data.use { inputStream -> inputStream.writeTo(it) } } }

                println("[Unknown] Extracted $name to $extractLocation (${data.size} bytes), took $time ms, was not converted from LIN due to no preferable conversions being available")
            }
        }
        else -> {
            val convertingTo = format.preferredConversions.firstOrNull()
            if (convertingTo != null) {
                val extractLocation = File(parent, name.replace(".${format.extension ?: "unk"}", "") + ".${convertingTo.extension ?: "unk"}")
                val time = measureTimeMillis {
                    val fileOutput = FileOutputStream(extractLocation)
                    format.convert(convertingTo, data, fileOutput)
                    fileOutput.close()
                }

                println("[Unknown] Extracted $name to $extractLocation (${data.size} bytes), took $time ms, converted from ${format.name} to ${convertingTo.name})")

                val extractLocationOrig = File(parent, if (name.lastIndexOf('.') == -1) name + ".${format.extension ?: "unk"}" else name)
                val timeOrig = measureTimeMillis { FileOutputStream(extractLocationOrig).use { data.use { inputStream -> inputStream.writeTo(it) } } }
                println("[Unknown] Extracted $name to $extractLocationOrig (${data.size} bytes), took $timeOrig ms)")
            } else {
                val extractLocation = File(parent, if (name.lastIndexOf('.') == -1) name + ".${format.extension ?: "unk"}" else name)
                val time = measureTimeMillis { FileOutputStream(extractLocation).use { data.use { inputStream -> inputStream.writeTo(it) } } }

                println("[Unknown] Extracted $name to $extractLocation (${data.size} bytes), took $time ms, was not converted from ${format.name} due to no preferable conversions being available")
            }
        }
    }
}

fun menu() {
    val os = EnumOS.determineOS()
    val wads = HashSet<File>()
    headless@ while (true) {
        try {
            Thread.sleep(100)
            println("What would you like to do?")
            print("> ")
            val operation = readLine()?.splitOutsideGroup("\\s+") ?: break
            when (operation[0].toLowerCase()) {
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
                    println("\tconvert [file] [convertFrom] [convertTo] - Converts a file from one format to another. If a directory is provided, this acts upon all files in the directory")
                    println("\tidentify [file] - Attempt to identify the format of a file. If a directory is provided, this acts upon all files in the directory")
                    println("\tidentify_and_convert [file] [convertTo] - Attempts to identify the format of a file, and convert to another format. If a directory is provided, this acts upon all files in the directory")
                    println("\tformats - What formats are currently known")
                    println("\texit - Exits the program")
                }

                "find" -> {
                    when (os) {
                        EnumOS.WINDOWS -> {
                            for (root in File.listRoots()) {
                                for (programFolder in arrayOf(File(root, "Program Files (x86)"), File(root, "Program Files"))) {
                                    val steamFolder = File(programFolder, "Steam")
                                    if (steamFolder.exists()) {
                                        val common = File(steamFolder, "steamapps${File.separator}common")
                                        for (game in common.listFiles { file -> file.isDirectory && file.name.contains("Danganronpa") })
                                            wads.addAll(game.listFiles { file -> file.isFile && file.extension == "wad" && !file.name.contains(".backup") })
                                    }
                                }
                            }
                        }
                        EnumOS.MACOSX -> {
                            val steamFolder = os.getStorageLocation("Steam")
                            if (steamFolder.exists()) {
                                val common = File(steamFolder, "steamapps${File.separator}common")
                                for (game in common.listFiles { file -> file.isDirectory && file.name.contains("Danganronpa") }) {
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

                    if (wads.isEmpty())
                        errPrintln("Error: No WAD files detected! You can manually add them via the register command, or by running the locate command!")
                    else
                        println("WADs: ${wads.joinToPrefixedString("", "\n\t")}")
                }
                "locate" -> {
                    if (operation.size == 1) {
                        errPrintln("Error: No directory provided!")
                        continue@headless
                    }

                    val dir = File(operation.copyFrom(1).joinToString(" "))
                    if (!dir.exists()) {
                        errPrintln("Error: $dir does not exist!")
                        continue@headless
                    }

                    if (question("Warning: This operation will take quite some time. Do you wish to proceed to scan $dir (Y/N)? ", "Y")) {
                        val time = measureTimeMillis {
                            dir.iterate(filters = arrayOf(
                                    FileFilter { file -> !file.name.startsWith(".") },
                                    FileFilter { file -> file.isDirectory || (file.isFile && file.extension == "wad" && !file.name.contains(".backup")) }
                            )).forEach { wad ->
                                if (question("WAD Found ($wad). Would you like to add this to the internal registry (Y/N)? ", "Y"))
                                    wads.add(wad)
                            }
                        }
                        println("Took $time ms.")
                        if (wads.isEmpty())
                            errPrintln("Error: No WAD files detected! You can manually add them via the register command, or by running the locate command!")
                        else
                            println(wads.joinToString("\n"))
                    }
                }

                "register" -> {
                    if (operation.size == 1) {
                        errPrintln("Error: No file provided!")
                        continue@headless
                    }

                    val wad = File(operation.copyFrom(1).joinToString(" "))
                    if (!wad.exists()) {
                        errPrintln("Error: $wad does not exist!")
                        continue@headless
                    }

                    if (!wad.isFile) {
                        errPrintln("Error: $wad is not a file!")
                        continue@headless
                    }

                    if (wad.extension != "wad") {
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
                    print("[operate] > ")
                    var wad = readLine() ?: break@headless
                    if (wad == "exit")
                        continue@headless
                    while (wads.none { file -> file.nameWithoutExtension == wad || file.name == wad || file.absolutePath.endsWith(wad) }) {
                        println("$wad is an invalid WAD file")
                        print("[operate] > ")
                        wad = readLine() ?: break@headless
                        if (wad == "exit")
                            continue@headless
                    }

                    val original = wads.first { file -> file.nameWithoutExtension == wad || file.name == wad || file.absolutePath.endsWith(wad) }
                    var wadFile = WAD(FileDataSource(original))
                    println("Now operating on $wad")

                    operate@ while (true) {
                        try {
                            Thread.sleep(100)
                            println("[$wad] What would you like to do?")
                            print("[$wad] > ")
                            val wadOperation = readLine()?.splitOutsideGroup("\\s+") ?: break

                            when (wadOperation[0].toLowerCase()) {
                                "help" -> {
                                    println("[$wad] WAD Operations")
                                    println("[$wad] Help")
                                    println("[$wad]\tCommands can be run just by entering the command, and parameters are supplied between speech marks")
                                    println("[$wad]\tParameters listed as [parameter] are mandatory, while ones listed as {parameter} are optional.")
                                    println("[$wad]\tFor instance: command \"Parameter 1\" \"Parameter 2\"")
                                    println("[$wad] Commands")
                                    println("[$wad]\thelp - Display this message")
                                    println("[$wad]\tlist {regex} - List all files that match the provided regex, or all files if no regex are provided")
                                    println("[$wad]\textract [dir] {regex} - Extract all files that match the provided regex to the provided directory, or all files if no regex are provided")
                                    println("[$wad]\textract_nicely [dir] {regex} - List all files that match the provided regex to the provided directory, or all files if no regex are provided. The files are converted to nicer formats where possible (TGA -> PNG)")
                                    println("[$wad]\tbackup - Backs up the WAD file to $wad.wad.backup")
                                    println("[$wad]\tcompile [dir] {regex} - Recompile this wad file using the files in the provided directory that match the provided regex, or all files if no regex are provided. The WAD file is then reloaded")
                                    println("[$wad]\tcompile_nicely [dir] {regex} - Recompile this wad file using the files in the provided directory that match the provided regex, or all files if no regex are provided. The WAD file is then reloaded. The files are converted from nicer formats where possible (PNG -> TGA)")
                                    println("[$wad]\treload - Reload the contents of this WAD file")
                                    println("[$wad]\texit - Return to the previous menu")
                                }

                                "list" -> {
                                    val pattern = (if (wadOperation.size == 1) ".*" else wadOperation.copyFrom(1).joinToString(" "))
                                    if (pattern.isRegex()) {
                                        val regex = pattern.toRegex()
                                        println("[$wad] Files in $wad that match the regex $regex:")
                                        wadFile.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) }.forEach { (name, size, offset) ->
                                            println("[$wad] $name ($size bytes, $offset bytes from the start)")
                                        }
                                    } else {
                                        val semiRegex = pattern.replace(".", "\\.").replace("*", ".*")
                                        if (semiRegex.isRegex()) {
                                            val regex = semiRegex.toRegex()
                                            println("[$wad] Files in $wad that match the regex $regex:")
                                            wadFile.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) }.forEach { (name, size, offset) ->
                                                println("[$wad] $name ($size bytes, $offset bytes from the start)")
                                            }
                                        } else {
                                            println("[$wad] Files in $wad that end with $pattern:")
                                            wadFile.files.filter { (name) -> name.endsWith(pattern) }.forEach { (name, size, offset) ->
                                                println("[$wad] $name ($size bytes, $offset bytes from the start)")
                                            }
                                        }
                                    }
                                }
                                "extract" -> {
                                    if (wadOperation.size == 1) {
                                        errPrintln("[$wad] Error: No directory path provided!")
                                        continue@operate
                                    }

                                    val dir = File(wadOperation[1])
                                    if (!dir.exists()) {
                                        errPrintln("[$wad] $dir does not exist, creating...")
                                        if (!dir.mkdirs()) {
                                            println("[$wad] An error occurred while creating $dir")
                                            continue@operate
                                        }
                                    }
                                    if (!dir.isDirectory) {
                                        errPrintln("[$wad] Error: $dir is not a directory!")
                                        continue@operate
                                    }

                                    val pattern = (if (wadOperation.size == 2) ".*" else wadOperation.copyFrom(2).joinToString(" "))
                                    val extracting = ArrayList<WADFileEntry>()
                                    if (pattern.isRegex()) {
                                        val regex = pattern.toRegex()
                                        println("[$wad] Extracting files in $wad to $dir that match the regex $regex:")
                                        extracting.addAll(wadFile.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) })
                                    } else {
                                        val semiRegex = pattern.replace(".", "\\.").replace("*", ".*")
                                        if (semiRegex.isRegex()) {
                                            val regex = semiRegex.toRegex()
                                            println("[$wad] Extracting files in $wad to $dir that match the regex $regex:")
                                            extracting.addAll(wadFile.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) })
                                        } else {
                                            println("[$wad] Extracting files in $wad to $dir that end in $pattern:")
                                            extracting.addAll(wadFile.files.filter { (name) -> name.endsWith(pattern) })
                                        }
                                    }

                                    val totalTime = measureTimeMillis {
                                        extracting.forEach files@ { file ->
                                            val parentDirs = File(dir, file.name.parents)
                                            if (!parentDirs.exists()) {
                                                if (!parentDirs.mkdirs()) {
                                                    errPrintln("[$wad] An error occurred while creating $parentDirs")
                                                    return@files
                                                }
                                            }

                                            val extractLocation = File(dir, file.name)
                                            val time = measureTimeMillis {
                                                val fileOutput = FileOutputStream(extractLocation)
                                                file.use { stream -> stream.writeTo(fileOutput, closeAfter = true) }
                                                fileOutput.close()
                                            }

                                            println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms")
                                        }
                                    }
                                    println("[$wad] Finished extracting ${extracting.count()} files to $dir, took $totalTime ms")
                                }
                                "extract_nicely" -> {
                                    if (wadOperation.size == 1) {
                                        errPrintln("[$wad] Error: No directory path provided!")
                                        continue@operate
                                    }

                                    val dir = File(wadOperation[1])
                                    if (!dir.exists()) {
                                        errPrintln("[$wad] $dir does not exist, creating...")
                                        if (!dir.mkdirs()) {
                                            println("[$wad] An error occurred while creating $dir")
                                            continue@operate
                                        }
                                    }
                                    if (!dir.isDirectory) {
                                        errPrintln("[$wad] Error: $dir is not a directory!")
                                        continue@operate
                                    }

                                    val pattern = (if (wadOperation.size == 2) ".*" else wadOperation.copyFrom(2).joinToString(" "))
                                    val extracting = ArrayList<WADFileEntry>()
                                    if (pattern.isRegex()) {
                                        val regex = pattern.toRegex()
                                        println("[$wad] Extracting files in $wad to $dir that match the regex $regex:")
                                        extracting.addAll(wadFile.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) })
                                    } else {
                                        val semiRegex = pattern.replace(".", "\\.").replace("*", ".*")
                                        if (semiRegex.isRegex()) {
                                            val regex = semiRegex.toRegex()
                                            println("[$wad] Extracting files in $wad to $dir that match the regex $regex:")
                                            extracting.addAll(wadFile.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) })
                                        } else {
                                            println("[$wad] Extracting files in $wad to $dir that end in $pattern:")
                                            extracting.addAll(wadFile.files.filter { (name) -> name.endsWith(pattern) })
                                        }
                                    }

                                    val totalTime = measureTimeMillis {
                                        extracting.forEach files@ { file ->
                                            val parentDirs = File(dir, file.name.parents)
                                            if (!parentDirs.exists()) {
                                                if (!parentDirs.mkdirs()) {
                                                    errPrintln("[$wad] An error occurred while creating $parentDirs")
                                                    return@files
                                                }
                                            }

                                            val sourceFormat = SpiralFormats.formatForExtension(file.name.extension)
                                            if(sourceFormat != null) {
                                                if (sourceFormat.isFormat(file)) {
                                                    when(sourceFormat) {
                                                        LINFormat -> {
                                                            val convertingTo = LINFormat.preferredConversions.firstOrNull()
                                                            if (convertingTo != null) {
                                                                val extractLocation = File(dir, file.name.replace(".lin", ".${convertingTo.extension ?: "unk"}"))
                                                                val time = measureTimeMillis {
                                                                    val fileOutput = FileOutputStream(extractLocation)
                                                                    LINFormat.convert(convertingTo, file, fileOutput, wad.contains("dr1_data"))
                                                                    fileOutput.close()
                                                                }

                                                                println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms, converted from LIN to ${convertingTo.name}")
                                                            } else {
                                                                val extractLocation = File(dir, file.name)
                                                                val time = measureTimeMillis { FileOutputStream(extractLocation).use { fos -> file.use { stream -> stream.writeTo(fos, closeAfter = true) } } }

                                                                println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms, was not converted from LIN due to no preferable conversions being available")
                                                            }
                                                        }
                                                        else -> {
                                                            val convertingTo = sourceFormat.preferredConversions.firstOrNull()
                                                            if (convertingTo != null) {
                                                                val extractLocation = File(dir, file.name.replace(".${sourceFormat.extension ?: "unk"}", ".${convertingTo.extension ?: "unk"}"))
                                                                val time = measureTimeMillis {
                                                                    val fileOutput = FileOutputStream(extractLocation)
                                                                    sourceFormat.convert(convertingTo, file, fileOutput)
                                                                    fileOutput.close()
                                                                }

                                                                println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms, converted from ${sourceFormat.name} to ${convertingTo.name}")
                                                            } else {
                                                                val extractLocation = File(dir, file.name)
                                                                val time = measureTimeMillis { FileOutputStream(extractLocation).use { fos -> file.use { stream -> stream.writeTo(fos, closeAfter = true) } } }

                                                                println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms, was not converted from ${sourceFormat.name} due to no preferable conversions being available")
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    val extractLocation = File(dir, file.name)
                                                    val time = measureTimeMillis { FileOutputStream(extractLocation).use { fos -> file.use { stream -> stream.writeTo(fos, closeAfter = true) } } }

                                                    println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms, was not converted from ${sourceFormat.name} due to a formatting error")
                                                }
                                            }
                                            else {
                                                val extractLocation = File(dir, file.name)
                                                val time = measureTimeMillis { FileOutputStream(extractLocation).use { fos -> file.use { stream -> stream.writeTo(fos, closeAfter = true) } } }

                                                println("[$wad] Extracted ${file.name} to $extractLocation (${file.size} bytes), took $time ms)")
                                            }
                                        }
                                    }
                                    println("[$wad] Finished extracting ${extracting.count()} files to $dir, took $totalTime ms")
                                }
                                "extract_all_nicely" -> {
                                    if (wadOperation.size == 1) {
                                        errPrintln("[$wad] Error: No directory path provided!")
                                        continue@operate
                                    }

                                    val dir = File(wadOperation[1])
                                    if (!dir.exists()) {
                                        errPrintln("[$wad] $dir does not exist, creating...")
                                        if (!dir.mkdirs()) {
                                            println("[$wad] An error occurred while creating $dir")
                                            continue@operate
                                        }
                                    }
                                    if (!dir.isDirectory) {
                                        errPrintln("[$wad] Error: $dir is not a directory!")
                                        continue@operate
                                    }

                                    val totalTime = measureTimeMillis {
                                        wadFile.files.forEach { file ->
                                            val parentDirs = File(dir, file.name.parents)
                                            if (!parentDirs.exists()) {
                                                if (!parentDirs.mkdirs()) {
                                                    errPrintln("[$wad] An error occurred while creating $parentDirs")
                                                    return@forEach
                                                }
                                            }

                                            process(file.name.child, file, parentDirs, wad.contains("dr1_data"))
                                        }
                                    }
                                    println("[$wad] Finished extracting all files of a format to $dir, took $totalTime ms")
                                }
                                "backup" -> {
                                    println("Backing up now...")
                                    val backup = File(original.absolutePath + ".backup")
                                    val timeTaken = measureTimeMillis { FileInputStream(original).use { inputStream -> inputStream.writeTo(FileOutputStream(backup), closeAfter = true) } }

                                    println("And all backed up to ${backup.absolutePath}, took $timeTaken ms")
                                }
                                "compile" -> {
                                    if (wadOperation.size == 1) {
                                        errPrintln("[$wad] Error: No directory path provided!")
                                        continue@operate
                                    }

                                    val dir = File(wadOperation[1])
                                    if (!dir.exists()) {
                                        errPrintln("[$wad] $dir does not exist!")
                                        continue@operate
                                    }

                                    if (!dir.isDirectory) {
                                        errPrintln("[$wad] Error: $dir is not a directory!")
                                        continue@operate
                                    }

                                    val pattern = (if (wadOperation.size == 2) ".*" else wadOperation.copyFrom(2).joinToString(" "))
                                    val extracting = ArrayList<File>()
                                    if (pattern.isRegex()) {
                                        val regex = pattern.toRegex()
                                        println("[$wad] Compiling files in $wad to $dir that match the regex $regex:")
                                        extracting.addAll(dir.iterate().filter { file -> file.absolutePath.replace(dir.absolutePath + File.separator, "").matches(regex) || file.name.matches(regex) })
                                    } else {
                                        val semiRegex = pattern.replace(".", "\\.").replace("*", ".*")
                                        if (semiRegex.isRegex()) {
                                            val regex = semiRegex.toRegex()
                                            println("[$wad] Compiling files in $wad to $dir that match the regex $regex:")
                                            extracting.addAll(dir.iterate().filter { file -> file.absolutePath.replace(dir.absolutePath + File.separator, "").matches(regex) || file.name.matches(regex) })
                                        } else {
                                            println("[$wad] Compiling files in $wad to $dir that end in $pattern:")
                                            extracting.addAll(dir.iterate().filter { file -> file.absolutePath.replace(dir.absolutePath + File.separator, "").endsWith(pattern) })
                                        }
                                    }

                                    extracting.forEach { println("[$wad] ${it.absolutePath.replace(dir.absolutePath + File.separator, "")} will be compiled into $original") }
                                    println("Compiling...")

                                    val customWADLocation = File(original.absolutePath + ".tmp")
                                    val compileTime = measureTimeMillis {
                                        val customWAD = make<CustomWAD> {
                                            major(1)
                                            minor(1)

                                            wad(wadFile)
                                            extracting.forEach { file(it, it.absolutePath.replace(dir.absolutePath + File.separator, "")) }
                                        }

                                        FileOutputStream(customWADLocation).use { customWAD.compile(it) }
                                    }
                                    val movementTime = measureTimeMillis { FileInputStream(customWADLocation).use { inputStream -> inputStream.writeTo(FileOutputStream(original), closeAfter = true) } }
                                    customWADLocation.delete()

                                    println("Compiled ${extracting.size} new files into $original, tool $compileTime ms to compile and $movementTime ms to move")
                                    wadFile = WAD(FileDataSource(original))
                                }
                                "compile_nicely" -> {
                                    if (wadOperation.size == 1) {
                                        errPrintln("[$wad] Error: No directory path provided!")
                                        continue@operate
                                    }

                                    val dir = File(wadOperation[1])
                                    if (!dir.exists()) {
                                        errPrintln("[$wad] $dir does not exist!")
                                        continue@operate
                                    }

                                    if (!dir.isDirectory) {
                                        errPrintln("[$wad] Error: $dir is not a directory!")
                                        continue@operate
                                    }

                                    val pattern = (if (wadOperation.size == 2) ".*" else wadOperation.copyFrom(2).joinToString(" "))
                                    val compilingFiles = ArrayList<File>()
                                    if (pattern.isRegex()) {
                                        val regex = pattern.toRegex()
                                        println("[$wad] Compiling files in $wad to $dir that match the regex $regex:")
                                        compilingFiles.addAll(dir.iterate().filter { file -> file.absolutePath.replace(dir.absolutePath + File.separator, "").matches(regex) || file.name.matches(regex) })
                                    } else {
                                        val semiRegex = pattern.replace(".", "\\.").replace("*", ".*")
                                        if (semiRegex.isRegex()) {
                                            val regex = semiRegex.toRegex()
                                            println("[$wad] Compiling files in $wad to $dir that match the regex $regex:")
                                            compilingFiles.addAll(dir.iterate().filter { file -> file.absolutePath.replace(dir.absolutePath + File.separator, "").matches(regex) || file.name.matches(regex) })
                                        } else {
                                            println("[$wad] Compiling files in $wad to $dir that end in $pattern:")
                                            compilingFiles.addAll(dir.iterate().filter { file -> file.absolutePath.replace(dir.absolutePath + File.separator, "").endsWith(pattern) })
                                        }
                                    }

                                    //compilingFiles.forEach { println("[$wad] ${it.absolutePath.replace(dir.absolutePath + File.separator, "")} will be compiled into $original") }
                                    val compiling = compilingFiles.map { file ->
                                        val fileData = FileDataSource(file)
                                        when (file.name.extension) {
                                            "png" -> {
                                                if (SpiralFormats.PNG.isFormat(fileData)) {
                                                    val name = file.absolutePath.replace(dir.absolutePath + File.separator, "").replace(".png", ".tga")
                                                    println("[$wad] $name will be compiled into $original (Converting from PNG -> TGA)")
                                                    return@map name to FunctionDataSource { SpiralFormats.convert(SpiralFormats.PNG, SpiralFormats.TGA, fileData) }
                                                } else {
                                                    println("[$wad] ${file.absolutePath.replace(dir.absolutePath + File.separator, "")} will be compiled into $original (Can't convert from PNG -> TGA due to a formatting error)")
                                                    return@map file.absolutePath.replace(dir.absolutePath + File.separator, "") to fileData
                                                }
                                            }
                                            "jpg" -> {
                                                if (SpiralFormats.JPG.isFormat(fileData)) {
                                                    val name = file.absolutePath.replace(dir.absolutePath + File.separator, "").replace(".jpg", ".tga")
                                                    println("[$wad] $name will be compiled into $original (Converting from JPG -> TGA)")
                                                    return@map name to FunctionDataSource { SpiralFormats.convert(SpiralFormats.JPG, SpiralFormats.TGA, fileData) }
                                                } else {
                                                    println("[$wad] ${file.absolutePath.replace(dir.absolutePath + File.separator, "")} will be compiled into $original (Can't convert from JPG -> TGA due to a formatting error)")
                                                    return@map file.absolutePath.replace(dir.absolutePath + File.separator, "") to fileData
                                                }
                                            }
                                            "zip" -> {
                                                if (SpiralFormats.ZIP.isFormat(fileData)) {
                                                    val name = file.absolutePath.replace(dir.absolutePath + File.separator, "").replace(".zip", ".pak")
                                                    println("[$wad] $name will be compiled into $original (Converting from ZIP -> PAK)")
                                                    return@map name to FunctionDataSource { SpiralFormats.convert(SpiralFormats.ZIP, SpiralFormats.PAK, fileData) }
                                                } else {
                                                    println("[$wad] ${file.absolutePath.replace(dir.absolutePath + File.separator, "")} will be compiled into $original (Can't convert from ZIP -> PAK due to a formatting error)")
                                                    return@map file.absolutePath.replace(dir.absolutePath + File.separator, "") to fileData
                                                }
                                            }
                                            "stxt" -> {
                                                if (SpiralTextFormat.isFormat(fileData)) {
                                                    val name = file.absolutePath.replace(dir.absolutePath + File.separator, "").replace(".stxt", ".lin")
                                                    println("[$wad] $name will be compiled into $original (Converting from SPIRAL TXT -> LIN)")
                                                    return@map name to FunctionDataSource { SpiralTextFormat.convertToBytes(LINFormat, fileData) }
                                                } else {
                                                    println("[$wad] ${file.absolutePath.replace(dir.absolutePath + File.separator, "")} will be compiled into $original (Can't convert from SPIRAL TXT -> LIN due to a formatting error)")
                                                    return@map file.absolutePath.replace(dir.absolutePath + File.separator, "") to fileData
                                                }
                                            }
                                            "txt" -> {
                                                if (SpiralFormats.TXT.isFormat(fileData)) {
                                                    val name = file.absolutePath.replace(dir.absolutePath + File.separator, "").replace(".txt", ".lin")
                                                    println("[$wad] $name will be compiled into $original (Converting from TXT -> LIN)")
                                                    return@map name to FunctionDataSource { SpiralFormats.convert(SpiralFormats.TXT, SpiralFormats.LIN, fileData) }
                                                } else {
                                                    println("[$wad] ${file.absolutePath.replace(dir.absolutePath + File.separator, "")} will be compiled into $original (Can't convert from TXT -> LIN due to a formatting error)")
                                                    return@map file.absolutePath.replace(dir.absolutePath + File.separator, "") to fileData
                                                }
                                            }
                                            else -> {
                                                println("[$wad] ${file.absolutePath.replace(dir.absolutePath + File.separator, "")} will be compiled into $original")
                                                return@map file.absolutePath.replace(dir.absolutePath + File.separator, "") to fileData
                                            }
                                        }
                                    }
                                    println("Compiling...")

                                    val customWADLocation = File(original.absolutePath + ".tmp")
                                    val compileTime = measureTimeMillis {
                                        val customWAD = make<CustomWAD> {
                                            major(1)
                                            minor(1)

                                            wad(wadFile)
                                            compiling.forEach { (name, data) -> this.data(name, data) }
                                        }

                                        FileOutputStream(customWADLocation).use { customWAD.compile(it) }
                                    }
                                    val movementTime = measureTimeMillis { FileInputStream(customWADLocation).use { inputStream -> inputStream.writeTo(FileOutputStream(original), closeAfter = true) } }
                                    customWADLocation.delete()

                                    println("Compiled ${compiling.size} new files into $original, took $compileTime ms to compile and $movementTime ms to move")
                                    wadFile = WAD(FileDataSource(original))
                                }
                                "reload" -> {
                                    wadFile = WAD(FileDataSource(original))
                                    println("[$wad] Reloaded WAD file")
                                }
                                "exit" -> {
                                    println("[$wad] Returning to previous menu...")
                                    break@operate
                                }
                            }
                        } catch(th: Throwable) {
                            errPrintln("An unexpected error occurred: \n${th.exportStackTrace()}")
                        }
                    }
                }

                "convert" -> {
                    if (operation.size == 1) {
                        errPrintln("Error: No file provided!")
                        continue@headless
                    }

                    if (operation.size == 2) {
                        errPrintln("Error: No format to convert from was provided!")
                        continue@headless
                    }

                    if (operation.size == 3) {
                        errPrintln("Error: No format to convert to was provided!")
                        continue@headless
                    }

                    val file = File(operation[1])
                    if (!file.exists()) {
                        errPrintln("Error: $file does not exist!")
                        continue@headless
                    }

                    val from = SpiralFormats.formatForName(operation[2])
                    val to = SpiralFormats.formatForName(operation[3])

                    if (from == null) {
                        errPrintln("Error: No such format with the name ${operation[2]} could be found to convert from!")
                        continue@headless
                    }

                    if (to == null) {
                        errPrintln("Error: No such format with the name ${operation[3]} could be found to convert to!")
                        continue@headless
                    }

                    if (!from.canConvert(to)) {
                        errPrintln("Error: You can't convert from ${from.name} to ${to.name}!")
                        continue@headless
                    }

                    if (file.isFile) {
                        val outputFile = if (operation.size == 4) File(file.absolutePath.replaceLast("." + (from.extension ?: "unk"), "") + "." + (to.extension ?: "unk")) else File(operation[4])
                        FileOutputStream(outputFile).use { from.convert(to, FileDataSource(file), it) }
                        println("Converted $file from ${from.name} to ${to.name}, new file is located at $outputFile")
                    } else if (file.isDirectory) {
                        file.iterate().filter { f -> f.isFile }.map { it to FileDataSource(it) }.filter { (_, data) -> from.isFormat(data) }.forEach { (f, data) ->
                            val outputFile = File(f.absolutePath.replaceLast("." + (from.extension ?: "unk"), "") + "." + (to.extension ?: "unk"))
                            FileOutputStream(outputFile).use { from.convert(to, data, it) }
                            println("Converted $f from ${from.name} to ${to.name}, new file is located at $outputFile")
                        }
                    }
                }
                "identify" -> {
                    if (operation.size == 1) {
                        errPrintln("Error: No file provided!")
                        continue@headless
                    }

                    val file = File(operation[1])
                    if (!file.exists()) {
                        errPrintln("Error: $file does not exist!")
                        continue@headless
                    }

                    if (file.isFile) {
                        val format = SpiralFormats.formatForData(FileDataSource(file))
                        if (format == null)
                            println("No format recognised for $file")
                        else
                            println("Format for $file: ${format.name}")
                    } else if (file.isDirectory) {
                        file.iterate().filter { f -> f.isFile }.map { it to SpiralFormats.formatForData(FileDataSource(it)) }.forEach { (f, format) ->
                            if (format == null)
                                println("No format recognised for $f")
                            else
                                println("Format for $f: ${format.name}")
                        }
                    }
                }
                "identify_and_convert" -> {
                    if (operation.size == 1) {
                        errPrintln("Error: No file provided!")
                        continue@headless
                    }

                    if (operation.size == 2) {
                        errPrintln("Error: No format to convert to was provided!")
                        continue@headless
                    }

                    val file = File(operation[1])
                    if (!file.exists()) {
                        errPrintln("Error: $file does not exist!")
                        continue@headless
                    }

                    val to = SpiralFormats.formatForName(operation[2])

                    if (to == null) {
                        errPrintln("Error: No such format with the name ${operation[3]} could be found to convert to!")
                        continue@headless
                    }

                    if (file.isFile) {
                        val data = FileDataSource(file)
                        val from = SpiralFormats.formatForData(data)

                        if (from == null) {
                            errPrintln("Error: No format found for $file")
                            continue@headless
                        }

                        if (!from.canConvert(to)) {
                            errPrintln("Error: You can't convert from ${from.name} to ${to.name}!")
                            continue@headless
                        }

                        val outputFile = if (operation.size == 4) File(file.absolutePath.replaceLast("." + (from.extension ?: "unk"), "") + "." + (to.extension ?: "unk")) else File(operation[4])
                        FileOutputStream(outputFile).use { from.convert(to, data, it) }
                        println("Converted $file from ${from.name} to ${to.name}, new file is located at $outputFile")
                    } else if (file.isDirectory) {
                        file.iterate().filter { f -> f.isFile }.map { it to FileDataSource(it) }.map { pair -> pair and SpiralFormats.formatForData(pair.second) }.forEach { (f, data, from) ->
                            if (from == null) {
                                errPrintln("Error: No format found for $f")
                                return@forEach
                            }

                            if (!from.canConvert(to)) {
                                errPrintln("Error: You can't convert from ${from.name} to ${to.name}!")
                                return@forEach
                            }

                            val outputFile = File(f.absolutePath.replaceLast(".${from.extension}", "") + ".${to.extension}")
                            FileOutputStream(outputFile).use { from.convert(to, data, it) }
                            println("Converted $f from ${from.name} to ${to.name}, new file is located at $outputFile")
                        }
                    }
                }
                "convert_lin" -> {
                    if (operation.size == 1) {
                        errPrintln("Error: No file provided!")
                        continue@headless
                    }

                    if (operation.size == 2) {
                        errPrintln("Error: No format to convert to was provided!")
                        continue@headless
                    }

                    val file = File(operation[1])
                    if (!file.exists()) {
                        errPrintln("Error: $file does not exist!")
                        continue@headless
                    }

                    val to = SpiralFormats.formatForName(operation[2])

                    if (to == null) {
                        errPrintln("Error: No such format with the name ${operation[3]} could be found to convert to!")
                        continue@headless
                    }

                    val dr1 = if(operation.size == 3) true else operation[3].toBoolean()

                    if (file.isFile) {
                        val data = FileDataSource(file)

                        if (!LINFormat.canConvert(to)) {
                            errPrintln("Error: You can't convert a LIN file to ${to.name}!")
                            continue@headless
                        }

                        val outputFile = if (operation.size == 4) File(file.absolutePath.replaceLast(".lin", "") + "." + (to.extension ?: "unk")) else File(operation[4])
                        FileOutputStream(outputFile).use { LINFormat.convert(to, data, it, dr1) }
                        println("Converted $file from LIN to ${to.name}, new file is located at $outputFile")
                    } else if (file.isDirectory) {
                        file.iterate().filter { f -> f.isFile }.map { it to FileDataSource(it) }.filter { (f, data) -> LINFormat.isFormat(data) }.forEach { (f, data) ->
                            if (!LINFormat.canConvert(to)) {
                                errPrintln("Error: You can't convert a LIN file to ${to.name}!")
                                return@forEach
                            }

                            val outputFile = File(f.absolutePath.replaceLast(".lin", "") + ".${to.extension}")
                            FileOutputStream(outputFile).use { LINFormat.convert(to, data, it, dr1) }
                            println("Converted $f from LIN to ${to.name}, new file is located at $outputFile")
                        }
                    }
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

                "custom" -> {
                    if (operation.size == 1) {
                        errPrintln("Error: No file provided!")
                        continue@headless
                    }

                    val file = File(operation[1])
                    if (!file.exists()) {
                        errPrintln("Error: $file does not exist!")
                        continue@headless
                    }

                    if (file.isFile) {
                        val result = SpiralDrill.stxtRunner.run(file.readText())
                        if (result.parseErrors.isNotEmpty())
                            println(ErrorUtils.printParseError(result.parseErrors[0]))
                        else {
                            val script = ArrayList<LinScript>()
                            result.valueStack.forEach { value ->
                                if (value is List<*>) script.addAll((value[0] as DrillHead).formScripts(value.subList(1, value.size).filterNotNull().toTypedArray()))
                            }
                            println(script.joinToString("\n"))

                            val customLin = make<CustomLin> {
                                script.forEach { entry(it) }
                            }

                            FileOutputStream(File("custom.lin")).use { customLin.compile(it) }
                        }
                    } else {
                        println("$file is not a file!")
                    }
                }

                "open" -> {
                    if(operation.size == 1) {
                        SteamProtocol.openGame(STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC)
                        println("Opening Danganronpa: Trigger Happy Havoc")
                    }
                    else {
                        when(operation[1].toLowerCase()) {
                            "dr1" -> {
                                SteamProtocol.openGame(STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC)
                                println("Opening Danganronpa: Trigger Happy Havoc")
                            }
                            "dr2" -> {
                                SteamProtocol.openGame(STEAM_DANGANRONPA_2_GOODBYE_DESPAIR)
                                println("Opening Danganronpa 2: Goodbye Despair")
                            }
                        }
                    }
                }

                "os" -> println(os)
                "roots" -> println(File.listRoots().joinToString())
                "in" -> {
                    if (operation.size == 1) {
                        errPrintln("Error: No directory provided!")
                        continue@headless
                    }

                    val dir = File(operation.copyFrom(1).joinToString(" "))
                    if (!dir.exists()) {
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

        } catch(th: Throwable) {
            errPrintln("An unexpected error occurred: \n${th.exportStackTrace()}")
        }
    }
}

fun restore() {
    var time = measureTimeMillis {
        val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad")
        val backupWadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data copy.wad")

        val backupWad = WAD(FileDataSource(backupWadFile))

        val customWad = make<CustomWAD> {
            major(1)
            minor(1)

            wad(backupWad)
        }
        customWad.compile(FileOutputStream(wadFile))
    }
    println("Took $time ms to restore dr1_data.wad")

    time = measureTimeMillis {
        val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data_us.wad")
        val backupWadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data_us copy.wad")

        val backupWad = WAD(FileDataSource(backupWadFile))

        val customWad = make<CustomWAD> {
            major(1)
            minor(1)

            wad(backupWad)
        }
        customWad.compile(FileOutputStream(wadFile))
    }
    println("Took $time ms to restore dr1_data_us.wad")
}

fun compare() {
    val time = measureTimeMillis {
        val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad")
        val backupWadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad copy")

        val wad = WAD(FileDataSource(wadFile))
        val backupWad = WAD(FileDataSource(backupWadFile))

        println(wad.directories.count())
        println(backupWad.directories.count())

        println(wad.directories.flatMap(WADSubdirectoryEntry::subfiles).count())
        println(backupWad.directories.flatMap(WADSubdirectoryEntry::subfiles).count())

        println(wad.files.count())
        println(backupWad.files.count())

        println(String(wad.spiralHeader ?: "null".toByteArray()))

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

        if (wad.dataOffset != backupWad.dataOffset)
            println("Data offsets are not equal (${wad.dataOffset} ≠ ${backupWad.dataOffset})")
    }
    println("Took $time ms")

    Thread.sleep(1000)
}

fun patch() {
//    var patchTime = measureTimeMillis {
//        val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad")
//        val backupWadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data copy.wad")
//        val wad = WAD(FileDataSource(backupWadFile))
//        //wad.extractToDirectory(File("functional"))
//        val customWad = make<CustomWAD> {
//            major(11037)
//            minor(1)
//
//            headerFile(File("/Users/undermybrella/Bee Movie Script.txt").readBytes())
//
//            wad(wad)
//            data("Dr1/data/all/cg/bustup_00_00.tga", SpiralFormats.convert(SpiralFormats.PNG, SpiralFormats.TGA, FileDataSource(File("processing/bustup_barry.png"))))
//            data("Dr1/data/all/cg/bustup_00_01.tga", SpiralFormats.convert(SpiralFormats.PNG, SpiralFormats.TGA, FileDataSource(File("processing/bustup_barry.png"))))
//        }
//        customWad.compile(FileOutputStream(wadFile))
//    }
//    println("Patching dr1_data took $patchTime ms")

    val patchTime = measureTimeMillis {
        val wadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data_us.wad")
        val backupWadFile = File("/Users/undermybrella/Library/Application Support/Steam/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data_us copy.wad")
        val wad = WAD(FileDataSource(backupWadFile))
        //wad.extractToDirectory(File("functional"))

        val pak = Pak(wad.files.first { (name) -> name.endsWith("bin_title_l.pak") })
        val menu = make<CustomPak> {
            dataSource(Pak(pak.files[0]).files[0])
            dataSource(FunctionDataSource { SpiralFormats.convert(SpiralFormats.PNG, SpiralFormats.TGA, FileDataSource(File("processing/tilted.png"))) })
            Pak(pak.files[0]).files.toTypedArray().copyFrom(2).forEach { dataSource(it) }
        }
        val menuData = ByteArrayOutputStream()
        menu.compile(menuData)

        val customPak = make<CustomPak> {
            dataSource(FunctionDataSource { menuData.toByteArray() })
            pak.files.toTypedArray().copyFrom(1).forEach { dataSource(it) }
        }

        val customPakData = ByteArrayOutputStream()
        customPak.compile(customPakData)

//        val lin = Lin(wad.files.first { (name) -> name.endsWith("e00_001_000.lin") })
//        val customLin = make<CustomLin> {
//            type(lin.linType.toInt())
//            lin.entries.forEach { script ->
//                if (script is TextEntry)
//                    entry(script.text.mapIndexed { index, char -> if (index % 2 == 0) ';' else char }.joinToString(""))
//                else
//                    entry(script)
//            }
//        }
//        val customLinData = ByteArrayOutputStream()
//        customLin.compile(customLinData)

        val nonstopNumber = "Dr1/data/us/bin/nonstop_01_001.dat"
        val nonstop = NonstopDebate(wad.files.first { (name) -> name == nonstopNumber })
        val customNonstop = make<CustomNonstopDebate> {
            this.secondsForDebate = nonstop.secondsForDebate / 4
            nonstop.sections.forEach { section ->
                section.rotation = 90
                section.rotationSpeed = 2
                section.sprite++
                this.section(section)
            }
        }

        val customNonstopData = make<ByteArrayOutputStream> { customNonstop.compile(this) }

        val customWad = make<CustomWAD> {
            major(11037)
            minor(1)

            headerFile(File("/Users/undermybrella/Bee Movie Script.txt").readBytes())

            wad(wad)
            data("Dr1/data/us/bin/bin_title_l.pak", ByteArrayDataSource(customPakData.toByteArray()))
            data(nonstopNumber, ByteArrayDataSource(customNonstopData.toByteArray()))
            //data("Dr1/data/us/script/e00_001_000.lin", FunctionDataSource(customLinData::toByteArray))
        }
        customWad.compile(FileOutputStream(wadFile))
    }
    println("Patching dr1_data_us took $patchTime ms")
}