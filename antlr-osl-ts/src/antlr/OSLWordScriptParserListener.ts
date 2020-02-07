// Generated from ..\grammar\OSLWordScriptParser.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeListener } from "antlr4ts/tree/ParseTreeListener";

import { WrdLabelReferenceContext } from "./OSLWordScriptParser";
import { WrdParameterReferenceContext } from "./OSLWordScriptParser";
import { WrdLongLabelReferenceContext } from "./OSLWordScriptParser";
import { WrdLongParameterReferenceContext } from "./OSLWordScriptParser";
import { BooleanRuleContext } from "./OSLWordScriptParser";
import { LongReferenceContext } from "./OSLWordScriptParser";


/**
 * This interface defines a complete listener for a parse tree produced by
 * `OSLWordScriptParser`.
 */
export interface OSLWordScriptParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by `OSLWordScriptParser.wrdLabelReference`.
	 * @param ctx the parse tree
	 */
	enterWrdLabelReference?: (ctx: WrdLabelReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OSLWordScriptParser.wrdLabelReference`.
	 * @param ctx the parse tree
	 */
	exitWrdLabelReference?: (ctx: WrdLabelReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `OSLWordScriptParser.wrdParameterReference`.
	 * @param ctx the parse tree
	 */
	enterWrdParameterReference?: (ctx: WrdParameterReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OSLWordScriptParser.wrdParameterReference`.
	 * @param ctx the parse tree
	 */
	exitWrdParameterReference?: (ctx: WrdParameterReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `OSLWordScriptParser.wrdLongLabelReference`.
	 * @param ctx the parse tree
	 */
	enterWrdLongLabelReference?: (ctx: WrdLongLabelReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OSLWordScriptParser.wrdLongLabelReference`.
	 * @param ctx the parse tree
	 */
	exitWrdLongLabelReference?: (ctx: WrdLongLabelReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `OSLWordScriptParser.wrdLongParameterReference`.
	 * @param ctx the parse tree
	 */
	enterWrdLongParameterReference?: (ctx: WrdLongParameterReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OSLWordScriptParser.wrdLongParameterReference`.
	 * @param ctx the parse tree
	 */
	exitWrdLongParameterReference?: (ctx: WrdLongParameterReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `OSLWordScriptParser.booleanRule`.
	 * @param ctx the parse tree
	 */
	enterBooleanRule?: (ctx: BooleanRuleContext) => void;
	/**
	 * Exit a parse tree produced by `OSLWordScriptParser.booleanRule`.
	 * @param ctx the parse tree
	 */
	exitBooleanRule?: (ctx: BooleanRuleContext) => void;

	/**
	 * Enter a parse tree produced by `OSLWordScriptParser.longReference`.
	 * @param ctx the parse tree
	 */
	enterLongReference?: (ctx: LongReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `OSLWordScriptParser.longReference`.
	 * @param ctx the parse tree
	 */
	exitLongReference?: (ctx: LongReferenceContext) => void;
}

