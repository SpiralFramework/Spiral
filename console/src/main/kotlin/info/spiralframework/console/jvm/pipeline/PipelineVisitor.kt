package info.spiralframework.console.jvm.pipeline

import dev.brella.knolus.ExpressionOperator
import dev.brella.knolus.KnolusUnion
import dev.brella.knolus.successVar
import dev.brella.knolus.transform.*
import dev.brella.knolus.types.*
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.toolkit.common.mapToArray
import info.spiralframework.antlr.pipeline.PipelineParser
import info.spiralframework.antlr.pipeline.PipelineParserBaseVisitor

class PipelineVisitor(val restrictions: KnolusTransVisitorRestrictions<*>, val parser: PipelineParser, val delegate: TransKnolusParserVisitor) : PipelineParserBaseVisitor<KorneaResult<KnolusUnion>>() {
    override fun visitScope(ctx: PipelineParser.ScopeContext): KorneaResult<KnolusUnion.ScopeType> {
//        if (restrictions.canVisitScope(ctx) !is KorneaResult.Success<*>)
//            return KorneaResult.errorAsIllegalState(TransKnolusVisitor.SCOPE_VISIT_DENIED, "Restriction denied scope visit")

        val lines = ctx.line()

        if (lines.isEmpty()) return KorneaResult.empty()

        return lines.foldResults(this::visitLine)
            .filter(List<KnolusUnion>::isNotEmpty)
            .map { list -> KnolusUnion.ScopeType(list.toTypedArray()) }
    }

    override fun visitLine(ctx: PipelineParser.LineContext): KorneaResult<KnolusUnion> {
        return ctx.functionCall()?.let(this::visitFunctionCall)
               ?: ctx.scriptCall()?.let(this::visitScriptCall)
               ?: ctx.declareFunction()?.let(this::visitDeclareFunction)
               ?: ctx.memberFunctionCall()?.let(this::visitMemberFunctionCall)
               ?: ctx.setVariableValue()?.let(this::visitSetVariableValue)
               ?: ctx.declareVariable()?.let(this::visitDeclareVariable)
               ?: KorneaResult.errorAsIllegalState(
                   TransKnolusVisitor.NO_VALID_LINE_STATEMENT,
                   "No valid variable value in \"${ctx.text}\" (${ctx.toString(parser)})"
               )
    }

    override fun visitDeclareVariable(ctx: PipelineParser.DeclareVariableContext): KorneaResult<KnolusUnion.DeclareVariableAction> = delegate.visitDeclareVariable(TransDeclareVariableBlueprint(ctx))
    override fun visitSetVariableValue(ctx: PipelineParser.SetVariableValueContext): KorneaResult<KnolusUnion.AssignVariableAction> = delegate.visitSetVariableValue(TransAssignVariableBlueprint(ctx))
    override fun visitDeclareFunction(ctx: PipelineParser.DeclareFunctionContext): KorneaResult<KnolusUnion.FunctionDeclaration> = delegate.visitDeclareFunction(TransDeclareFunctionBlueprint(ctx))
    override fun visitDeclareFunctionBody(ctx: PipelineParser.DeclareFunctionBodyContext): KorneaResult<KnolusUnion.ScopeType> = delegate.visitDeclareFunctionBody(TransDeclareFunctionBodyBlueprint(ctx))

    override fun visitFunctionCall(ctx: PipelineParser.FunctionCallContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> =
        delegate.visitFunctionCall(TransFunctionCallBlueprint(ctx))

    override fun visitFunctionCallParameter(ctx: PipelineParser.FunctionCallParameterContext): KorneaResult<KnolusUnion.FunctionParameterType> =
        delegate.visitFunctionCallParameter(TransFunctionCallParameterBlueprint(ctx))

    override fun visitMemberFunctionCall(ctx: PipelineParser.MemberFunctionCallContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>> =
        delegate.visitMemberFunctionCall(TransMemberFunctionCallBlueprint(ctx))

    override fun visitVariableReference(ctx: PipelineParser.VariableReferenceContext): KorneaResult<KnolusUnion.VariableValue<KnolusVariableReference>> =
        delegate.visitVariableReference(TransVariableReferenceBlueprint(ctx))

    override fun visitMemberVariableReference(ctx: PipelineParser.MemberVariableReferenceContext): KorneaResult<KnolusUnion.VariableValue<KnolusPropertyReference>> =
        delegate.visitMemberVariableReference(TransMemberVariableReferenceBlueprint(ctx))

    override fun visitVariableValue(ctx: PipelineParser.VariableValueContext): KorneaResult<KnolusUnion.VariableValue<KnolusTypedValue>> =
        delegate.visitVariableValue(TransVariableValueBlueprint(ctx))

    override fun visitStringValue(ctx: PipelineParser.StringValueContext): KorneaResult<KnolusUnion.VariableValue<KnolusTypedValue>> =
        delegate.visitStringValue(TransStringValueBlueprint(ctx))

//    override fun visitIntValue(ctx: PipelineParser.IntValueContext): KorneaResult<KnolusUnion.VariableValue<KnolusTypedValue>> =
//        delegate.visitVariableValue(TransVariableValueBlueprint(ctx))

    override fun visitArray(ctx: PipelineParser.ArrayContext): KorneaResult<KnolusUnion.VariableValue<KnolusArray<out KnolusTypedValue>>> = delegate.visitArray(TransArrayBlueprint(ctx))
    override fun visitArrayContents(ctx: PipelineParser.ArrayContentsContext): KorneaResult<KnolusUnion.ArrayContents> = delegate.visitArrayContents(TransArrayContentsBlueprint(ctx))
    override fun visitBool(ctx: PipelineParser.BoolContext): KorneaResult<KnolusUnion.VariableValue<KnolusBoolean>> = delegate.visitBool(TransBooleanBlueprint(ctx))
    override fun visitQuotedString(ctx: PipelineParser.QuotedStringContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyString>> = delegate.visitQuotedString(TransQuotedStringBlueprint(ctx))
    override fun visitQuotedCharacter(ctx: PipelineParser.QuotedCharacterContext): KorneaResult<KnolusUnion.VariableValue<KnolusChar>> = delegate.visitQuotedCharacter(TransQuotedCharacterBlueprint(ctx))

    override fun visitNumber(ctx: PipelineParser.NumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusNumericalType>> = delegate.visitNumber(TransNumberBlueprint(ctx))
    override fun visitWholeNumber(ctx: PipelineParser.WholeNumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusNumericalType>> = delegate.visitWholeNumber(TransWholeNumberBlueprint(ctx))
    override fun visitDecimalNumber(ctx: PipelineParser.DecimalNumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusDouble>> = delegate.visitDecimalNumber(TransDecimalNumberBlueprint(ctx))
    override fun visitExpression(ctx: PipelineParser.ExpressionContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyExpression>> = delegate.visitExpression(TransExpressionBlueprint(ctx))

    override fun visitExpressionOperation(ctx: PipelineParser.ExpressionOperationContext): KorneaResult<ExpressionOperator> =
        KorneaResult.successOrEmpty(
            ctx.EXPR_PLUS()?.let { ExpressionOperator.PLUS }
            ?: ctx.EXPR_MINUS()?.let { ExpressionOperator.MINUS }
            ?: ctx.EXPR_MULTIPLY()?.let { ExpressionOperator.MULTIPLY }
            ?: ctx.EXPR_DIVIDE()?.let { ExpressionOperator.DIVIDE }
            ?: ctx.EXPR_EXPONENTIAL()?.let { ExpressionOperator.EXPONENTIAL },
            null
        )

    override fun visitScriptCall(ctx: PipelineParser.ScriptCallContext): KorneaResult<KnolusUnion> {
        val functionName = ctx.scriptName.text.replace(" ", "")
        return ctx.scriptCallParameters()
            .scriptParameter()
            .foldResults(this::visitScriptParameter)
            .flatMap { params ->
                val array = arrayOfNulls<KnolusUnion.FunctionParameterType>(params.sumOf { it.values.size })
                var i = 0
                params.forEach { union ->
                    union.values.copyInto(array, destinationOffset = i)
                    i += union.values.size
                }
                KorneaResult.successVar(KnolusLazyFunctionCall(functionName, array as Array<KnolusUnion.FunctionParameterType>))
            }
    }

    override fun visitScriptParameter(ctx: PipelineParser.ScriptParameterContext): KorneaResult<KnolusUnion.MultiValue<KnolusUnion.FunctionParameterType>> {
        ctx.SCRIPT_CALL_FLAG_GROUP()?.let { group ->
            return KorneaResult.success(KnolusUnion.MultiValue(group.text.trimStart('-').toList().mapToArray { flagValue -> KnolusUnion.FunctionParameterType(name = "flag_$flagValue", KnolusBoolean(true)) }), null)
        }

        ctx.SCRIPT_CALL_FLAG()?.let { flag ->
            return KorneaResult.success(KnolusUnion.MultiValue(arrayOf(KnolusUnion.FunctionParameterType(flag.text.trimStart('-'), KnolusBoolean(true)))), null)
        }

        ctx.variableValue()?.let(this::visitVariableValue)?.let { result ->
            return result.map { union -> KnolusUnion.MultiValue(arrayOf(KnolusUnion.FunctionParameterType(null, union.value))) }
        }
        return KorneaResult.empty()
    }
}