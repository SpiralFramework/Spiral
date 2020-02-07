// Generated from ..\grammar\LibParser.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeListener } from "antlr4ts/tree/ParseTreeListener";

import { BooleanRuleContext } from "./LibParser";
import { LongReferenceContext } from "./LibParser";


/**
 * This interface defines a complete listener for a parse tree produced by
 * `LibParser`.
 */
export interface LibParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by `LibParser.booleanRule`.
	 * @param ctx the parse tree
	 */
	enterBooleanRule?: (ctx: BooleanRuleContext) => void;
	/**
	 * Exit a parse tree produced by `LibParser.booleanRule`.
	 * @param ctx the parse tree
	 */
	exitBooleanRule?: (ctx: BooleanRuleContext) => void;

	/**
	 * Enter a parse tree produced by `LibParser.longReference`.
	 * @param ctx the parse tree
	 */
	enterLongReference?: (ctx: LongReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `LibParser.longReference`.
	 * @param ctx the parse tree
	 */
	exitLongReference?: (ctx: LongReferenceContext) => void;
}

