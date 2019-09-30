package info.spiralframework.osl

import info.spiralframework.antlr.osl.OpenSpiralLexer
import info.spiralframework.antlr.osl.OpenSpiralParser
import info.spiralframework.base.locale.CustomLocaleBundle
import info.spiralframework.formats.archives.SPC
import info.spiralframework.formats.customLin
import info.spiralframework.formats.customSPC
import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.scripting.EnumWordScriptCommand
import info.spiralframework.formats.scripting.WordScriptFile
import info.spiralframework.formats.scripting.lin.*
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.json.JsonType
import info.spiralframework.json.parseJsonFromAntlr
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.PrintStream
import java.util.*

object OSLProxy {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args[0] == "-x" || args[0] == "--extract") {
            convertToOsl(args[1])
            println("Done!")
        } else if (args[1] == "-o" || args[0] == "--compile") {
            parseOsl(args[1], args[2])
            println("Done!")
        } else {
            println("Unknown operation ${args[0]}")
        }
    }

    fun convertToOsl(path: String) {
        val loadedWrd = WordScriptFile.unsafe(V3, File(path)::inputStream)
        val out = PrintStream(path.replace("wrd", "osl"))
        out.println("OSL Script")
        out.println()
//        loadedWrd.labels.forEach { label -> out.println("//Label: $label") }
//        out.println()
//        loadedWrd.parameters.forEach { parameter -> out.println("//Parameter: $parameter") }
//        out.println()
//        loadedWrd.strings.forEach { string -> out.println("//String: $string") }
//        out.println()
        loadedWrd.entries.forEach { section ->
            section.forEach { entry ->
                val name = V3.opCodes[entry.opCode]?.first?.firstOrNull() ?: "0x${entry.opCode.toString(16).toUpperCase().padStart(2, '0')}"
                out.print("$name|")
                out.println(entry.rawArguments.mapIndexed { index, arg ->
                    when (V3.opCodeCommandEntries[entry.opCode]?.getOrNull(index) ?: EnumWordScriptCommand.PARAMETER) {
                        EnumWordScriptCommand.LABEL -> loadedWrd.labels.getOrNull(arg)?.let { "@{$it}" } ?: arg.toString()
                        EnumWordScriptCommand.PARAMETER -> loadedWrd.parameters.getOrNull(arg)?.let { "%{$it}" } ?: arg.toString()
                        EnumWordScriptCommand.STRING -> loadedWrd.strings.getOrNull(arg)?.let { "\"$it\"" } ?: arg.toString()
                        EnumWordScriptCommand.RAW -> arg.toString()
                    }
                }.joinToString(","))
            }
        }
    }

    fun parseOsl(path: String, resultSpcPath: String) {
        val input = CharStreams.fromFileName(path.replace("wrd", "osl"))
        val lexer = OpenSpiralLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = OpenSpiralParser(tokens)
        val tree = parser.script()
        val visitor = OSLVisitor()
        visitor.game = V3
//        val v3GameVisitor = requireNotNull(visitor.gameVisitor as? V3Visitor)

//        val base = WordScriptFile.unsafe(V3, File("C:\\Users\\under\\Downloads\\DRV3-Tools_v0.3.2_beta\\chap1_US\\c01_202_000.wrd")::inputStream)
//        v3GameVisitor.customWrd.labels.addAll(base.labels.toList().shuffled())
//        v3GameVisitor.customWrd.parameters.addAll(base.parameters.toList().shuffled())
//        v3GameVisitor.customWrd.strings.addAll(base.strings.toList().shuffled())

        val result = visitor.visitScript(tree)
        println(result)
        require(result is OSLUnion.CustomWrdType)
        File(path).outputStream().use(result.wrd::compile)

        val resultSpcFile = File(resultSpcPath)
        val baseSpcPath = resultSpcFile.readBytes()
        val baseSpc = SPC.unsafe(baseSpcPath::inputStream)
        val spc = customSPC {
            add(baseSpc)
            add(path.substringAfterLast('/').substringAfterLast('\\'), File(path))
        }

        resultSpcFile.outputStream().use(spc::compile)
    }

    fun locale() {
//        val input = CharStreams.fromString(buildString {
//            val data = SpiralLocale::class.java.classLoader.getResourceAsStream("SpiralBase.properties")?.let(InputStream::readBytes)
//                    ?: return@buildString
//            appendln(String(data, Charsets.UTF_8))
//        })
//        val lexer = OSLLocaleLexer(input)
//        val tokens = CommonTokenStream(lexer)
//        val parser = OSLLocaleParser(tokens)
//        val tree = parser.locale()
//        val visitor = LocaleVisitor()
//        visitor.visit(tree)
//        val bundle = visitor.createResourceBundle()
//        println(bundle.keys.toList())
//
//        SpiralLocale.PROMPT_AFFIRMATIVE
//
//        println()

        val bundle = OSLLocaleBundle.loadBundle("SpiralBase")
        println((bundle as? CustomLocaleBundle)?.loadWithLocale(Locale.CHINESE)?.locale)


//        println(visitor.visit(tree))
//        val visitor = OSLVisitor()
//        println(visitor.visitScript(tree).represent())
    }

    fun osl() {
        DataHandler.streamToMap = { stream -> (parseJsonFromAntlr(stream) as? JsonType.JsonObject)?.toMap() }

        val input = CharStreams.fromFileName("osl-2/src/main/antlr/tests/NonstopDebate.osl")
        val lexer = OpenSpiralLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = OpenSpiralParser(tokens)
        val tree = parser.script()
        val visitor = OSLVisitor()
        //visitor.game = DR1
        val result = visitor.visitScript(tree)
        println(result.represent())

        when (result) {
            is OSLUnion.CustomWrdType -> {
                File("custom.wrd").outputStream().use(result.wrd::compile)

                val loadedWrd = WordScriptFile(V3, File("custom.wrd")::inputStream)
                println(loadedWrd)
            }
        }

//        when (val result = visitor.visit(tree)) {
//            is OSLUnion.CustomLinType -> {
//                result.lin.writeTextBOM = true
//                File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa Another Episode Ultra Despair Girls\\data\\_scr\\en\\e00_000_200.lin").outputStream().use(result.lin::compile)
//            }
//        }
    }

    fun customLinStuff() {
        val lin = customLin {
            add(ScreenFadeEntry(fadeIn = true, colour = 0, frameDuration = 24))

            add(SetFlagEntry(0, 4, 1))
            add(SetFlagEntry(0, 5, 1))
            add(SetFlagEntry(0, 6, 1))
            add(SetFlagEntry(0, 7, 1))

//            add(TextEntry("Awaiting input...", -1))
//            add(WaitForInputEntry.DR1)
//
//            for (i in 0 until 255) {
//                add(TextEntry("<CLT $i>CLT $i<CLT>", -1))
//                add(UnknownEntry(0x33, intArrayOf(6, 0, 0, 60)))
//            }

            add(TextEntry("Of course.\nI'm<CLT 030 an esper<CLT> test", -1))
            add(WaitForInputEntry.DR1)

            add(StopScriptEntry())
            add(StopScriptEntry())
        }

        File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa Trigger Happy Havoc\\content\\Dr1\\data\\us\\script\\e00_001_000.lin")
                .outputStream().use(lin::compile)
    }
}