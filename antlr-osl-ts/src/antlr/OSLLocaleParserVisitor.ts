// Generated from ..\grammar\OSLLocaleParser.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeVisitor } from "antlr4ts/tree/ParseTreeVisitor";

import { LocaleContext } from "./OSLLocaleParser";
import { LocaleLineContext } from "./OSLLocaleParser";


/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by `OSLLocaleParser`.
 *
 * @param <Result> The return type of the visit operation. Use `void` for
 * operations with no return type.
 */
export interface OSLLocaleParserVisitor<Result> extends ParseTreeVisitor<Result> {
	/**
	 * Visit a parse tree produced by `OSLLocaleParser.locale`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitLocale?: (ctx: LocaleContext) => Result;

	/**
	 * Visit a parse tree produced by `OSLLocaleParser.localeLine`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitLocaleLine?: (ctx: LocaleLineContext) => Result;
}

