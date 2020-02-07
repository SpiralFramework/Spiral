// Generated from ..\grammar\OpenSpiralParser.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeVisitor } from "antlr4ts/tree/ParseTreeVisitor";

import { HeaderDeclarationContext } from "./OpenSpiralParser";
import { ScriptContext } from "./OpenSpiralParser";
import { LineSeparatorContext } from "./OpenSpiralParser";
import { ScriptLineContext } from "./OpenSpiralParser";
import { MetaVariableAssignmentContext } from "./OpenSpiralParser";
import { BasicDrillContext } from "./OpenSpiralParser";
import { BasicDrillNamedContext } from "./OpenSpiralParser";
import { QuotedStringContext } from "./OpenSpiralParser";
import { LocalisedStringContext } from "./OpenSpiralParser";
import { LocalisedComponentContext } from "./OpenSpiralParser";
import { LongColourReferenceContext } from "./OpenSpiralParser";
import { BasicDrillValueContext } from "./OpenSpiralParser";
import { VariableValueContext } from "./OpenSpiralParser";
import { ActionDeclarationContext } from "./OpenSpiralParser";
import { ComplexDrillsContext } from "./OpenSpiralParser";
import { DialogueDrillContext } from "./OpenSpiralParser";
import { WrdLabelReferenceContext } from "./OpenSpiralParser";
import { WrdParameterReferenceContext } from "./OpenSpiralParser";
import { WrdLongLabelReferenceContext } from "./OpenSpiralParser";
import { WrdLongParameterReferenceContext } from "./OpenSpiralParser";
import { BooleanRuleContext } from "./OpenSpiralParser";
import { LongReferenceContext } from "./OpenSpiralParser";


/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by `OpenSpiralParser`.
 *
 * @param <Result> The return type of the visit operation. Use `void` for
 * operations with no return type.
 */
export interface OpenSpiralParserVisitor<Result> extends ParseTreeVisitor<Result> {
	/**
	 * Visit a parse tree produced by `OpenSpiralParser.headerDeclaration`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitHeaderDeclaration?: (ctx: HeaderDeclarationContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.script`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitScript?: (ctx: ScriptContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.lineSeparator`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitLineSeparator?: (ctx: LineSeparatorContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.scriptLine`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitScriptLine?: (ctx: ScriptLineContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.metaVariableAssignment`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitMetaVariableAssignment?: (ctx: MetaVariableAssignmentContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.basicDrill`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitBasicDrill?: (ctx: BasicDrillContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.basicDrillNamed`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitBasicDrillNamed?: (ctx: BasicDrillNamedContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.quotedString`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitQuotedString?: (ctx: QuotedStringContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.localisedString`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitLocalisedString?: (ctx: LocalisedStringContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.localisedComponent`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitLocalisedComponent?: (ctx: LocalisedComponentContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.longColourReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitLongColourReference?: (ctx: LongColourReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.basicDrillValue`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitBasicDrillValue?: (ctx: BasicDrillValueContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.variableValue`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitVariableValue?: (ctx: VariableValueContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.actionDeclaration`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitActionDeclaration?: (ctx: ActionDeclarationContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.complexDrills`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitComplexDrills?: (ctx: ComplexDrillsContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.dialogueDrill`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitDialogueDrill?: (ctx: DialogueDrillContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.wrdLabelReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitWrdLabelReference?: (ctx: WrdLabelReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.wrdParameterReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitWrdParameterReference?: (ctx: WrdParameterReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.wrdLongLabelReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitWrdLongLabelReference?: (ctx: WrdLongLabelReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.wrdLongParameterReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitWrdLongParameterReference?: (ctx: WrdLongParameterReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.booleanRule`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitBooleanRule?: (ctx: BooleanRuleContext) => Result;

	/**
	 * Visit a parse tree produced by `OpenSpiralParser.longReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitLongReference?: (ctx: LongReferenceContext) => Result;
}

