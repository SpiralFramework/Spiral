// Generated from ..\grammar\LibParser.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeVisitor } from "antlr4ts/tree/ParseTreeVisitor";

import { BooleanRuleContext } from "./LibParser";
import { LongReferenceContext } from "./LibParser";


/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by `LibParser`.
 *
 * @param <Result> The return type of the visit operation. Use `void` for
 * operations with no return type.
 */
export interface LibParserVisitor<Result> extends ParseTreeVisitor<Result> {
	/**
	 * Visit a parse tree produced by `LibParser.booleanRule`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitBooleanRule?: (ctx: BooleanRuleContext) => Result;

	/**
	 * Visit a parse tree produced by `LibParser.longReference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitLongReference?: (ctx: LongReferenceContext) => Result;
}

