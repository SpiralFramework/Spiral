package info.spiralframework.console.jvm.pipeline

import info.spiralframework.antlr.pipeline.PipelineParser
import info.spiralframework.antlr.pipeline.PipelineParserBaseVisitor
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode

@ExperimentalUnsignedTypes
class PipelineVisitor : PipelineParserBaseVisitor<PipelineUnion?>() {
    override fun visitScope(ctx: PipelineParser.ScopeContext): PipelineUnion.ScopeType =
            PipelineUnion.ScopeType(
                    ctx.children
                            ?.mapNotNull(this::visit)
                            ?.toTypedArray()
                            ?: emptyArray()
            )

    override fun visitAssignVariable(ctx: PipelineParser.AssignVariableContext): PipelineUnion.AssignVariableAction =
            PipelineUnion.AssignVariableAction(ctx.variableName.text, visitVariableValue(ctx.variableValue()), ctx.GLOBAL() != null)

    override fun visitFunctionDeclaration(ctx: PipelineParser.FunctionDeclarationContext): PipelineUnion.FunctionDeclaration {
        val functionName = ctx.functionName.text
        val functionParameters = ctx.parameters.map(Token::getText).toTypedArray()
        val functionBody = ctx.functionBody()?.scope()?.let(this::visitScope)

        return PipelineUnion.FunctionDeclaration(functionName, functionParameters, ctx.GLOBAL() != null, functionBody)
    }


    override fun visitTrueLiteral(ctx: PipelineParser.TrueLiteralContext): PipelineUnion.VariableValue.BooleanType =
            PipelineUnion.VariableValue.BooleanType(true)

    override fun visitFalseLiteral(ctx: PipelineParser.FalseLiteralContext): PipelineUnion.VariableValue.BooleanType =
            PipelineUnion.VariableValue.BooleanType(false)

    override fun visitNullLiteral(ctx: PipelineParser.NullLiteralContext): PipelineUnion.VariableValue.NullType =
            PipelineUnion.VariableValue.NullType

    override fun visitInteger(ctx: PipelineParser.IntegerContext): PipelineUnion.VariableValue.IntegerType =
            PipelineUnion.VariableValue.IntegerType(ctx.INTEGER().text.toIntVariable())

    override fun visitDecimalNumber(ctx: PipelineParser.DecimalNumberContext): PipelineUnion.VariableValue.DecimalType =
            PipelineUnion.VariableValue.DecimalType(ctx.DECIMAL_NUMBER().text.toDouble())

    override fun visitQuotedString(ctx: PipelineParser.QuotedStringContext): PipelineUnion.VariableValue.StringComponents {
        val components: MutableList<PipelineUnion.StringComponent> = ArrayList()
        val builder = StringBuilder()
        ctx.children.forEach { node ->
            when (node) {
                is TerminalNode -> {
                    when (node.symbol.type) {
                        PipelineParser.ESCAPES -> {
                            when (val c = node.text[1]) {
                                'b' -> builder.append('\b')
                                'f' -> builder.append(0x0C.toChar())
                                'n' -> builder.append('\n')
                                'r' -> builder.append('\r')
                                't' -> builder.append('\t')
                                'u' -> builder.append(node.text.substring(2).toInt(16).toChar())
                                else -> builder.append(c)
                            }
                        }
                        PipelineParser.STRING_CHARACTERS -> builder.append(node.text)
                        PipelineParser.STRING_WHITESPACE -> builder.append(node.text)
                        PipelineParser.QUOTED_STRING_LINE_BREAK -> builder.append("\n")
                        PipelineParser.QUOTED_STRING_LINE_BREAK_NO_SPACE -> builder.append("\n")
                    }
                }
                is PipelineParser.VariableReferenceContext -> {
                    if (builder.isNotEmpty()) {
                        components.add(PipelineUnion.StringComponent.RawText(builder.toString()))
                        builder.clear()
                    }
                    components.add(PipelineUnion.StringComponent.VariableReference(node.variableName.text))
                }
            }
        }

        if (builder.isNotEmpty()) {
            components.add(PipelineUnion.StringComponent.RawText(builder.toString()))
            builder.clear()
        }

        return PipelineUnion.VariableValue.StringComponents(components.toTypedArray())
    }

    override fun visitVariableReference(ctx: PipelineParser.VariableReferenceContext): PipelineUnion.VariableValue.VariableReferenceType =
            PipelineUnion.VariableValue.VariableReferenceType(ctx.variableName.text)

    private fun visitWrappedFunctionCall(ctx: PipelineParser.FunctionCallContext): PipelineUnion.VariableValue.WrappedFunctionCallType =
            PipelineUnion.VariableValue.WrappedFunctionCallType(visitFunctionCall(ctx))

    override fun visitFunctionCall(ctx: PipelineParser.FunctionCallContext): PipelineUnion.FunctionCallAction {
        val functionName = ctx.functionName.text
        val parameters = ctx.functionCallParameters()
                .functionParameter()
                .map(this::visitFunctionParameter)

        return PipelineUnion.FunctionCallAction(functionName, parameters.toTypedArray())
    }

    override fun visitFunctionParameter(ctx: PipelineParser.FunctionParameterContext): PipelineUnion.FunctionParameterType =
            PipelineUnion.FunctionParameterType(ctx.parameterName?.text, visitFunctionVariableValue(ctx.functionVariableValue()))

    override fun visitFunctionVariableValue(ctx: PipelineParser.FunctionVariableValueContext): PipelineUnion.VariableValue =
            visitVariableValue(ctx.variableValue())

    override fun visitWrappedScriptCall(ctx: PipelineParser.WrappedScriptCallContext): PipelineUnion.VariableValue.WrappedScriptCallType =
            PipelineUnion.VariableValue.WrappedScriptCallType(visitScriptCall(ctx.scriptCall()))

    override fun visitScriptCall(ctx: PipelineParser.ScriptCallContext): PipelineUnion.ScriptCallAction {
        val scriptName = ctx.scriptName.text
        val parameters = ctx.scriptCallParameters()
                ?.scriptParameter()
                ?.map(this::visitScriptParameter)
                ?: emptyList()

        return PipelineUnion.ScriptCallAction(scriptName, parameters.toTypedArray())
    }

    override fun visitScriptParameter(ctx: PipelineParser.ScriptParameterContext): PipelineUnion.ScriptParameterType {
        ctx.scriptFlag()?.let(this::visitScriptFlag)?.let { return it }
        ctx.scriptFlagGroup()?.let(this::visitScriptFlagGroup)?.let { return it }

        return PipelineUnion.ScriptParameterType(ctx.parameterName?.text, visitScriptVariableValue(ctx.scriptVariableValue()))
    }

    override fun visitScriptFlag(ctx: PipelineParser.ScriptFlagContext): PipelineUnion.ScriptParameterType =
            PipelineUnion.ScriptParameterType(ctx.IDENTIFIER().text, PipelineUnion.VariableValue.BooleanType(true))

    override fun visitScriptFlagGroup(ctx: PipelineParser.ScriptFlagGroupContext): PipelineUnion.ScriptParameterType =
            PipelineUnion.ScriptParameterType(ctx.IDENTIFIER().text, PipelineUnion.VariableValue.BooleanType(true))

    override fun visitScriptVariableValue(ctx: PipelineParser.ScriptVariableValueContext): PipelineUnion.VariableValue =
            visitVariableValue(ctx.variableValue())

    override fun visitVariableValue(ctx: PipelineParser.VariableValueContext): PipelineUnion.VariableValue {
        ctx.quotedString()?.let(this::visitQuotedString)?.let { return it }
        ctx.trueLiteral()?.let(this::visitTrueLiteral)?.let { return it }
        ctx.falseLiteral()?.let(this::visitFalseLiteral)?.let { return it }
        ctx.integer()?.let(this::visitInteger)?.let { return it }
        ctx.decimalNumber()?.let(this::visitDecimalNumber)?.let { return it }
        ctx.variableReference()?.let(this::visitVariableReference)?.let { return it }
        ctx.nullLiteral()?.let(this::visitNullLiteral)?.let { return it }
        ctx.functionCall()?.let(this::visitWrappedFunctionCall)?.let { return it }
        ctx.wrappedScriptCall()?.let(this::visitWrappedScriptCall)?.let { return it }
        ctx.expression()?.let(this::visitExpression)?.let { return it }

        error("Invalid variable value $ctx")
    }

    override fun visitExpression(ctx: PipelineParser.ExpressionContext): PipelineUnion.VariableValue.ExpressionType {
        val starting = visitVariableValue(ctx.startingValue)
        val ops = Array(ctx.exprOps.size) { i ->
            visitExpressionOperation(ctx.exprOps[i]) to visitVariableValue(ctx.exprVals[i])
        }

        return PipelineUnion.VariableValue.ExpressionType(starting, ops)
    }

    override fun visitExpressionOperation(ctx: PipelineParser.ExpressionOperationContext): PipelineUnion.ExpressionOperation {
        ctx.EXPR_PLUS()?.let { return PipelineUnion.ExpressionOperation.PLUS }
        ctx.EXPR_MINUS()?.let { return PipelineUnion.ExpressionOperation.MINUS }
        ctx.EXPR_DIVIDE()?.let { return PipelineUnion.ExpressionOperation.DIVIDE }
        ctx.EXPR_MULTIPLY()?.let { return PipelineUnion.ExpressionOperation.MULTIPLY }

        error("Invalid expression operation $ctx")
    }

    fun String.toIntVariable(): Int = when {
        startsWith("0b") -> substring(2).toInt(2)
        startsWith("0o") -> substring(2).toInt(8)
        startsWith("0x") -> substring(2).toInt(16)
        startsWith("0d") -> substring(2).toInt()
        else -> toInt()
    }

    fun String.toLongVariable(): Long = when {
        startsWith("0b") -> substring(2).toLong(2)
        startsWith("0o") -> substring(2).toLong(8)
        startsWith("0x") -> substring(2).toLong(16)
        startsWith("0d") -> substring(2).toLong()
        else -> toLong()
    }

    override fun shouldVisitNextChild(node: RuleNode, currentResult: PipelineUnion?): Boolean {
        return currentResult == null
    }
}