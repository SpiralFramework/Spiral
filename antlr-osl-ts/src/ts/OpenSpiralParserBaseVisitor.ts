import {OpenSpiralParserVisitor} from "../antlr/OpenSpiralParserVisitor";
import {AbstractParseTreeVisitor} from "antlr4ts/tree";
import {
    ActionDeclarationContext,
    BasicDrillContext,
    BasicDrillNamedContext,
    BasicDrillValueContext,
    ComplexDrillsContext,
    DialogueDrillContext,
    HeaderDeclarationContext,
    LineSeparatorContext,
    LocalisedComponentContext,
    LocalisedStringContext,
    LongColourReferenceContext,
    MetaVariableAssignmentContext,
    QuotedStringContext,
    ScriptContext,
    ScriptLineContext,
    VariableValueContext,
    WrdLabelReferenceContext,
    WrdLongLabelReferenceContext,
    WrdLongParameterReferenceContext,
    WrdParameterReferenceContext
} from "../antlr/OpenSpiralParser";
import {BooleanRuleContext, LongReferenceContext} from "../antlr/LibParser";

export class OpenSpiralParserBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements OpenSpiralParserVisitor<T> {
    protected defaultResult(): T {
        return null;
    }

    visitActionDeclaration(ctx: ActionDeclarationContext): T {
        return this.visitChildren(ctx);
    }

    visitBasicDrill(ctx: BasicDrillContext): T {
        return this.visitChildren(ctx);
    }

    visitBasicDrillNamed(ctx: BasicDrillNamedContext): T {
        return this.visitChildren(ctx);
    }

    visitBasicDrillValue(ctx: BasicDrillValueContext): T {
        return this.visitChildren(ctx);
    }

    visitBooleanRule(ctx: BooleanRuleContext): T {
        return this.visitChildren(ctx);
    }

    visitComplexDrills(ctx: ComplexDrillsContext): T {
        return this.visitChildren(ctx);
    }

    visitDialogueDrill(ctx: DialogueDrillContext): T {
        return this.visitChildren(ctx);
    }

    visitHeaderDeclaration(ctx: HeaderDeclarationContext): T {
        return this.visitChildren(ctx);
    }

    visitLineSeparator(ctx: LineSeparatorContext): T {
        return this.visitChildren(ctx);
    }

    visitLocalisedComponent(ctx: LocalisedComponentContext): T {
        return this.visitChildren(ctx);
    }

    visitLocalisedString(ctx: LocalisedStringContext): T {
        return this.visitChildren(ctx);
    }

    visitLongColourReference(ctx: LongColourReferenceContext): T {
        return this.visitChildren(ctx);
    }

    visitLongReference(ctx: LongReferenceContext): T {
        return this.visitChildren(ctx);
    }

    visitMetaVariableAssignment(ctx: MetaVariableAssignmentContext): T {
        return this.visitChildren(ctx);
    }

    visitQuotedString(ctx: QuotedStringContext): T {
        return this.visitChildren(ctx);
    }

    visitScript(ctx: ScriptContext): T {
        return this.visitChildren(ctx);
    }

    visitScriptLine(ctx: ScriptLineContext): T {
        return this.visitChildren(ctx);
    }

    visitVariableValue(ctx: VariableValueContext): T {
        return this.visitChildren(ctx);
    }

    visitWrdLabelReference(ctx: WrdLabelReferenceContext): T {
        return this.visitChildren(ctx);
    }

    visitWrdLongLabelReference(ctx: WrdLongLabelReferenceContext): T {
        return this.visitChildren(ctx);
    }

    visitWrdLongParameterReference(ctx: WrdLongParameterReferenceContext): T {
        return this.visitChildren(ctx);
    }

    visitWrdParameterReference(ctx: WrdParameterReferenceContext): T {
        return this.visitChildren(ctx);
    }
}