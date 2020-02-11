package info.spiralframework.osl

import info.spiralframework.antlr.osl.OpenSpiralParser
import info.spiralframework.antlr.osl.OpenSpiralParserBaseVisitor
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.osb.common.OSLUnion
import info.spiralframework.osb.common.OpenSpiralBitcodeBuilder
import info.spiralframework.osb.common.buildAction
import info.spiralframework.osb.common.buildLongReference
import kotlinx.coroutines.runBlocking
import org.abimon.kornea.io.common.flow.OutputFlow
import org.antlr.v4.runtime.tree.TerminalNode

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class OSLVisitor(val builder: OpenSpiralBitcodeBuilder) : OpenSpiralParserBaseVisitor<OSLUnion>() {
    companion object {
        const val OSL_HEADER_LENGTH = "OSL Script".length
        suspend operator fun invoke(output: OutputFlow) = OSLVisitor(OpenSpiralBitcodeBuilder(output))

        operator fun <T> OpenSpiralBitcodeBuilder.invoke(block: suspend OpenSpiralBitcodeBuilder.() -> T) =
                runBlocking { this@invoke.block() }
    }

    override fun visitHeaderDeclaration(ctx: OpenSpiralParser.HeaderDeclarationContext): OSLUnion {
        val rawSemanticVersion = ctx.HEADER_DECLARATION()
                .text
                .substring(OSL_HEADER_LENGTH)
                .takeIf(String::isNotBlank)
                ?.substring(1)
                ?.split('.')
                ?.map(String::trim)
                ?.mapNotNull(String::toIntOrNull)
                ?: return OSLUnion.NoOpType

        builder {
            setVersion(
                    SemanticVersion(
                            rawSemanticVersion.getOrNull(0) ?: 0,
                            rawSemanticVersion.getOrNull(1) ?: 0,
                            rawSemanticVersion.getOrNull(2) ?: 0
                    )
            )
        }

        return OSLUnion.NoOpType
    }
    
    @ExperimentalStdlibApi
    override fun visitBasicDrill(ctx: OpenSpiralParser.BasicDrillContext): OSLUnion {
        val opcode = ctx.INTEGER().text.toIntVariable()
        builder {
            val values = ctx.basicDrillValue()
            addOpcode(opcode, Array(values.size) { i -> visitBasicDrillValue(values[i]) })
        }
        return OSLUnion.NoOpType
    }

    @ExperimentalStdlibApi
    override fun visitBasicDrillNamed(ctx: OpenSpiralParser.BasicDrillNamedContext): OSLUnion {
        val opcodeName = ctx.NAME_IDENTIFIER().text.trimEnd('|')
        builder {
            val values = ctx.basicDrillValue()
            addOpcode(opcodeName, Array(values.size) { i -> visitBasicDrillValue(values[i]) })
        }
        return OSLUnion.NoOpType
    }

    @ExperimentalStdlibApi
    override fun visitBasicDrillValue(ctx: OpenSpiralParser.BasicDrillValueContext): OSLUnion {
        ctx.wrdLabelReference()?.let(this::visitWrdLabelReference)?.let { return it }
        ctx.wrdParameterReference()?.let(this::visitWrdParameterReference)?.let { return it }
        ctx.variableValue()?.let(this::visitVariableValue)?.let { return it }

        return OSLUnion.UndefinedType
    }

    @ExperimentalStdlibApi
    override fun visitWrdLabelReference(ctx: OpenSpiralParser.WrdLabelReferenceContext): OSLUnion {
        ctx.WRD_SHORT_LABEL_REFERENCE()?.let { node -> return OSLUnion.LabelType(node.text.substring(1)) }
        ctx.wrdLongLabelReference()?.let(this::visitWrdLongLabelReference)?.let { return it }

        return OSLUnion.UndefinedType
    }

    @ExperimentalStdlibApi
    override fun visitWrdParameterReference(ctx: OpenSpiralParser.WrdParameterReferenceContext): OSLUnion {
        ctx.WRD_SHORT_PARAMETER_REFERENCE()?.let { node -> return OSLUnion.ParameterType(node.text.substring(1)) }
        ctx.wrdLongParameterReference()?.let(this::visitWrdLongParameterReference)?.let { return it }

        return OSLUnion.UndefinedType
    }

    @ExperimentalStdlibApi
    override fun visitWrdLongLabelReference(ctx: OpenSpiralParser.WrdLongLabelReferenceContext): OSLUnion.LongLabelType =
            OSLUnion.LongLabelType(visitLongReference(ctx.longReference()).longReference)

    @ExperimentalStdlibApi
    override fun visitWrdLongParameterReference(ctx: OpenSpiralParser.WrdLongParameterReferenceContext): OSLUnion.LongParameterType =
            OSLUnion.LongParameterType(visitLongReference(ctx.longReference()).longReference)

    @ExperimentalStdlibApi
    override fun visitLongReference(ctx: OpenSpiralParser.LongReferenceContext): OSLUnion.LongReferenceType {
        val longReference = builder {
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

        return OSLUnion.LongReferenceType(longReference)
    }

    @ExperimentalStdlibApi
    override fun visitQuotedString(ctx: OpenSpiralParser.QuotedStringContext): OSLUnion.LongReferenceType {
        val longReference = builder {
            buildLongReference {
                val builder = StringBuilder()
                ctx.children.forEach { node ->
                    if (node !is TerminalNode)
                        return@forEach

                    when (node.symbol.type) {
                        OpenSpiralParser.ESCAPES -> {
                            when (node.text[1]) {
                                'b' -> builder.append('\b')
                                'f' -> builder.append(0x0C.toChar())
                                'n' -> builder.append('\n')
                                'r' -> builder.append('\r')
                                't' -> builder.append('\t')
                                'u' -> builder.append(node.text.substring(2).toInt(16).toChar())
                            }
                        }
                        OpenSpiralParser.STRING_CHARACTERS -> builder.append(node.text)
                        OpenSpiralParser.QUOTED_STRING_VARIABLE_REFERENCE -> {
                            if (builder.isNotEmpty()) {
                                appendText(builder.toString())
                                builder.clear()
                            }
                            appendVariable(node.text.substring(1))
                        }
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

        return OSLUnion.LongReferenceType(longReference)
    }

    @ExperimentalStdlibApi
    override fun visitMetaVariableAssignment(ctx: OpenSpiralParser.MetaVariableAssignmentContext): OSLUnion {
        val name = ctx.ASSIGN_VARIABLE_NAME().text.substringAfter(' ').trim()
        val value = visitVariableValue(ctx.variableValue())
        builder { setVariable(name, value) }
        return OSLUnion.NoOpType
    }

    @ExperimentalStdlibApi
    override fun visitVariableValue(ctx: OpenSpiralParser.VariableValueContext): OSLUnion {
        ctx.DECIMAL_NUMBER()?.let { double -> return OSLUnion.NumberType(double.text.toDouble()) }
        ctx.INTEGER()?.let { integer -> return OSLUnion.NumberType(integer.text.toLongVariable()) }
        ctx.VARIABLE_REFERENCE()?.let { varRef -> return OSLUnion.VariableReferenceType(varRef.text.substring(1)) }
//        ctx.LOCALISED_STRING()
        ctx.booleanRule()?.let(this::visitBooleanRule)?.let { return it }
        ctx.NULL()?.let { return OSLUnion.NullType }

        ctx.quotedString()?.let(this::visitQuotedString)?.let { return it }

        return OSLUnion.UndefinedType
    }

    override fun visitBooleanRule(ctx: OpenSpiralParser.BooleanRuleContext): OSLUnion {
        if (ctx.TRUE() != null) return OSLUnion.BooleanType(true)
        if (ctx.FALSE() != null) return OSLUnion.BooleanType(false)
        return OSLUnion.UndefinedType
    }

    override fun visitActionDeclaration(ctx: OpenSpiralParser.ActionDeclarationContext): OSLUnion {
        val actionName = builder {
            buildAction {
                val builder = StringBuilder()
                ctx.children.forEach { node ->
                    if (node !is TerminalNode)
                        return@forEach

                    when (node.symbol.type) {
                        OpenSpiralParser.ACTION_ESCAPES -> {
                            when (node.text[1]) {
                                'b' -> builder.append('\b')
                                'f' -> builder.append(0x0C.toChar())
                                'n' -> builder.append('\n')
                                'r' -> builder.append('\r')
                                't' -> builder.append('\t')
                                'u' -> builder.append(node.text.substring(2).toInt(16).toChar())
                            }
                        }
                        OpenSpiralParser.ACTION_CHARACTERS -> builder.append(node.text)
                        OpenSpiralParser.ACTION_VARIABLE_REFERENCE -> {
                            if (builder.isNotEmpty()) {
                                appendText(builder.toString())
                                builder.clear()
                            }
                            appendVariable(node.text.substring(1))
                        }
                    }
                }

                if (builder.isNotEmpty()) appendText(builder.toString())
            }
        }

        return OSLUnion.ActionType(actionName)
    }

    override fun visitDialogueDrill(ctx: OpenSpiralParser.DialogueDrillContext): OSLUnion {
        if (ctx.NAME_IDENTIFIER() != null) {
            val speakerName = ctx.NAME_IDENTIFIER().text
            builder { addDialogue(speakerName, visitVariableValue(ctx.variableValue())) }
            return OSLUnion.NoOpType
        } else if (ctx.VARIABLE_REFERENCE() != null) {
            val speakerVariable = ctx.VARIABLE_REFERENCE().text.substring(1)
            builder { addDialogueVariable(speakerVariable, visitVariableValue(ctx.variableValue())) }
            return OSLUnion.NoOpType
        } else {
            return OSLUnion.NoOpType
        }
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
}