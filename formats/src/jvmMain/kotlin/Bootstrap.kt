
import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.copyFrom
import info.spiralframework.base.common.io.copyTo
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.use
import info.spiralframework.base.common.io.useInputFlow
import info.spiralframework.base.common.logging.CommonSpiralLogger
import info.spiralframework.base.common.text.arbitrarySuspendedProgressBar
import info.spiralframework.base.jvm.io.files.FileDataSource
import info.spiralframework.base.jvm.io.files.FileOutputFlow
import info.spiralframework.formats.common.archives.*
import info.spiralframework.formats.common.archives.srd.BaseSrdEntry
import info.spiralframework.formats.common.archives.srd.SrdArchive
import info.spiralframework.formats.common.audio.CustomWavAudio
import info.spiralframework.formats.common.audio.HighCompressionAudio
import info.spiralframework.formats.common.audio.readAudioSamples
import info.spiralframework.formats.common.compression.decompressCrilayla
import info.spiralframework.formats.common.data.V3NonstopDebate
import info.spiralframework.formats.common.games.UnsafeDr1
import info.spiralframework.formats.common.scripting.CustomLinScript
import info.spiralframework.formats.common.scripting.LinScript
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1CheckFlagEntry
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1TextEntry
import info.spiralframework.formats.defaultSpiralContextWithFormats
import info.spiralframework.formats.scripting.lin.StopScriptEntry
import info.spiralframework.formats.scripting.lin.WaitForInputEntry
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun main() {
    val context = defaultSpiralContextWithFormats()
            .copy(newLogger = CommonSpiralLogger("Bootstrap", errorEnabled = true, infoEnabled = true, warnEnabled = true))

    val duration = measureTime {
        context.testLin()
    }

    println("Took ${duration.toLongMilliseconds()} ms")

//    val source = FileDataSource(File("formats/src/jvmMain/resources/SpiralFormats.properties"))
//    val flow = source.openInputFlow()!!
//    val properties = flow.loadProperties()
//    println(properties.entries.sortedBy(Map.Entry<String, *>::key).joinToString("\n"))
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.testWad() {
    val wad = WadArchive.unsafe(this, FileDataSource(File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa Trigger Happy Havoc\\dr1_data.wad")))!!

    FileOutputFlow(File("test.ogg")).use { out ->
        wad.openFlow(wad["Dr1/data/us/voice/dr1_voice_hca_us.awb.01599.ogg"]!!)?.use(out::copyFrom)
    }

    println(wad.files.joinToString("\n", transform = WadFileEntry::name))
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.testPak() {
    val pak = PakArchive.unsafe(this, FileDataSource(File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa Trigger Happy Havoc\\Dr1\\data\\all\\flash\\fla_502.pak")))

    FileOutputFlow(File("test.tga")).use { out ->
        pak.openFlow(pak[1])?.use(out::copyFrom)
    }
}

@ExperimentalTime
@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.testSpc() {
    val context = this
    val cpk = CpkArchive.unsafe(context, FileDataSource(File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa V3 Killing Harmony\\data\\win\\partition_data_win.cpk")))
    val spc = SpcArchive.unsafe(context, cpk.openDecompressedSource(context, cpk["model/bg/ID770_sch_side_6.SPC"]!!)!!)

    val output = File("spc_output")
    output.mkdirs()

    with(context) {
        spc.files.forEach { entry ->
            FileOutputFlow(File(output, entry.name)).use { out ->
                val flow = arbitrarySuspendedProgressBar(loadingText = "Decompressing...", loadedText = "") { spc.openDecompressedFlow(this, entry)!! }
                flow.use(out::copyFrom)
            }
        }
    }
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.testCpk() {
    val context = this
    val cpk = CpkArchive.unsafe(context, FileDataSource(File("D:\\DR1\\Dr_us.cpk")))

    val outputDir = File("D:\\DrVitaMppFancy")

    cpk.files.distinctBy(CpkFileEntry::directoryName).forEach { entry -> File(outputDir, entry.directoryName).mkdirs() }

//    if (cpk.dataSource.reproducibility.isRandomAccess()) {
    val sortedFiles = cpk.files.sortedBy(CpkFileEntry::fileOffset)
    cpk.dataSource.openInputFlow()?.use { flow ->
        sortedFiles.forEach { entry ->
            FileOutputFlow(File(outputDir, entry.name)).use { out ->
                flow.seek(entry.fileOffset - flow.position().toLong(), InputFlow.FROM_POSITION)

                if (entry.isCompressed) {
                    val binaryOut = BinaryOutputFlow()
                    flow.copyTo(binaryOut, dataSize = entry.fileSize)
                    out.write(decompressCrilayla(binaryOut.getData()))
                } else {
                    flow.copyTo(out, dataSize = entry.fileSize)
                }
            }
        }
    }
//    } else {
//        cpk.files.forEach { entry ->
//            cpk.openDecompressedFlow(context, entry)?.use { flow ->
//                FileOutputFlow(File(outputDir, entry.name)).use(flow::copyToOutputFlow)
//            }
//        }
//    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.testAwb() {
    val context = this
    val awb = AwbArchive.unsafe(context, FileDataSource(File("D:\\DrVitaMppFancy\\Dr1\\data\\all\\bgm\\dr1_bgm_hca.awb")))

    val output = File("D:\\DrVitaMppFancy\\Dr1\\data\\all\\bgm\\awb")
    output.mkdirs()

    awb.files.forEach { entry ->
        FileOutputFlow(File(output, "${entry.id}.hca")).use { out ->
            awb.openFlow(entry)?.use(out::copyFrom)
        }
    }
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.testHca() {
    val context = this
    val hca = HighCompressionAudio.unsafe(context, FileDataSource(File("D:\\DrVitaMppFancy\\Dr1\\data\\all\\bgm\\awb\\0.hca")))
    println(hca)
    val customWav = CustomWavAudio()
    customWav.numberOfChannels = hca.channelCount
    customWav.sampleRate = hca.sampleRate
    customWav.addSamples(requireNotNull(hca.readAudioSamples(context)))

    FileOutputFlow(File("test.wav")).use(customWav::write)
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.testSrd() {
    val context = this
    val spc = SpcArchive.unsafe(context, FileDataSource(File("E:\\Games\\Steam\\steamapps\\common\\Danganronpa V3 Killing Harmony\\data\\win\\model\\chara\\stand_000_000.SPC")))
    val srd = SrdArchive.unsafe(context, spc.openDecompressedSource(context, spc["model.srd"]!!)!!)
    println(srd.entries.joinToString("\n", transform = BaseSrdEntry::classifierAsString))
}

//suspend fun SpiralContext.testDataTable() {
//    val context = this
//    val cpk = CpkArchive.unsafe(context, FileDataSource(File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa V3 Killing Harmony\\data\\win\\partition_data_win.cpk")))
//    val spc = SpcArchive.unsafe(context, cpk.openDecompressedSource(context, cpk["model/bg/ID770_sch_side_6.SPC"]!!)!!)
//
////    val spc = SpcArchive.unsafe(context, FileDataSource(File("E:\\Games\\Steam\\steamapps\\common\\Danganronpa V3 Killing Harmony\\data\\win\\model\\chara\\stand_000_000.SPC")))
//
//}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.testNonstopDebates() {
    val path = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa V3 Killing Harmony\\data\\base_game\\wrd_data\\nonstop_01_US\\nonstop_01_202"
    val nonstopDebate = V3NonstopDebate.unsafe(this, FileDataSource(File("$path.dat")))
    nonstopDebate.sections.forEachIndexed { index, section ->
        println("[Section $index] stand_${section.characterID.toString().padStart(3, '0')}_${section.modelID.toString().padStart(3, '0')}")
//        val data = ByteArray(section.data.size * 2)
//
//        section.data.forEachIndexed { i, num ->
//            data[i * 2 + 0] = (num shr 0).toByte()
//            data[i * 2 + 1] = (num shr 8).toByte()
//        }
//
//        File("${path}_$index.dat").writeBytes(data)
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.testHypothesis() {
    val context = this
    val cpk = CpkArchive.unsafe(context, FileDataSource(File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa V3 Killing Harmony\\data\\backup_cpks\\partition_data_win.cpk")))
    cpk.files.filter { entry -> entry.fileName.endsWith("spc", ignoreCase = true) }.forEach { cpkEntry ->
        requireNotNull(cpk.openDecompressedSource(context, cpkEntry)).use { cpkEntrySource ->
            val spc = SpcArchive.unsafe(context, cpkEntrySource)

            spc.files.filter { entry -> entry.name.endsWith(".srd") }.forEach { spcEntry ->
                requireNotNull(spc.openDecompressedSource(context, spcEntry)).use { spcEntrySource ->
                    val srd = SrdArchive.unsafe(context, spcEntrySource)

                    srd.entries.forEachIndexed { index, entry ->
                        if (entry.classifierAsString == "\$COL") {
                            println("${cpkEntry.name}/${spcEntry.name}/$index features \$COL")
                        } else if (entry.classifierAsString == "\$OVT") {
                            println("${cpkEntry.name}/${spcEntry.name}/$index features \$OVT")
                        } else if (entry.classifierAsString == "\$VTR") {
                            println("${cpkEntry.name}/${spcEntry.name}/$index features \$VTR")
                        } else if (entry.classifierAsString == "\$SKL") {
                            println("${cpkEntry.name}/${spcEntry.name}/$index features \$SKL")
                        }
                    }

//                    srd.entries.filter { entry -> entry.subDataLength > 32u }.forEachIndexed local@{ index, srdEntry ->
//                        val rsi = RSISrdEntry(context, srdEntry.openSubDataSource()) ?: return@local
////                        if (rsi.unk1 == 6) {
////                            if (rsi.unk4 != 0 && rsi.unk6 != 0) {
////                                if (rsi.unk5 != 0 || rsi.unk7 != 0) {
////                                    //We broke our pattern
////                                    println("${cpkEntry.name}/${spcEntry.name}/${srdEntry.classifierAsString} ($index) broke our pattern (06 05 xx xx), ${rsi.unk4}, ${rsi.unk5}, ${rsi.unk6}, ${rsi.unk7}")
////                                }
////
//////                                if (rsi.resourceCount != 0) {
//////                                    //We broke our pattern
//////                                    println("${cpkEntry.name}/${spcEntry.name}/${srdEntry.classifierAsString} ($index) broke our pattern (06 05 xx ${rsi.resourceCount.toString(16).padStart(2, '0')}), ${rsi.unk4}, ${rsi.unk5}, ${rsi.unk6}, ${rsi.unk7}")
//////                                }
////                            }
////                        } else if (rsi.unk1 == 4) {
////                            if (rsi.unk4 != 0 && rsi.unk6 != 0) {
////                                if (rsi.unk5 == 0 || rsi.unk7 == 0) {
////                                    //We broke our pattern
////                                    println("${cpkEntry.name}/${spcEntry.name}/${srdEntry.classifierAsString} ($index) broke our pattern (04 05 xx xx), ${rsi.unk4}, ${rsi.unk5}, ${rsi.unk6}, ${rsi.unk7}")
////                                }
////
//////                                if (rsi.resourceCount != 0) {
//////                                    //We broke our pattern
//////                                    println("${cpkEntry.name}/${spcEntry.name}/${srdEntry.classifierAsString} ($index) broke our pattern (04 05 xx ${rsi.resourceCount.toString(16).padStart(2, '0')}), ${rsi.unk4}, ${rsi.unk5}, ${rsi.unk6}, ${rsi.unk7}")
//////                                }
////                            }
////                        }
////
////                        if (rsi.unk3 == 0xFF) {
////                            if (rsi.resourceCount != 0) {
////                                //We broke our pattern
////                                println("${cpkEntry.name}/${spcEntry.name}/${srdEntry.classifierAsString} ($index) broke our pattern (${rsi.unk1.toString(16).padStart(2, '0')} 05 FF ${rsi.resourceCount.toString(16).padStart(2, '0')})")
////                            }
////                        }
//                    }
                }
            }
        }
    }
}

data class DummyTest(val intArrayMain: IntArray) {
    val intArraySecondary: IntArray = IntArray(16) { it }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.testCustomCpk() {
    warn("Test")
    println("??")
    val baseCpk = CpkArchive.unsafe(this, FileDataSource(File("F:\\Games\\Steam\\steamapps\\common\\Danganronpa Another Episode Ultra Despair Girls\\en\\a3_compressed.cpk")))
    val extractOffsets = ArrayList<Long>()
    baseCpk.header?.let { header ->
        for (i in 0 until header.rowCount.toInt()) {
            header.schema.forEach { column ->
                val columnStorage = when (val type = column.type and UtfTableInfo.COLUMN_STORAGE_MASK) {
                    UtfTableInfo.COLUMN_STORAGE_ZERO -> "[ ZERO ]"
                    UtfTableInfo.COLUMN_STORAGE_PERROW -> "[PERROW]"
                    UtfTableInfo.COLUMN_STORAGE_CONSTANT -> "[CONST ]"
                    else -> "[${type.toString(16).padStart(6, ' ')}]"
                }
                val columnType = when (val type = column.type and UtfTableInfo.COLUMN_TYPE_MASK) {
                    UtfTableInfo.COLUMN_TYPE_STRING -> "[STRING]"
                    UtfTableInfo.COLUMN_TYPE_8BYTE -> "[ LONG ]"
                    UtfTableInfo.COLUMN_TYPE_DATA -> "[ DATA ]"
                    UtfTableInfo.COLUMN_TYPE_FLOAT -> "[FLOAT ]"
                    UtfTableInfo.COLUMN_TYPE_4BYTE -> "[ INT  ]"
                    UtfTableInfo.COLUMN_TYPE_4BYTE2 -> "[ UINT ]"
                    UtfTableInfo.COLUMN_TYPE_2BYTE -> "[SHORT ]"
                    UtfTableInfo.COLUMN_TYPE_2BYTE2 -> "[USHORT]"
                    UtfTableInfo.COLUMN_TYPE_1BYTE -> "[ BYTE ]"
                    UtfTableInfo.COLUMN_TYPE_1BYTE2 -> "[UBYTE ]"
                    else -> "[${type.toString(16).padStart(6, ' ')}]"
                }
                val rowData = header.readRowDataUnsafe(this, i, column)
                val data = when (val data = rowData.data) {
                    is ByteArray -> data.joinToString(" ", prefix = "[", postfix = "]") { byte -> "0x${byte.toInt().and(0xFF).toString(16).padStart(2, '0')}" }
                    else -> data.toString()
                }

                println("[${i.toString().padStart(header.rowCount.toString().length, ' ')}] | [${rowData.name.padStart(20, ' ')}] | $columnStorage | $columnType | ${data}")
            }

//            extractOffsets.add((header.readRowDataUnsafe(this, i, header.getColumn("FileOffset")) as UtfRowData.TypeLong).data)
        }
    }

    println(extractOffsets.sorted().take(8).joinToString())
    println(extractOffsets.sortedDescending().take(8).joinToString())

    println(baseCpk.etocHeader?.let { etoc ->
        println(CpkArchive.convertFromEtocTime((etoc.readRowData(this, 0, etoc.getColumn("UpdateDateTime")) as UtfRowData.TypeLong).data))
    })
    val customCpk = CustomCpkArchive()
    baseCpk.files.forEach { entry ->
        customCpk[entry.name] = baseCpk.openDecompressedSource(this, entry) ?: return@forEach
    }

    FileOutputFlow(File("F:\\Games\\Steam\\steamapps\\common\\Danganronpa Another Episode Ultra Despair Girls\\en\\a3_spiral.cpk")).use { out ->
        customCpk.compile(this, out)
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.testMagicPak() {
    val file = FileDataSource(File("D:\\BST\\Dr1\\data\\all\\bin\\bin_anagram.pak"))
    val pak = PakArchive.unsafe(this, file)
    val subpak = PakArchive.unsafe(this, pak.openSource(pak[0]))
    val tga = subpak.openSource(subpak[1])

    FileOutputFlow(File("D:\\BST\\Dr1\\data\\all\\bin\\bin_anagram.tga")).use { out -> tga.useInputFlow(out::copyFrom) }
}

@ExperimentalStdlibApi
suspend fun SpiralContext.testGames() {
//    val dr1 = Dr1()
//    println(dr1)
    println(Dr1CheckFlagEntry(intArrayOf(0, 1, 0, 7)).conditions())
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.testLin() {
//    val test = BinaryInputFlow(ByteArray(16384) { it.toByte() })
//    val buffered = BufferedInputFlow(test)
//    println(buffered.read())
//    buffered.skip(8190u)
//    println(buffered.peek(23))
//
//    println("------")
//
//    test.seek(0, InputFlow.FROM_BEGINNING)
//    println(test.read())
//    test.skip(8190u)
//    println(test.peek(23))

    val lin = LinScript(this, UnsafeDr1(), FileDataSource(File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa Trigger Happy Havoc\\content\\Dr1\\data\\us\\script\\e00_001_000.lin")))

    val customLinScript = CustomLinScript()

    customLinScript.addEntry(Dr1TextEntry(0))
    customLinScript.addEntry(WaitForInputEntry.DR1)
    customLinScript.addEntry(Dr1TextEntry(1))
    customLinScript.addEntry(WaitForInputEntry.DR1)
    customLinScript.addEntry(StopScriptEntry())

    customLinScript.addText("Hello, World!")
    customLinScript.addText("This is a custom script")

    FileOutputFlow(File("test.lin")).use(customLinScript::compile)

    println(lin)
}