// Generated from ..\grammar\OSLLocaleParser.g4 by ANTLR 4.7.3-SNAPSHOT


import { ATN } from "antlr4ts/atn/ATN";
import { ATNDeserializer } from "antlr4ts/atn/ATNDeserializer";
import { FailedPredicateException } from "antlr4ts/FailedPredicateException";
import { NotNull } from "antlr4ts/Decorators";
import { NoViableAltException } from "antlr4ts/NoViableAltException";
import { Override } from "antlr4ts/Decorators";
import { Parser } from "antlr4ts/Parser";
import { ParserRuleContext } from "antlr4ts/ParserRuleContext";
import { ParserATNSimulator } from "antlr4ts/atn/ParserATNSimulator";
import { ParseTreeListener } from "antlr4ts/tree/ParseTreeListener";
import { ParseTreeVisitor } from "antlr4ts/tree/ParseTreeVisitor";
import { RecognitionException } from "antlr4ts/RecognitionException";
import { RuleContext } from "antlr4ts/RuleContext";
//import { RuleVersion } from "antlr4ts/RuleVersion";
import { TerminalNode } from "antlr4ts/tree/TerminalNode";
import { Token } from "antlr4ts/Token";
import { TokenStream } from "antlr4ts/TokenStream";
import { Vocabulary } from "antlr4ts/Vocabulary";
import { VocabularyImpl } from "antlr4ts/VocabularyImpl";

import * as Utils from "antlr4ts/misc/Utils";

import { OSLLocaleParserListener } from "./OSLLocaleParserListener";
import { OSLLocaleParserVisitor } from "./OSLLocaleParserVisitor";


export class OSLLocaleParser extends Parser {
	public static readonly LOCALE_DECLARATION = 1;
	public static readonly LOCALE_PROPERTY_NAME = 2;
	public static readonly LOCALE_ASSIGNMENT = 3;
	public static readonly LOCALE_COMMENT = 4;
	public static readonly HEADER_DECLARATION = 5;
	public static readonly SEMICOLON_SEPARATOR = 6;
	public static readonly NL_SEPARATOR = 7;
	public static readonly ASSIGN_VARIABLE_NAME = 8;
	public static readonly VARIABLE_ASSIGNMENT = 9;
	public static readonly VARIABLE_REFERENCE = 10;
	public static readonly SEMANTIC_VERSION = 11;
	public static readonly VALUE_SEPARATOR = 12;
	public static readonly NAME_IDENTIFIER = 13;
	public static readonly DIALOG_DRILL_NAME = 14;
	public static readonly BASIC_DRILL_SEPARATOR = 15;
	public static readonly DIALOGUE_SEPARATOR = 16;
	public static readonly INTEGER = 17;
	public static readonly DECIMAL_NUMBER = 18;
	public static readonly TRUE = 19;
	public static readonly FALSE = 20;
	public static readonly NULL = 21;
	public static readonly BEGIN_LOCALE_STRING = 22;
	public static readonly BEGIN_QUOTED_STRING = 23;
	public static readonly BEGIN_ACTION = 24;
	public static readonly BACKTICK = 25;
	public static readonly BACKTICK_START = 26;
	public static readonly WRD_SHORT_LABEL_REFERENCE = 27;
	public static readonly WRD_SHORT_PARAMETER_REFERENCE = 28;
	public static readonly WRD_START_LONG_LABEL_REFERENCE = 29;
	public static readonly WRD_START_LONG_PARAMETER_REFERENCE = 30;
	public static readonly LOCALE_LINE_BREAK = 31;
	public static readonly LOCALE_ESCAPES = 32;
	public static readonly LOCALE_STRING_CHARACTERS = 33;
	public static readonly END_LOCALE_EOF = 34;
	public static readonly END_LOCALE_VALUE = 35;
	public static readonly ESCAPES = 36;
	public static readonly STRING_CHARACTERS = 37;
	public static readonly QUOTED_STRING_VARIABLE_REFERENCE = 38;
	public static readonly BEGIN_LONG_QUOTED_COLOUR_CODE = 39;
	public static readonly QUOTED_COLOUR_CODE = 40;
	public static readonly END_QUOTED_STRING = 41;
	public static readonly LOCALE_NAME_SEPARATOR = 42;
	public static readonly LOCALE_NAME_IDENTIFIER = 43;
	public static readonly LOCALE_VARIABLE_REFERENCE = 44;
	public static readonly END_LOCALE_STRING = 45;
	public static readonly LONG_REF_ESCAPES = 46;
	public static readonly LONG_REF_CHARACTERS = 47;
	public static readonly LONG_REF_VARIABLE_REFERENCE = 48;
	public static readonly END_LONG_REFERENCE = 49;
	public static readonly ACTION_ESCAPES = 50;
	public static readonly ACTION_CHARACTERS = 51;
	public static readonly ACTION_VARIABLE_REFERENCE = 52;
	public static readonly END_ACTION = 53;
	public static readonly BACKTICKED_ESCAPES = 54;
	public static readonly BACKTICKED_STRING_CHARACTERS = 55;
	public static readonly BACKTICK_END = 56;
	public static readonly RULE_locale = 0;
	public static readonly RULE_localeLine = 1;
	// tslint:disable:no-trailing-whitespace
	public static readonly ruleNames: string[] = [
		"locale", "localeLine",
	];

	private static readonly _LITERAL_NAMES: Array<string | undefined> = [
		undefined, undefined, undefined, undefined, undefined, undefined, undefined, 
		undefined, undefined, undefined, undefined, undefined, undefined, undefined, 
		undefined, undefined, undefined, undefined, undefined, undefined, undefined, 
		undefined, "'\"locale.'", undefined, "'['", undefined, undefined, undefined, 
		undefined, "'@{'", "'%{'", undefined, undefined, undefined, undefined, 
		undefined, undefined, undefined, undefined, "'&{'", undefined, undefined, 
		"'.'", undefined, undefined, undefined, undefined, undefined, undefined, 
		"'}'", undefined, undefined, undefined, "']'",
	];
	private static readonly _SYMBOLIC_NAMES: Array<string | undefined> = [
		undefined, "LOCALE_DECLARATION", "LOCALE_PROPERTY_NAME", "LOCALE_ASSIGNMENT", 
		"LOCALE_COMMENT", "HEADER_DECLARATION", "SEMICOLON_SEPARATOR", "NL_SEPARATOR", 
		"ASSIGN_VARIABLE_NAME", "VARIABLE_ASSIGNMENT", "VARIABLE_REFERENCE", "SEMANTIC_VERSION", 
		"VALUE_SEPARATOR", "NAME_IDENTIFIER", "DIALOG_DRILL_NAME", "BASIC_DRILL_SEPARATOR", 
		"DIALOGUE_SEPARATOR", "INTEGER", "DECIMAL_NUMBER", "TRUE", "FALSE", "NULL", 
		"BEGIN_LOCALE_STRING", "BEGIN_QUOTED_STRING", "BEGIN_ACTION", "BACKTICK", 
		"BACKTICK_START", "WRD_SHORT_LABEL_REFERENCE", "WRD_SHORT_PARAMETER_REFERENCE", 
		"WRD_START_LONG_LABEL_REFERENCE", "WRD_START_LONG_PARAMETER_REFERENCE", 
		"LOCALE_LINE_BREAK", "LOCALE_ESCAPES", "LOCALE_STRING_CHARACTERS", "END_LOCALE_EOF", 
		"END_LOCALE_VALUE", "ESCAPES", "STRING_CHARACTERS", "QUOTED_STRING_VARIABLE_REFERENCE", 
		"BEGIN_LONG_QUOTED_COLOUR_CODE", "QUOTED_COLOUR_CODE", "END_QUOTED_STRING", 
		"LOCALE_NAME_SEPARATOR", "LOCALE_NAME_IDENTIFIER", "LOCALE_VARIABLE_REFERENCE", 
		"END_LOCALE_STRING", "LONG_REF_ESCAPES", "LONG_REF_CHARACTERS", "LONG_REF_VARIABLE_REFERENCE", 
		"END_LONG_REFERENCE", "ACTION_ESCAPES", "ACTION_CHARACTERS", "ACTION_VARIABLE_REFERENCE", 
		"END_ACTION", "BACKTICKED_ESCAPES", "BACKTICKED_STRING_CHARACTERS", "BACKTICK_END",
	];
	public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(OSLLocaleParser._LITERAL_NAMES, OSLLocaleParser._SYMBOLIC_NAMES, []);

	// @Override
	// @NotNull
	public get vocabulary(): Vocabulary {
		return OSLLocaleParser.VOCABULARY;
	}
	// tslint:enable:no-trailing-whitespace

	// @Override
	public get grammarFileName(): string { return "OSLLocaleParser.g4"; }

	// @Override
	public get ruleNames(): string[] { return OSLLocaleParser.ruleNames; }

	// @Override
	public get serializedATN(): string { return OSLLocaleParser._serializedATN; }

	constructor(input: TokenStream) {
		super(input);
		this._interp = new ParserATNSimulator(OSLLocaleParser._ATN, this);
	}
	// @RuleVersion(0)
	public locale(): LocaleContext {
		let _localctx: LocaleContext = new LocaleContext(this._ctx, this.state);
		this.enterRule(_localctx, 0, OSLLocaleParser.RULE_locale);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 4;
			this.match(OSLLocaleParser.LOCALE_DECLARATION);
			this.state = 14;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === OSLLocaleParser.LOCALE_PROPERTY_NAME || _la === OSLLocaleParser.NL_SEPARATOR) {
				{
				{
				this.state = 8;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === OSLLocaleParser.NL_SEPARATOR) {
					{
					{
					this.state = 5;
					this.match(OSLLocaleParser.NL_SEPARATOR);
					}
					}
					this.state = 10;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 11;
				this.localeLine();
				}
				}
				this.state = 16;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public localeLine(): LocaleLineContext {
		let _localctx: LocaleLineContext = new LocaleLineContext(this._ctx, this.state);
		this.enterRule(_localctx, 2, OSLLocaleParser.RULE_localeLine);
		let _la: number;
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 17;
			this.match(OSLLocaleParser.LOCALE_PROPERTY_NAME);
			this.state = 18;
			this.match(OSLLocaleParser.LOCALE_ASSIGNMENT);
			this.state = 22;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 2, this._ctx);
			while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1 + 1) {
					{
					{
					this.state = 19;
					_la = this._input.LA(1);
					if (!(((((_la - 31)) & ~0x1F) === 0 && ((1 << (_la - 31)) & ((1 << (OSLLocaleParser.LOCALE_LINE_BREAK - 31)) | (1 << (OSLLocaleParser.LOCALE_ESCAPES - 31)) | (1 << (OSLLocaleParser.LOCALE_STRING_CHARACTERS - 31)))) !== 0))) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					}
					}
				}
				this.state = 24;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 2, this._ctx);
			}
			this.state = 25;
			this.match(OSLLocaleParser.END_LOCALE_VALUE);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}

	public static readonly _serializedATN: string =
		"\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x03:\x1E\x04\x02" +
		"\t\x02\x04\x03\t\x03\x03\x02\x03\x02\x07\x02\t\n\x02\f\x02\x0E\x02\f\v" +
		"\x02\x03\x02\x07\x02\x0F\n\x02\f\x02\x0E\x02\x12\v\x02\x03\x03\x03\x03" +
		"\x03\x03\x07\x03\x17\n\x03\f\x03\x0E\x03\x1A\v\x03\x03\x03\x03\x03\x03" +
		"\x03\x03\x18\x02\x02\x04\x02\x02\x04\x02\x02\x03\x03\x02!#\x02\x1E\x02" +
		"\x06\x03\x02\x02\x02\x04\x13\x03\x02\x02\x02\x06\x10\x07\x03\x02\x02\x07" +
		"\t\x07\t\x02\x02\b\x07\x03\x02\x02\x02\t\f\x03\x02\x02\x02\n\b\x03\x02" +
		"\x02\x02\n\v\x03\x02\x02\x02\v\r\x03\x02\x02\x02\f\n\x03\x02\x02\x02\r" +
		"\x0F\x05\x04\x03\x02\x0E\n\x03\x02\x02\x02\x0F\x12\x03\x02\x02\x02\x10" +
		"\x0E\x03\x02\x02\x02\x10\x11\x03\x02\x02\x02\x11\x03\x03\x02\x02\x02\x12" +
		"\x10\x03\x02\x02\x02\x13\x14\x07\x04\x02\x02\x14\x18\x07\x05\x02\x02\x15" +
		"\x17\t\x02\x02\x02\x16\x15\x03\x02\x02\x02\x17\x1A\x03\x02\x02\x02\x18" +
		"\x19\x03\x02\x02\x02\x18\x16\x03\x02\x02\x02\x19\x1B\x03\x02\x02\x02\x1A" +
		"\x18\x03\x02\x02\x02\x1B\x1C\x07%\x02\x02\x1C\x05\x03\x02\x02\x02\x05" +
		"\n\x10\x18";
	public static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!OSLLocaleParser.__ATN) {
			OSLLocaleParser.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(OSLLocaleParser._serializedATN));
		}

		return OSLLocaleParser.__ATN;
	}

}

export class LocaleContext extends ParserRuleContext {
	public LOCALE_DECLARATION(): TerminalNode { return this.getToken(OSLLocaleParser.LOCALE_DECLARATION, 0); }
	public localeLine(): LocaleLineContext[];
	public localeLine(i: number): LocaleLineContext;
	public localeLine(i?: number): LocaleLineContext | LocaleLineContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LocaleLineContext);
		} else {
			return this.getRuleContext(i, LocaleLineContext);
		}
	}
	public NL_SEPARATOR(): TerminalNode[];
	public NL_SEPARATOR(i: number): TerminalNode;
	public NL_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OSLLocaleParser.NL_SEPARATOR);
		} else {
			return this.getToken(OSLLocaleParser.NL_SEPARATOR, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OSLLocaleParser.RULE_locale; }
	// @Override
	public enterRule(listener: OSLLocaleParserListener): void {
		if (listener.enterLocale) {
			listener.enterLocale(this);
		}
	}
	// @Override
	public exitRule(listener: OSLLocaleParserListener): void {
		if (listener.exitLocale) {
			listener.exitLocale(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OSLLocaleParserVisitor<Result>): Result {
		if (visitor.visitLocale) {
			return visitor.visitLocale(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LocaleLineContext extends ParserRuleContext {
	public LOCALE_PROPERTY_NAME(): TerminalNode { return this.getToken(OSLLocaleParser.LOCALE_PROPERTY_NAME, 0); }
	public LOCALE_ASSIGNMENT(): TerminalNode { return this.getToken(OSLLocaleParser.LOCALE_ASSIGNMENT, 0); }
	public END_LOCALE_VALUE(): TerminalNode { return this.getToken(OSLLocaleParser.END_LOCALE_VALUE, 0); }
	public LOCALE_LINE_BREAK(): TerminalNode[];
	public LOCALE_LINE_BREAK(i: number): TerminalNode;
	public LOCALE_LINE_BREAK(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OSLLocaleParser.LOCALE_LINE_BREAK);
		} else {
			return this.getToken(OSLLocaleParser.LOCALE_LINE_BREAK, i);
		}
	}
	public LOCALE_ESCAPES(): TerminalNode[];
	public LOCALE_ESCAPES(i: number): TerminalNode;
	public LOCALE_ESCAPES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OSLLocaleParser.LOCALE_ESCAPES);
		} else {
			return this.getToken(OSLLocaleParser.LOCALE_ESCAPES, i);
		}
	}
	public LOCALE_STRING_CHARACTERS(): TerminalNode[];
	public LOCALE_STRING_CHARACTERS(i: number): TerminalNode;
	public LOCALE_STRING_CHARACTERS(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OSLLocaleParser.LOCALE_STRING_CHARACTERS);
		} else {
			return this.getToken(OSLLocaleParser.LOCALE_STRING_CHARACTERS, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OSLLocaleParser.RULE_localeLine; }
	// @Override
	public enterRule(listener: OSLLocaleParserListener): void {
		if (listener.enterLocaleLine) {
			listener.enterLocaleLine(this);
		}
	}
	// @Override
	public exitRule(listener: OSLLocaleParserListener): void {
		if (listener.exitLocaleLine) {
			listener.exitLocaleLine(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OSLLocaleParserVisitor<Result>): Result {
		if (visitor.visitLocaleLine) {
			return visitor.visitLocaleLine(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


