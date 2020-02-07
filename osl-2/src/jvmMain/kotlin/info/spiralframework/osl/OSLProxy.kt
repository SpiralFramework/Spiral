package info.spiralframework.osl

import info.spiralframework.antlr.osl.OpenSpiralLexer
import info.spiralframework.antlr.osl.OpenSpiralParser
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.games.UnsafeDRv3
import info.spiralframework.formats.common.scripting.wrd.UnsafeWordScript
import info.spiralframework.formats.jvm.defaultSpiralContextWithFormats
import info.spiralframework.osb.common.OpenSpiralBitcodeVisitor
import info.spiralframework.osb.common.parseOpenSpiralBitcode
import kotlinx.coroutines.runBlocking
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.BinaryOutputFlow
import org.abimon.kornea.io.jvm.files.FileDataSource
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.PrintStream

object OSLProxy {
    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val context = defaultSpiralContextWithFormats()
//            if (args[0] == "-x" || args[0] == "--extract") {
//                context.convertToOsl(args[1])
//                println("Done!")
//            } else if (args[0] == "-o" || args[0] == "--compile") {
//                context.parseOsl(args[1], args[2])
//                println("Done!")
//            } else {
                context.parseOsl("", "")
                println("Unknown operation ${args[0]}")
//            }
        }
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    suspend fun SpiralContext.convertToOsl(path: String) {
        val drv3 = UnsafeDRv3()
        val loadedWrd = UnsafeWordScript(drv3, FileDataSource(File(path)))
        val out = PrintStream(path.replaceAfterLast('.', "osl"))
        out.println("OSL Script")
        out.println()
//        loadedWrd.labels.forEach { label -> out.println("//Label: $label") }
//        out.println()
//        loadedWrd.parameters.forEach { parameter -> out.println("//Parameter: $parameter") }
//        out.println()
//        loadedWrd.strings.forEach { string -> out.println("//String: $string") }
//        out.println()
        loadedWrd.scriptDataBlocks.forEach { section ->
            section.forEach { entry ->
                val name = drv3.wrdOpcodeMap[entry.opcode]?.names?.firstOrNull() ?: entry.opcode.toHexString()
                out.println("$name|${entry.arguments.joinToString()}")
            }
        }
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    suspend fun SpiralContext.parseOsl(path: String, resultSpcPath: String) {
        val input = CharStreams.fromString("OSL Script\n0x01|1, 2, 3\nText|2, 3, 4\n0x03|3, %{4}, \"F\\ni\\u0020v\$e\"")
        val lexer = OpenSpiralLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = OpenSpiralParser(tokens)
        val tree = parser.script()
        val binary = BinaryOutputFlow()
        val visitor = OSLVisitor(binary)
        val result = visitor.visitScript(tree)
        BinaryInputFlow(binary.getData())
                .parseOpenSpiralBitcode(this, OpenSpiralBitcodeVisitor.DEBUG)
    }
}