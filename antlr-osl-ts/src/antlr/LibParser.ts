// Generated from ..\grammar\LibParser.g4 by ANTLR 4.7.3-SNAPSHOT


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

import { LibParserListener } from "./LibParserListener";
import { LibParserVisitor } from "./LibParserVisitor";


export class LibParser extends Parser {
	public static readonly INTEGER = 1;
	public static readonly DECIMAL_NUMBER = 2;
	public static readonly TRUE = 3;
	public static readonly FALSE = 4;
	public static readonly NULL = 5;
	public static readonly BEGIN_LOCALE_STRING = 6;
	public static readonly BEGIN_QUOTED_STRING = 7;
	public static readonly BEGIN_ACTION = 8;
	public static readonly ESCAPES = 9;
	public static readonly STRING_CHARACTERS = 10;
	public static readonly QUOTED_STRING_VARIABLE_REFERENCE = 11;
	public static readonly BEGIN_LONG_QUOTED_COLOUR_CODE = 12;
	public static readonly QUOTED_COLOUR_CODE = 13;
	public static readonly END_QUOTED_STRING = 14;
	public static readonly LOCALE_NAME_SEPARATOR = 15;
	public static readonly LOCALE_NAME_IDENTIFIER = 16;
	public static readonly LOCALE_VARIABLE_REFERENCE = 17;
	public static readonly END_LOCALE_STRING = 18;
	public static readonly LONG_REF_ESCAPES = 19;
	public static readonly LONG_REF_CHARACTERS = 20;
	public static readonly LONG_REF_VARIABLE_REFERENCE = 21;
	public static readonly END_LONG_REFERENCE = 22;
	public static readonly ACTION_ESCAPES = 23;
	public static readonly ACTION_CHARACTERS = 24;
	public static readonly ACTION_VARIABLE_REFERENCE = 25;
	public static readonly END_ACTION = 26;
	public static readonly RULE_booleanRule = 0;
	public static readonly RULE_longReference = 1;
	// tslint:disable:no-trailing-whitespace
	public static readonly ruleNames: string[] = [
		"booleanRule", "longReference",
	];

	private static readonly _LITERAL_NAMES: Array<string | undefined> = [
		undefined, undefined, undefined, undefined, undefined, undefined, "'\"locale.'", 
		undefined, "'['", undefined, undefined, undefined, "'&{'", undefined, 
		undefined, "'.'", undefined, undefined, undefined, undefined, undefined, 
		undefined, "'}'", undefined, undefined, undefined, "']'",
	];
	private static readonly _SYMBOLIC_NAMES: Array<string | undefined> = [
		undefined, "INTEGER", "DECIMAL_NUMBER", "TRUE", "FALSE", "NULL", "BEGIN_LOCALE_STRING", 
		"BEGIN_QUOTED_STRING", "BEGIN_ACTION", "ESCAPES", "STRING_CHARACTERS", 
		"QUOTED_STRING_VARIABLE_REFERENCE", "BEGIN_LONG_QUOTED_COLOUR_CODE", "QUOTED_COLOUR_CODE", 
		"END_QUOTED_STRING", "LOCALE_NAME_SEPARATOR", "LOCALE_NAME_IDENTIFIER", 
		"LOCALE_VARIABLE_REFERENCE", "END_LOCALE_STRING", "LONG_REF_ESCAPES", 
		"LONG_REF_CHARACTERS", "LONG_REF_VARIABLE_REFERENCE", "END_LONG_REFERENCE", 
		"ACTION_ESCAPES", "ACTION_CHARACTERS", "ACTION_VARIABLE_REFERENCE", "END_ACTION",
	];
	public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(LibParser._LITERAL_NAMES, LibParser._SYMBOLIC_NAMES, []);

	// @Override
	// @NotNull
	public get vocabulary(): Vocabulary {
		return LibParser.VOCABULARY;
	}
	// tslint:enable:no-trailing-whitespace

	// @Override
	public get grammarFileName(): string { return "LibParser.g4"; }

	// @Override
	public get ruleNames(): string[] { return LibParser.ruleNames; }

	// @Override
	public get serializedATN(): string { return LibParser._serializedATN; }

	constructor(input: TokenStream) {
		super(input);
		this._interp = new ParserATNSimulator(LibParser._ATN, this);
	}
	// @RuleVersion(0)
	public booleanRule(): BooleanRuleContext {
		let _localctx: BooleanRuleContext = new BooleanRuleContext(this._ctx, this.state);
		this.enterRule(_localctx, 0, LibParser.RULE_booleanRule);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 4;
			_la = this._input.LA(1);
			if (!(_la === LibParser.TRUE || _la === LibParser.FALSE)) {
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
	public longReference(): LongReferenceContext {
		let _localctx: LongReferenceContext = new LongReferenceContext(this._ctx, this.state);
		this.enterRule(_localctx, 2, LibParser.RULE_longReference);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 9;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while ((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << LibParser.LONG_REF_ESCAPES) | (1 << LibParser.LONG_REF_CHARACTERS) | (1 << LibParser.LONG_REF_VARIABLE_REFERENCE))) !== 0)) {
				{
				{
				this.state = 6;
				_la = this._input.LA(1);
				if (!((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << LibParser.LONG_REF_ESCAPES) | (1 << LibParser.LONG_REF_CHARACTERS) | (1 << LibParser.LONG_REF_VARIABLE_REFERENCE))) !== 0))) {
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
				this.state = 11;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 12;
			this.match(LibParser.END_LONG_REFERENCE);
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
		"\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x03\x1C\x11\x04\x02" +
		"\t\x02\x04\x03\t\x03\x03\x02\x03\x02\x03\x03\x07\x03\n\n\x03\f\x03\x0E" +
		"\x03\r\v\x03\x03\x03\x03\x03\x03\x03\x02\x02\x02\x04\x02\x02\x04\x02\x02" +
		"\x04\x03\x02\x05\x06\x03\x02\x15\x17\x02\x0F\x02\x06\x03\x02\x02\x02\x04" +
		"\v\x03\x02\x02\x02\x06\x07\t\x02\x02\x02\x07\x03\x03\x02\x02\x02\b\n\t" +
		"\x03\x02\x02\t\b\x03\x02\x02\x02\n\r\x03\x02\x02\x02\v\t\x03\x02\x02\x02" +
		"\v\f\x03\x02\x02\x02\f\x0E\x03\x02\x02\x02\r\v\x03\x02\x02\x02\x0E\x0F" +
		"\x07\x18\x02\x02\x0F\x05\x03\x02\x02\x02\x03\v";
	public static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!LibParser.__ATN) {
			LibParser.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(LibParser._serializedATN));
		}

		return LibParser.__ATN;
	}

}

export class BooleanRuleContext extends ParserRuleContext {
	public TRUE(): TerminalNode | undefined { return this.tryGetToken(LibParser.TRUE, 0); }
	public FALSE(): TerminalNode | undefined { return this.tryGetToken(LibParser.FALSE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return LibParser.RULE_booleanRule; }
	// @Override
	public enterRule(listener: LibParserListener): void {
		if (listener.enterBooleanRule) {
			listener.enterBooleanRule(this);
		}
	}
	// @Override
	public exitRule(listener: LibParserListener): void {
		if (listener.exitBooleanRule) {
			listener.exitBooleanRule(this);
		}
	}
	// @Override
	public accept<Result>(visitor: LibParserVisitor<Result>): Result {
		if (visitor.visitBooleanRule) {
			return visitor.visitBooleanRule(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LongReferenceContext extends ParserRuleContext {
	public END_LONG_REFERENCE(): TerminalNode { return this.getToken(LibParser.END_LONG_REFERENCE, 0); }
	public LONG_REF_ESCAPES(): TerminalNode[];
	public LONG_REF_ESCAPES(i: number): TerminalNode;
	public LONG_REF_ESCAPES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(LibParser.LONG_REF_ESCAPES);
		} else {
			return this.getToken(LibParser.LONG_REF_ESCAPES, i);
		}
	}
	public LONG_REF_CHARACTERS(): TerminalNode[];
	public LONG_REF_CHARACTERS(i: number): TerminalNode;
	public LONG_REF_CHARACTERS(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(LibParser.LONG_REF_CHARACTERS);
		} else {
			return this.getToken(LibParser.LONG_REF_CHARACTERS, i);
		}
	}
	public LONG_REF_VARIABLE_REFERENCE(): TerminalNode[];
	public LONG_REF_VARIABLE_REFERENCE(i: number): TerminalNode;
	public LONG_REF_VARIABLE_REFERENCE(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(LibParser.LONG_REF_VARIABLE_REFERENCE);
		} else {
			return this.getToken(LibParser.LONG_REF_VARIABLE_REFERENCE, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return LibParser.RULE_longReference; }
	// @Override
	public enterRule(listener: LibParserListener): void {
		if (listener.enterLongReference) {
			listener.enterLongReference(this);
		}
	}
	// @Override
	public exitRule(listener: LibParserListener): void {
		if (listener.exitLongReference) {
			listener.exitLongReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: LibParserVisitor<Result>): Result {
		if (visitor.visitLongReference) {
			return visitor.visitLongReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


