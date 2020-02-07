// Generated from ..\grammar\OSLWordScriptParser.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeVisitor } from "antlr4ts/tree/ParseTreeVisitor";

import { WrdLabelReferenceContext } from "./OSLWordScriptParser";
import { WrdParameterReferenceContext } from "./OSLWordScriptParser";
import { WrdLongLabelReferenceContext } from "./OSLWordScriptParser";
import { WrdLongParameterReferenceContext } from "./OSLWordScriptParser";
import { BooleanRuleContext } from "./OSLWordScriptParser";
import { LongReferenceContext } from "./OSLWordScriptParser";


/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by `OSLWordScriptParser`.
 *
 * @param <Result> The return type of the visit operation. Use `void` for
 * operations with no return type.
 */
export interface OSLWordScriptParserVisitor<Result> extends ParseTreeVisitor<Result> {
	/**
	 * Visit a parse tree produced by `OSLWordScriptParser.wrdLabelReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitWrdLabelReference?: (ctx: WrdLabelReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `OSLWordScriptParser.wrdParameterReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitWrdParameterReference?: (ctx: WrdParameterReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `OSLWordScriptParser.wrdLongLabelReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitWrdLongLabelReference?: (ctx: WrdLongLabelReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `OSLWordScriptParser.wrdLongParameterReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitWrdLongParameterReference?: (ctx: WrdLongParameterReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `OSLWordScriptParser.booleanRule`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitBooleanRule?: (ctx: BooleanRuleContext) => Result;

	/**
	 * Visit a parse tree produced by `OSLWordScriptParser.longReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitLongReference?: (ctx: LongReferenceContext) => Result;
}

