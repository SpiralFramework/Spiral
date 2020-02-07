// Generated from ..\grammar\OpenSpiralParser.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeListener } from "antlr4ts/tree/ParseTreeListener";

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
 * This interface defines a complete listener for a parse tree produced by
 * `OpenSpiralParser`.
 */
export interface OpenSpiralParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by `OpenSpiralParser.headerDeclaration`.
	 * @param ctx the parse tree
	 */
	enterHeaderDeclaration?: (ctx: HeaderDeclarationContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.headerDeclaration`.
	 * @param ctx the parse tree
	 */
	exitHeaderDeclaration?: (ctx: HeaderDeclarationContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.script`.
	 * @param ctx the parse tree
	 */
	enterScript?: (ctx: ScriptContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.script`.
	 * @param ctx the parse tree
	 */
	exitScript?: (ctx: ScriptContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.lineSeparator`.
	 * @param ctx the parse tree
	 */
	enterLineSeparator?: (ctx: LineSeparatorContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.lineSeparator`.
	 * @param ctx the parse tree
	 */
	exitLineSeparator?: (ctx: LineSeparatorContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.scriptLine`.
	 * @param ctx the parse tree
	 */
	enterScriptLine?: (ctx: ScriptLineContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.scriptLine`.
	 * @param ctx the parse tree
	 */
	exitScriptLine?: (ctx: ScriptLineContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.metaVariableAssignment`.
	 * @param ctx the parse tree
	 */
	enterMetaVariableAssignment?: (ctx: MetaVariableAssignmentContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.metaVariableAssignment`.
	 * @param ctx the parse tree
	 */
	exitMetaVariableAssignment?: (ctx: MetaVariableAssignmentContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.basicDrill`.
	 * @param ctx the parse tree
	 */
	enterBasicDrill?: (ctx: BasicDrillContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.basicDrill`.
	 * @param ctx the parse tree
	 */
	exitBasicDrill?: (ctx: BasicDrillContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.basicDrillNamed`.
	 * @param ctx the parse tree
	 */
	enterBasicDrillNamed?: (ctx: BasicDrillNamedContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.basicDrillNamed`.
	 * @param ctx the parse tree
	 */
	exitBasicDrillNamed?: (ctx: BasicDrillNamedContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.quotedString`.
	 * @param ctx the parse tree
	 */
	enterQuotedString?: (ctx: QuotedStringContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.quotedString`.
	 * @param ctx the parse tree
	 */
	exitQuotedString?: (ctx: QuotedStringContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.localisedString`.
	 * @param ctx the parse tree
	 */
	enterLocalisedString?: (ctx: LocalisedStringContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.localisedString`.
	 * @param ctx the parse tree
	 */
	exitLocalisedString?: (ctx: LocalisedStringContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.localisedComponent`.
	 * @param ctx the parse tree
	 */
	enterLocalisedComponent?: (ctx: LocalisedComponentContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.localisedComponent`.
	 * @param ctx the parse tree
	 */
	exitLocalisedComponent?: (ctx: LocalisedComponentContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.longColourReference`.
	 * @param ctx the parse tree
	 */
	enterLongColourReference?: (ctx: LongColourReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.longColourReference`.
	 * @param ctx the parse tree
	 */
	exitLongColourReference?: (ctx: LongColourReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.basicDrillValue`.
	 * @param ctx the parse tree
	 */
	enterBasicDrillValue?: (ctx: BasicDrillValueContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.basicDrillValue`.
	 * @param ctx the parse tree
	 */
	exitBasicDrillValue?: (ctx: BasicDrillValueContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.variableValue`.
	 * @param ctx the parse tree
	 */
	enterVariableValue?: (ctx: VariableValueContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.variableValue`.
	 * @param ctx the parse tree
	 */
	exitVariableValue?: (ctx: VariableValueContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.actionDeclaration`.
	 * @param ctx the parse tree
	 */
	enterActionDeclaration?: (ctx: ActionDeclarationContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.actionDeclaration`.
	 * @param ctx the parse tree
	 */
	exitActionDeclaration?: (ctx: ActionDeclarationContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.complexDrills`.
	 * @param ctx the parse tree
	 */
	enterComplexDrills?: (ctx: ComplexDrillsContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.complexDrills`.
	 * @param ctx the parse tree
	 */
	exitComplexDrills?: (ctx: ComplexDrillsContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.dialogueDrill`.
	 * @param ctx the parse tree
	 */
	enterDialogueDrill?: (ctx: DialogueDrillContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.dialogueDrill`.
	 * @param ctx the parse tree
	 */
	exitDialogueDrill?: (ctx: DialogueDrillContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.wrdLabelReference`.
	 * @param ctx the parse tree
	 */
	enterWrdLabelReference?: (ctx: WrdLabelReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.wrdLabelReference`.
	 * @param ctx the parse tree
	 */
	exitWrdLabelReference?: (ctx: WrdLabelReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.wrdParameterReference`.
	 * @param ctx the parse tree
	 */
	enterWrdParameterReference?: (ctx: WrdParameterReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.wrdParameterReference`.
	 * @param ctx the parse tree
	 */
	exitWrdParameterReference?: (ctx: WrdParameterReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.wrdLongLabelReference`.
	 * @param ctx the parse tree
	 */
	enterWrdLongLabelReference?: (ctx: WrdLongLabelReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.wrdLongLabelReference`.
	 * @param ctx the parse tree
	 */
	exitWrdLongLabelReference?: (ctx: WrdLongLabelReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.wrdLongParameterReference`.
	 * @param ctx the parse tree
	 */
	enterWrdLongParameterReference?: (ctx: WrdLongParameterReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.wrdLongParameterReference`.
	 * @param ctx the parse tree
	 */
	exitWrdLongParameterReference?: (ctx: WrdLongParameterReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.booleanRule`.
	 * @param ctx the parse tree
	 */
	enterBooleanRule?: (ctx: BooleanRuleContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.booleanRule`.
	 * @param ctx the parse tree
	 */
	exitBooleanRule?: (ctx: BooleanRuleContext) => void;

	/**
	 * Enter a parse tree produced by `OpenSpiralParser.longReference`.
	 * @param ctx the parse tree
	 */
	enterLongReference?: (ctx: LongReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OpenSpiralParser.longReference`.
	 * @param ctx the parse tree
	 */
	exitLongReference?: (ctx: LongReferenceContext) => void;
}

