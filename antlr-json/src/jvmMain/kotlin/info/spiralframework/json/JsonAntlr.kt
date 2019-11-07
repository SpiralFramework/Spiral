package info.spiralframework.json

import info.spiralframework.antlr.json.JsonLexer
import info.spiralframework.antlr.json.JsonParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.InputStream

fun parseJsonFromAntlr(stream: InputStream): JsonType {
    val input = CharStreams.fromStream(stream)
    val lexer = JsonLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = JsonParser(tokens)
    val tree = parser.file()

    val json = JsonVisitor()
    return json.visit(tree)
}