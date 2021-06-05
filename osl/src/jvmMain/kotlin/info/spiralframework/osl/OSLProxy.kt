package info.spiralframework.osl

import dev.brella.kornea.base.common.use
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.io.common.flow.BinaryInputFlow
import dev.brella.kornea.io.common.flow.BinaryOutputFlow
import dev.brella.kornea.io.common.flow.BufferedOutputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.io.jvm.files.AsyncFileInputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileOutputFlow
import info.spiralframework.antlr.osl.OpenSpiralLexer
import info.spiralframework.antlr.osl.OpenSpiralParser
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.games.UnsafeDRv3
import info.spiralframework.formats.common.games.UnsafeDr1
import info.spiralframework.formats.common.games.UnsafeDr2
import info.spiralframework.formats.common.scripting.lin.UnsafeLinScript
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.wrd.UnsafeWordScript
import info.spiralframework.formats.jvm.defaultSpiralContextWithFormats
import info.spiralframework.osb.common.OpenSpiralBitcodeBuilder
import info.spiralframework.osb.common.compileLinFromBitcode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.PrintStream

object OSLProxy {
    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking(Dispatchers.IO) {
            with(defaultSpiralContextWithFormats()) {
                val dr1 = UnsafeDr1()
                val dr2 = UnsafeDr2()

                val game = if (args.any { str -> str == "--dr2" }) dr2 else dr1

                suspend fun transpile(base: File, filename: String) {
                    val linFileSource = AsyncFileDataSource(File(base, "$filename.lin"))
                    val lin = UnsafeLinScript(dr1, linFileSource)
                    val out = AsyncFileOutputFlow(File(base, "$filename.osl"))

                    val transpiler = LinTranspiler(lin)
                    transpiler.transpile(out)
                }

                suspend fun compile(base: File, filename: String) {
                    val input = CharStreams.fromPath(File(base, "$filename.osl").toPath())
                    val lexer = OpenSpiralLexer(input)
                    val tokens = CommonTokenStream(lexer)
                    val parser = OpenSpiralParser(tokens)
                    val tree = parser.script()
                    BufferedOutputFlow(AsyncFileOutputFlow(File(base, "$filename.osb"))).use { binary ->
                        val visitor = OSLVisitor()
                        val script = visitor.visitScript(tree)
                        val builder = OpenSpiralBitcodeBuilder(binary)
                        script.writeToBuilder(builder)
                    }
                    val linOut = AsyncFileOutputFlow(File(base, "$filename.lin"))
                    linOut.compileLinFromBitcode(this, UnsafeDr1(), AsyncFileInputFlow(File(base, "$filename.osb")))
                }

                var i = 0
                while (i < args.size) {
                    when (args[i]) {
                        in arrayOf("-x", "--extract") -> {
                            while (!args[i + 1].startsWith('-')) {
                                val name = args[++i]
                                val file = File(name)
                                transpile(file.parentFile, file.nameWithoutExtension)
                            }
                        }

                        in arrayOf("-o", "--compile", "--object") -> {
                            while (!args[i + 1].startsWith('-')) {
                                val name = args[++i]
                                val file = File(name)
                                compile(file.parentFile, file.nameWithoutExtension)
                            }
                        }

                        else -> {
                            println("Unknown operation ${args[i]}")
                        }
                    }
                }
            }
        }
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    suspend fun SpiralContext.convertToOsl(path: String) {
        val drv3 = UnsafeDRv3()
        val loadedWrd = UnsafeWordScript(drv3, AsyncFileDataSource(File(path)))
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
        val input = CharStreams.fromString("OSL Script\n0x01|\"Test\"\nval test = 1.23\nMakoto: \"Hello, world!\"\nText|\"OwO What's \$test?\"")
        val lexer = OpenSpiralLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = OpenSpiralParser(tokens)
        val tree = parser.script()
        val binary = BinaryOutputFlow()
        val visitor = OSLVisitor()
        val script = visitor.visitScript(tree)
        val builder = OpenSpiralBitcodeBuilder(binary)
        script.writeToBuilder(builder)
        val linOut = BinaryOutputFlow()
        linOut.compileLinFromBitcode(this, UnsafeDr1(), BinaryInputFlow(binary.getData()))
        val lin = UnsafeLinScript(UnsafeDr1(), BinaryDataSource(linOut.getData()))
        println(lin)
    }
}