package info.spiralframework.json

import info.spiralframework.antlr.json.JsonParser
import info.spiralframework.antlr.json.JsonParserBaseVisitor
import org.antlr.v4.runtime.tree.TerminalNode

class JsonVisitor : JsonParserBaseVisitor<JsonType>() {
    override fun visitObject(ctx: JsonParser.ObjectContext): JsonType {
        return JsonType.JsonObject(
            ctx.pair()
                .map { pairCtx -> visitString(pairCtx.string()).string to visit(pairCtx.value()) }
                .toMap()
        )
    }

    override fun visitArray(ctx: JsonParser.ArrayContext): JsonType {
        return JsonType.JsonArray(ctx.value().map { valueCtx -> visit(valueCtx) })
    }

    override fun visitString(ctx: JsonParser.StringContext): JsonType.JsonString {
        val string = buildString {
            ctx.children.forEach { node ->
                if (node !is TerminalNode)
                    return@forEach

                when (node.symbol.type) {
                    JsonParser.ESCAPES -> {
                        when (node.text[1]) {
                            'b' -> append('\b')
                            'f' -> append(0x0C.toChar())
                            'n' -> append('\n')
                            'r' -> append('\r')
                            't' -> append('\t')
                            'u' -> append(node.text.substring(2).toInt(16).toChar())
                        }
                    }
                    JsonParser.STRING_CHARACTERS -> append(node.text)
                }
            }
        }
        return JsonType.JsonString(string)
    }

    override fun visitNumber(ctx: JsonParser.NumberContext): JsonType {
        val text = ctx.text
        if (text.contains('.') || text.contains('e') || text.contains('E'))
            return JsonType.JsonNumber(text.toDouble())
        return JsonType.JsonNumber(text.toLong())
    }

    override fun visitBooleanRule(ctx: JsonParser.BooleanRuleContext): JsonType {
        return JsonType.JsonBoolean(ctx.text!!.toBoolean())
    }

    override fun visitNullRule(ctx: JsonParser.NullRuleContext?): JsonType = JsonType.JsonNull
}