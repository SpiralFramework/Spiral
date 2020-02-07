// Generated from ..\grammar\OSLLocaleParser.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeListener } from "antlr4ts/tree/ParseTreeListener";

import { LocaleContext } from "./OSLLocaleParser";
import { LocaleLineContext } from "./OSLLocaleParser";


/**
 * This interface defines a complete listener for a parse tree produced by
 * `OSLLocaleParser`.
 */
export interface OSLLocaleParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by `OSLLocaleParser.locale`.
	 * @param ctx the parse tree
	 */
	enterLocale?: (ctx: LocaleContext) => void;
	/**
	 * Exit a parse tree produced by `OSLLocaleParser.locale`.
	 * @param ctx the parse tree
	 */
	exitLocale?: (ctx: LocaleContext) => void;

	/**
	 * Enter a parse tree produced by `OSLLocaleParser.localeLine`.
	 * @param ctx the parse tree
	 */
	enterLocaleLine?: (ctx: LocaleLineContext) => void;
	/**
	 * Exit a parse tree produced by `OSLLocaleParser.localeLine`.
	 * @param ctx the parse tree
	 */
	exitLocaleLine?: (ctx: LocaleLineContext) => void;
}

