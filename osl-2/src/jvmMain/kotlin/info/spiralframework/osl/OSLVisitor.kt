package info.spiralframework.osl

import info.spiralframework.antlr.osl.OpenSpiralParser
import info.spiralframework.antlr.osl.OpenSpiralParserBaseVisitor
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.freeze
import info.spiralframework.osb.common.OSLUnion
import info.spiralframework.osb.common.OpenSpiralBitcode
import info.spiralframework.osb.common.buildLongReference
import kotlinx.coroutines.runBlocking
import org.antlr.v4.runtime.tree.TerminalNode
import java.lang.IllegalStateException

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class OSLVisitor : OpenSpiralParserBaseVisitor<OSLVisitorUnion>() {
    companion object {
        const val OSL_HEADER_LENGTH = "OSL Script".length
    }

    override fun visitScript(ctx: OpenSpiralParser.ScriptContext): OSLVisitorUnion.Script =
            OSLVisitorUnion.Script(visitHeaderDeclaration(ctx.headerDeclaration()), visitScope(ctx.scope()))

    override fun visitScope(ctx: OpenSpiralParser.ScopeContext): OSLVisitorUnion.Scope {
        return OSLVisitorUnion.Scope(ctx.scriptLine().map(this::visitScriptLine))
    }

    override fun visitScriptLine(ctx: OpenSpiralParser.ScriptLineContext): OSLVisitorUnion {
        ctx.functionCall()?.let(this::visitFunctionCall)?.let { (func) -> return OSLVisitorUnion.AddFunctionCall(func.functionName, func.parameters) }

        return super.visitScriptLine(ctx)
    }

    override fun visitHeaderDeclaration(ctx: OpenSpiralParser.HeaderDeclarationContext): OSLVisitorUnion.Header {
        val rawSemanticVersion = ctx.HEADER_DECLARATION()
                .text
                .substring(OSL_HEADER_LENGTH)
                .takeIf(String::isNotBlank)
                ?.substring(1)
                ?.split('.')
                ?.map(String::trim)
                ?.mapNotNull(String::toIntOrNull)
                ?: return OSLVisitorUnion.Header()

        return OSLVisitorUnion.Header(
                SemanticVersion(
                        rawSemanticVersion.getOrNull(0) ?: 0,
                        rawSemanticVersion.getOrNull(1) ?: 0,
                        rawSemanticVersion.getOrNull(2) ?: 0
                )
        )
    }

    @ExperimentalStdlibApi
    override fun visitBasicDrill(ctx: OpenSpiralParser.BasicDrillContext): OSLVisitorUnion.AddOpcode {
        val opcode = ctx.INTEGER().text.toIntVariable()
        val values = ctx.basicDrillValue()
        return OSLVisitorUnion.AddOpcode(opcode, Array(values.size) { i -> (visitBasicDrillValue(values[i]) as OSLVisitorUnion.Value<*>).union })
    }

    @ExperimentalStdlibApi
    override fun visitBasicDrillNamed(ctx: OpenSpiralParser.BasicDrillNamedContext): OSLVisitorUnion.AddNamedOpcode {
        val opcodeName = ctx.NAME_IDENTIFIER().text.trimEnd('|')
        val values = ctx.basicDrillValue()
        return OSLVisitorUnion.AddNamedOpcode(opcodeName, Array(values.size) { i -> (visitBasicDrillValue(values[i]) as OSLVisitorUnion.Value<*>).union })
    }

    override fun visitFunctionCall(ctx: OpenSpiralParser.FunctionCallContext): OSLVisitorUnion.Value<OSLUnion.FunctionCallType> {
        val functionName = ctx.NAME_IDENTIFIER().text
        val parameters = visitFunctionCallParameters(ctx.functionCallParameters())

        return OSLVisitorUnion.Value(OSLUnion.FunctionCallType(functionName, parameters.unions.toTypedArray()))
    }

    override fun visitRecursiveFuncCall(ctx: OpenSpiralParser.RecursiveFuncCallContext): OSLVisitorUnion.Value<OSLUnion.FunctionCallType> {
        val functionName = ctx.FUNC_CALL_NAME_IDENTIFIER().text
        val parameters = visitFunctionCallParameters(ctx.functionCallParameters())

        return OSLVisitorUnion.Value(OSLUnion.FunctionCallType(functionName, parameters.unions.toTypedArray()))
    }

    override fun visitIfCheckFuncCall(ctx: OpenSpiralParser.IfCheckFuncCallContext): OSLVisitorUnion.Value<OSLUnion.FunctionCallType> {
        val functionName = ctx.IF_CHECK_NAME_IDENTIFIER().text
        val parameters = visitFunctionCallParameters(ctx.functionCallParameters())

        return OSLVisitorUnion.Value(OSLUnion.FunctionCallType(functionName, parameters.unions.toTypedArray()))
    }

    override fun visitIfCheckEquality(ctx: OpenSpiralParser.IfCheckEqualityContext): OSLVisitorUnion.Equality =
            OSLVisitorUnion.Equality(
                    if (ctx.IF_CHECK_EQUALITY_NOT_EQUAL() != null) OpenSpiralBitcode.EQUALITY_NOT_EQUAL
                    else if (ctx.IF_CHECK_EQUALITY_EQUAL() != null) OpenSpiralBitcode.EQUALITY_EQUAL
                    else if (ctx.IF_CHECK_EQUALITY_LESS_THAN_EQUAL_TO() != null) OpenSpiralBitcode.EQUALITY_LESS_THAN_EQUAL_TO
                    else if (ctx.IF_CHECK_EQUALITY_GREATER_THAN_EQUAL_TO() != null) OpenSpiralBitcode.EQUALITY_GREATER_THAN_EQUAL_TO
                    else if (ctx.IF_CHECK_EQUALITY_LESS_THAN() != null) OpenSpiralBitcode.EQUALITY_LESS_THAN
                    else if (ctx.IF_CHECK_EQUALITY_GREATER_THAN() != null) OpenSpiralBitcode.EQUALITY_GREATER_THAN
                    else throw IllegalStateException("No known equality operator")
            )

    override fun visitIfCheckLogical(ctx: OpenSpiralParser.IfCheckLogicalContext): OSLVisitorUnion.Logical =
            OSLVisitorUnion.Logical(
                    if (ctx.IF_CHECK_LOGICAL_AND() != null) OpenSpiralBitcode.LOGICAL_AND
                    else if (ctx.IF_CHECK_LOGICAL_OR() != null) OpenSpiralBitcode.LOGICAL_OR
                    else throw IllegalStateException("No known logical operator")
            )

    override fun visitFunctionCallParameters(ctx: OpenSpiralParser.FunctionCallParametersContext): OSLVisitorUnion.ValueList<OSLUnion.FunctionParameterType> =
            OSLVisitorUnion.ValueList(ctx.functionParameter().map { visitFunctionParameter(it).union })

    override fun visitFunctionParameter(ctx: OpenSpiralParser.FunctionParameterContext): OSLVisitorUnion.Value<OSLUnion.FunctionParameterType> =
            OSLVisitorUnion.Value(OSLUnion.FunctionParameterType(ctx.FUNC_CALL_PARAMETER_NAME()?.text?.substringBeforeLast("=")?.trim(), visitFunctionVariableValue(ctx.functionVariableValue())?.union
                    ?: OSLUnion.NullType))

//    @ExperimentalStdlibApi
//    override fun visitBasicDrillValue(ctx: OpenSpiralParser.BasicDrillValueContext): OSLVisitorUnion {
//        ctx.wrdLabelReference()?.let(this::visitWrdLabelReference)?.let { return it }
//        ctx.wrdParameterReference()?.let(this::visitWrdParameterReference)?.let { return it }
//        ctx.variableValue()?.let(this::visitVariableValue)?.let { return it }
//
//        return OSLUnion.UndefinedType
//    }

    @ExperimentalStdlibApi
    override fun visitWrdLabelReference(ctx: OpenSpiralParser.WrdLabelReferenceContext): OSLVisitorUnion.Value<*>? {
        ctx.WRD_SHORT_LABEL_REFERENCE()?.let { node -> return OSLVisitorUnion.Value(OSLUnion.LabelType(node.text.substring(1))) }
        ctx.wrdLongLabelReference()?.let(this::visitWrdLongLabelReference)?.let { return it }

        return null
    }

    @ExperimentalStdlibApi
    override fun visitWrdParameterReference(ctx: OpenSpiralParser.WrdParameterReferenceContext): OSLVisitorUnion.Value<*>? {
        ctx.WRD_SHORT_PARAMETER_REFERENCE()?.let { node -> return OSLVisitorUnion.Value(OSLUnion.ParameterType(node.text.substring(1))) }
        ctx.wrdLongParameterReference()?.let(this::visitWrdLongParameterReference)?.let { return it }

        return null
    }

    @ExperimentalStdlibApi
    override fun visitWrdLongLabelReference(ctx: OpenSpiralParser.WrdLongLabelReferenceContext): OSLVisitorUnion.Value<OSLUnion.LongLabelType> =
            OSLVisitorUnion.Value(OSLUnion.LongLabelType(visitLongReference(ctx.longReference()).union.longReference))

    @ExperimentalStdlibApi
    override fun visitWrdLongParameterReference(ctx: OpenSpiralParser.WrdLongParameterReferenceContext): OSLVisitorUnion.Value<OSLUnion.LongParameterType> =
            OSLVisitorUnion.Value(OSLUnion.LongParameterType(visitLongReference(ctx.longReference()).union.longReference))

    @ExperimentalStdlibApi
    override fun visitLongReference(ctx: OpenSpiralParser.LongReferenceContext): OSLVisitorUnion.Value<OSLUnion.LongReferenceType> {
        val longReference = runBlocking {
            buildLongReference {
                val builder = StringBuilder()
                ctx.children.forEach { node ->
                    if (node !is TerminalNode)
                        return@forEach

                    when (node.symbol.type) {
                        OpenSpiralParser.LONG_REF_ESCAPES -> {
                            when (node.text[1]) {
                                'b' -> builder.append('\b')
                                'f' -> builder.append(0x0C.toChar())
                                'n' -> builder.append('\n')
                                'r' -> builder.append('\r')
                                't' -> builder.append('\t')
                                'u' -> builder.append(node.text.substring(2).toInt(16).toChar())
                            }
                        }
                        OpenSpiralParser.LONG_REF_CHARACTERS -> builder.append(node.text)
                        OpenSpiralParser.LONG_REF_VARIABLE_REFERENCE -> {
                            if (builder.isNotEmpty()) {
                                appendText(builder.toString())
                                builder.clear()
                            }
                            appendVariable(node.text.substring(1))
                        }
                    }
                }

                if (builder.isNotEmpty()) {
                    appendText(builder.toString())
                    builder.clear()
                }
            }
        }

        return OSLVisitorUnion.Value(OSLUnion.LongReferenceType(longReference))
    }

    @ExperimentalStdlibApi
    override fun visitQuotedStringContent(ctx: OpenSpiralParser.QuotedStringContentContext): OSLVisitorUnion.Value<OSLUnion.LongReferenceType> {
        val longReference = runBlocking {
            buildLongReference {
                val builder = StringBuilder()
                ctx.children.forEach { node ->
                    if (node !is TerminalNode)
                        return@forEach

                    when (node.symbol.type) {
                        OpenSpiralParser.ESCAPES -> {
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
                        OpenSpiralParser.STRING_CHARACTERS -> builder.append(node.text)
                        OpenSpiralParser.STRING_WHITESPACE -> builder.append(node.text)
                        OpenSpiralParser.QUOTED_STRING_VARIABLE_REFERENCE -> {
                            if (builder.isNotEmpty()) {
                                appendText(builder.toString())
                                builder.clear()
                            }
                            appendVariable(node.text.substring(1))
                        }
                        OpenSpiralParser.QUOTED_STRING_LINE_BREAK -> builder.append("\n")
                        OpenSpiralParser.QUOTED_STRING_LINE_BREAK_NO_SPACE -> builder.append("\n")
                        OpenSpiralParser.QUOTED_COLOUR_CODE -> {
                            if (builder.isNotEmpty()) {
                                appendText(builder.toString())
                                builder.clear()
                            }
                            appendColourCode(node.text.substring(1).trim())
                        }
                    }
                }

                if (builder.isNotEmpty()) {
                    appendText(builder.toString())
                    builder.clear()
                }
            }
        }

        return OSLVisitorUnion.Value(OSLUnion.LongReferenceType(longReference))
    }

    @ExperimentalStdlibApi
    override fun visitMetaVariableAssignment(ctx: OpenSpiralParser.MetaVariableAssignmentContext): OSLVisitorUnion.SetVariable? {
        val name = ctx.ASSIGN_VARIABLE_NAME().text.substringAfter(' ').trim()
        val value = visitVariableValue(ctx.variableValue())?.union ?: return null
        return OSLVisitorUnion.SetVariable(name, value)
    }

    @ExperimentalStdlibApi
    override fun visitVariableValue(ctx: OpenSpiralParser.VariableValueContext): OSLVisitorUnion.Value<*>? =
            wrapUnionValue {
                ctx.DECIMAL_NUMBER()?.let { double -> return@wrapUnionValue OSLUnion.DecimalNumberType(double.text.toDouble()) }
                ctx.INTEGER()?.let { integer -> return@wrapUnionValue OSLUnion.IntegerNumberType(integer.text.toLongVariable()) }
                ctx.VARIABLE_REFERENCE()?.let { varRef -> return@wrapUnionValue OSLUnion.VariableReferenceType(varRef.text.substring(1)) }
                ctx.booleanRule()?.let(this::visitBooleanRule)?.let { return it }
                ctx.NULL()?.let { return@wrapUnionValue OSLUnion.NullType }

                ctx.quotedString()?.let(this::visitQuotedString)?.let { return it as? OSLVisitorUnion.Value<*> }
                ctx.functionCall()?.let(this::visitFunctionCall)?.let { return it }

                OSLUnion.UndefinedType
            }

    @ExperimentalStdlibApi
    override fun visitFunctionVariableValue(ctx: OpenSpiralParser.FunctionVariableValueContext): OSLVisitorUnion.Value<*>? =
            wrapUnionValue {
                ctx.FUNC_CALL_DECIMAL_NUMBER()?.let { double -> return@wrapUnionValue OSLUnion.DecimalNumberType(double.text.toDouble()) }
                ctx.FUNC_CALL_INTEGER()?.let { integer -> return@wrapUnionValue OSLUnion.IntegerNumberType(integer.text.toLongVariable()) }
                ctx.FUNC_CALL_VARIABLE_REFERENCE()?.let { varRef -> return@wrapUnionValue OSLUnion.VariableReferenceType(varRef.text.substring(1)) }
                ctx.funcBooleanRule()?.let(this::visitFuncBooleanRule)?.let { return it }
                ctx.FUNC_CALL_NULL()?.let { return@wrapUnionValue OSLUnion.NullType }

                ctx.quotedStringContent()?.let(this::visitQuotedStringContent)?.let { return it }
                ctx.recursiveFuncCall()?.let(this::visitRecursiveFuncCall)?.let { return it }

                OSLUnion.UndefinedType
            }

    override fun visitIfCheckValue(ctx: OpenSpiralParser.IfCheckValueContext): OSLVisitorUnion.Value<*>? =
            wrapUnionValue {
                ctx.IF_CHECK_DECIMAL_NUMBER()?.let { double -> return@wrapUnionValue OSLUnion.DecimalNumberType(double.text.toDouble()) }
                ctx.IF_CHECK_INTEGER()?.let { integer -> return@wrapUnionValue OSLUnion.IntegerNumberType(integer.text.toLongVariable()) }
                ctx.IF_CHECK_VARIABLE_REFERENCE()?.let { varRef -> return@wrapUnionValue OSLUnion.VariableReferenceType(varRef.text.substring(1)) }
                ctx.ifCheckBooleanRule()?.let(this::visitIfCheckBooleanRule)?.let { return it }
                ctx.IF_CHECK_NULL()?.let { return@wrapUnionValue OSLUnion.NullType }

                ctx.quotedStringContent()?.let(this::visitQuotedStringContent)?.let { return it }
                ctx.ifCheckFuncCall()?.let(this::visitIfCheckFuncCall)?.let { return it }

                OSLUnion.UndefinedType
            }

    override fun visitIfFlagID(ctx: OpenSpiralParser.IfFlagIDContext): OSLVisitorUnion.Value<*>? =
            wrapUnionValue {
                ctx.IF_CHECK_INTEGER()?.let { integer -> return@wrapUnionValue OSLUnion.IntegerNumberType(integer.text.toLongVariable()) }
                ctx.IF_CHECK_VARIABLE_REFERENCE()?.let { varRef -> return@wrapUnionValue OSLUnion.VariableReferenceType(varRef.text.substring(1)) }
                ctx.ifCheckFuncCall()?.let(this::visitIfCheckFuncCall)?.let { return it }

                OSLUnion.UndefinedType
            }

    override fun visitBooleanRule(ctx: OpenSpiralParser.BooleanRuleContext): OSLVisitorUnion.Value<OSLUnion.BooleanType>? {
        if (ctx.TRUE() != null) return OSLVisitorUnion.Value(OSLUnion.BooleanType(true))
        if (ctx.FALSE() != null) return OSLVisitorUnion.Value(OSLUnion.BooleanType(false))
        return null
    }

    override fun visitFuncBooleanRule(ctx: OpenSpiralParser.FuncBooleanRuleContext): OSLVisitorUnion.Value<OSLUnion.BooleanType>? {
        if (ctx.FUNC_TRUE() != null) return OSLVisitorUnion.Value(OSLUnion.BooleanType(true))
        if (ctx.FUNC_FALSE() != null) return OSLVisitorUnion.Value(OSLUnion.BooleanType(false))
        return null
    }

    override fun visitIfCheckBooleanRule(ctx: OpenSpiralParser.IfCheckBooleanRuleContext): OSLVisitorUnion.Value<OSLUnion.BooleanType>? {
        if (ctx.IF_CHECK_TRUE() != null) return OSLVisitorUnion.Value(OSLUnion.BooleanType(true))
        if (ctx.IF_CHECK_FALSE() != null) return OSLVisitorUnion.Value(OSLUnion.BooleanType(false))
        return null
    }

    override fun visitDialogueDrill(ctx: OpenSpiralParser.DialogueDrillContext): OSLVisitorUnion? {
        if (ctx.NAME_IDENTIFIER() != null) {
            val speakerName = ctx.NAME_IDENTIFIER().text
            return OSLVisitorUnion.AddDialogue(speakerName, visitVariableValue(ctx.variableValue())?.union
                    ?: OSLUnion.NullType)
        } else if (ctx.VARIABLE_REFERENCE() != null) {
            val speakerVariable = ctx.VARIABLE_REFERENCE().text.substring(1)
            return OSLVisitorUnion.AddDialogueVariable(speakerVariable, visitVariableValue(ctx.variableValue())?.union
                    ?: OSLUnion.NullType)
        } else {
            return null
        }
    }

    override fun visitIfCheck(ctx: OpenSpiralParser.IfCheckContext): OSLVisitorUnion.IfCheck {
        return OSLVisitorUnion.IfCheck(visitIfCheckPure(ctx.ifCheckPure(0)), ctx.ifCheckPure().drop(1).map(this::visitIfCheckPure), ctx.scope()?.let(this::visitScope))
    }

    override fun visitIfCheckPure(ctx: OpenSpiralParser.IfCheckPureContext): OSLVisitorUnion.CheckBranch =
            OSLVisitorUnion.CheckBranch(
                    OSLVisitorUnion.Condition(
                            visitIfCheckValue(ctx.ifCheckValue(0)),
                            visitIfCheckEquality(ctx.ifCheckEquality()),
                            visitIfCheckValue(ctx.ifCheckValue(1))
                    ),
                    emptyArray(),
                    visitScope(ctx.scope())
            )

    override fun visitCheckFlag(ctx: OpenSpiralParser.CheckFlagContext): OSLVisitorUnion.CheckFlag {
        return OSLVisitorUnion.CheckFlag(visitCheckFlagPure(ctx.checkFlagPure(0)), ctx.checkFlagPure().drop(1).map(this::visitCheckFlagPure), ctx.scope()?.let(this::visitScope))
    }

    override fun visitCheckFlagPure(ctx: OpenSpiralParser.CheckFlagPureContext): OSLVisitorUnion.CheckBranch =
            OSLVisitorUnion.CheckBranch(
                    visitCheckFlagCondition(ctx.checkFlagCondition(0)),
                    freeze(ctx.checkFlagCondition().drop(1)) { flagConditionArray ->
                        Array(flagConditionArray.size) { i -> Pair(visitIfCheckLogical(ctx.ifCheckLogical(i)), visitCheckFlagCondition(flagConditionArray[i])) }
                    },
                    visitScope(ctx.scope())
            )

    override fun visitCheckFlagCondition(ctx: OpenSpiralParser.CheckFlagConditionContext): OSLVisitorUnion.Condition =
            OSLVisitorUnion.Condition(
                    visitIfFlagID(ctx.ifFlagID()),
                    visitIfCheckEquality(ctx.ifCheckEquality()),
                    visitIfCheckValue(ctx.ifCheckValue())
            )

    override fun visitCheckCharacter(ctx: OpenSpiralParser.CheckCharacterContext): OSLVisitorUnion.CheckCharacter =
            OSLVisitorUnion.CheckCharacter(visitIfCheckValue(ctx.ifCheckValue()), visitScope(ctx.scope()))

    override fun visitCheckObject(ctx: OpenSpiralParser.CheckObjectContext): OSLVisitorUnion.CheckObject =
            OSLVisitorUnion.CheckObject(visitIfCheckValue(ctx.ifCheckValue()), visitScope(ctx.scope()))


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
}