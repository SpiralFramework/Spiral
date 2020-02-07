// Generated from ..\grammar\OSLWordScriptParser.g4 by ANTLR 4.7.3-SNAPSHOT


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

import { OSLWordScriptParserListener } from "./OSLWordScriptParserListener";
import { OSLWordScriptParserVisitor } from "./OSLWordScriptParserVisitor";


export class OSLWordScriptParser extends Parser {
	public static readonly WRD_SHORT_LABEL_REFERENCE = 1;
	public static readonly WRD_SHORT_PARAMETER_REFERENCE = 2;
	public static readonly WRD_START_LONG_LABEL_REFERENCE = 3;
	public static readonly WRD_START_LONG_PARAMETER_REFERENCE = 4;
	public static readonly INTEGER = 5;
	public static readonly DECIMAL_NUMBER = 6;
	public static readonly TRUE = 7;
	public static readonly FALSE = 8;
	public static readonly NULL = 9;
	public static readonly BEGIN_LOCALE_STRING = 10;
	public static readonly BEGIN_QUOTED_STRING = 11;
	public static readonly BEGIN_ACTION = 12;
	public static readonly ESCAPES = 13;
	public static readonly STRING_CHARACTERS = 14;
	public static readonly QUOTED_STRING_VARIABLE_REFERENCE = 15;
	public static readonly BEGIN_LONG_QUOTED_COLOUR_CODE = 16;
	public static readonly QUOTED_COLOUR_CODE = 17;
	public static readonly END_QUOTED_STRING = 18;
	public static readonly LOCALE_NAME_SEPARATOR = 19;
	public static readonly LOCALE_NAME_IDENTIFIER = 20;
	public static readonly LOCALE_VARIABLE_REFERENCE = 21;
	public static readonly END_LOCALE_STRING = 22;
	public static readonly LONG_REF_ESCAPES = 23;
	public static readonly LONG_REF_CHARACTERS = 24;
	public static readonly LONG_REF_VARIABLE_REFERENCE = 25;
	public static readonly END_LONG_REFERENCE = 26;
	public static readonly ACTION_ESCAPES = 27;
	public static readonly ACTION_CHARACTERS = 28;
	public static readonly ACTION_VARIABLE_REFERENCE = 29;
	public static readonly END_ACTION = 30;
	public static readonly RULE_wrdLabelReference = 0;
	public static readonly RULE_wrdParameterReference = 1;
	public static readonly RULE_wrdLongLabelReference = 2;
	public static readonly RULE_wrdLongParameterReference = 3;
	public static readonly RULE_booleanRule = 4;
	public static readonly RULE_longReference = 5;
	// tslint:disable:no-trailing-whitespace
	public static readonly ruleNames: string[] = [
		"wrdLabelReference", "wrdParameterReference", "wrdLongLabelReference", 
		"wrdLongParameterReference", "booleanRule", "longReference",
	];

	private static readonly _LITERAL_NAMES: Array<string | undefined> = [
		undefined, undefined, undefined, "'@{'", "'%{'", undefined, undefined, 
		undefined, undefined, undefined, "'\"locale.'", undefined, "'['", undefined, 
		undefined, undefined, "'&{'", undefined, undefined, "'.'", undefined, 
		undefined, undefined, undefined, undefined, undefined, "'}'", undefined, 
		undefined, undefined, "']'",
	];
	private static readonly _SYMBOLIC_NAMES: Array<string | undefined> = [
		undefined, "WRD_SHORT_LABEL_REFERENCE", "WRD_SHORT_PARAMETER_REFERENCE", 
		"WRD_START_LONG_LABEL_REFERENCE", "WRD_START_LONG_PARAMETER_REFERENCE", 
		"INTEGER", "DECIMAL_NUMBER", "TRUE", "FALSE", "NULL", "BEGIN_LOCALE_STRING", 
		"BEGIN_QUOTED_STRING", "BEGIN_ACTION", "ESCAPES", "STRING_CHARACTERS", 
		"QUOTED_STRING_VARIABLE_REFERENCE", "BEGIN_LONG_QUOTED_COLOUR_CODE", "QUOTED_COLOUR_CODE", 
		"END_QUOTED_STRING", "LOCALE_NAME_SEPARATOR", "LOCALE_NAME_IDENTIFIER", 
		"LOCALE_VARIABLE_REFERENCE", "END_LOCALE_STRING", "LONG_REF_ESCAPES", 
		"LONG_REF_CHARACTERS", "LONG_REF_VARIABLE_REFERENCE", "END_LONG_REFERENCE", 
		"ACTION_ESCAPES", "ACTION_CHARACTERS", "ACTION_VARIABLE_REFERENCE", "END_ACTION",
	];
	public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(OSLWordScriptParser._LITERAL_NAMES, OSLWordScriptParser._SYMBOLIC_NAMES, []);

	// @Override
	// @NotNull
	public get vocabulary(): Vocabulary {
		return OSLWordScriptParser.VOCABULARY;
	}
	// tslint:enable:no-trailing-whitespace

	// @Override
	public get grammarFileName(): string { return "OSLWordScriptParser.g4"; }

	// @Override
	public get ruleNames(): string[] { return OSLWordScriptParser.ruleNames; }

	// @Override
	public get serializedATN(): string { return OSLWordScriptParser._serializedATN; }

	constructor(input: TokenStream) {
		super(input);
		this._interp = new ParserATNSimulator(OSLWordScriptParser._ATN, this);
	}
	// @RuleVersion(0)
	public wrdLabelReference(): WrdLabelReferenceContext {
		let _localctx: WrdLabelReferenceContext = new WrdLabelReferenceContext(this._ctx, this.state);
		this.enterRule(_localctx, 0, OSLWordScriptParser.RULE_wrdLabelReference);
		try {
			this.state = 14;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case OSLWordScriptParser.WRD_SHORT_LABEL_REFERENCE:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 12;
				this.match(OSLWordScriptParser.WRD_SHORT_LABEL_REFERENCE);
				}
				break;
			case OSLWordScriptParser.WRD_START_LONG_LABEL_REFERENCE:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 13;
				this.wrdLongLabelReference();
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public wrdParameterReference(): WrdParameterReferenceContext {
		let _localctx: WrdParameterReferenceContext = new WrdParameterReferenceContext(this._ctx, this.state);
		this.enterRule(_localctx, 2, OSLWordScriptParser.RULE_wrdParameterReference);
		try {
			this.state = 18;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case OSLWordScriptParser.WRD_SHORT_PARAMETER_REFERENCE:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 16;
				this.match(OSLWordScriptParser.WRD_SHORT_PARAMETER_REFERENCE);
				}
				break;
			case OSLWordScriptParser.WRD_START_LONG_PARAMETER_REFERENCE:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 17;
				this.wrdLongParameterReference();
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public wrdLongLabelReference(): WrdLongLabelReferenceContext {
		let _localctx: WrdLongLabelReferenceContext = new WrdLongLabelReferenceContext(this._ctx, this.state);
		this.enterRule(_localctx, 4, OSLWordScriptParser.RULE_wrdLongLabelReference);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 20;
			this.match(OSLWordScriptParser.WRD_START_LONG_LABEL_REFERENCE);
			this.state = 21;
			this.longReference();
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
	public wrdLongParameterReference(): WrdLongParameterReferenceContext {
		let _localctx: WrdLongParameterReferenceContext = new WrdLongParameterReferenceContext(this._ctx, this.state);
		this.enterRule(_localctx, 6, OSLWordScriptParser.RULE_wrdLongParameterReference);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 23;
			this.match(OSLWordScriptParser.WRD_START_LONG_PARAMETER_REFERENCE);
			this.state = 24;
			this.longReference();
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
	public booleanRule(): BooleanRuleContext {
		let _localctx: BooleanRuleContext = new BooleanRuleContext(this._ctx, this.state);
		this.enterRule(_localctx, 8, OSLWordScriptParser.RULE_booleanRule);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 26;
			_la = this._input.LA(1);
			if (!(_la === OSLWordScriptParser.TRUE || _la === OSLWordScriptParser.FALSE)) {
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
		this.enterRule(_localctx, 10, OSLWordScriptParser.RULE_longReference);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 31;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while ((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << OSLWordScriptParser.LONG_REF_ESCAPES) | (1 << OSLWordScriptParser.LONG_REF_CHARACTERS) | (1 << OSLWordScriptParser.LONG_REF_VARIABLE_REFERENCE))) !== 0)) {
				{
				{
				this.state = 28;
				_la = this._input.LA(1);
				if (!((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << OSLWordScriptParser.LONG_REF_ESCAPES) | (1 << OSLWordScriptParser.LONG_REF_CHARACTERS) | (1 << OSLWordScriptParser.LONG_REF_VARIABLE_REFERENCE))) !== 0))) {
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
				this.state = 33;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 34;
			this.match(OSLWordScriptParser.END_LONG_REFERENCE);
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
		"\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x03 \'\x04\x02\t" +
		"\x02\x04\x03\t\x03\x04\x04\t\x04\x04\x05\t\x05\x04\x06\t\x06\x04\x07\t" +
		"\x07\x03\x02\x03\x02\x05\x02\x11\n\x02\x03\x03\x03\x03\x05\x03\x15\n\x03" +
		"\x03\x04\x03\x04\x03\x04\x03\x05\x03\x05\x03\x05\x03\x06\x03\x06\x03\x07" +
		"\x07\x07 \n\x07\f\x07\x0E\x07#\v\x07\x03\x07\x03\x07\x03\x07\x02\x02\x02" +
		"\b\x02\x02\x04\x02\x06\x02\b\x02\n\x02\f\x02\x02\x04\x03\x02\t\n\x03\x02" +
		"\x19\x1B\x02#\x02\x10\x03\x02\x02\x02\x04\x14\x03\x02\x02\x02\x06\x16" +
		"\x03\x02\x02\x02\b\x19\x03\x02\x02\x02\n\x1C\x03\x02\x02\x02\f!\x03\x02" +
		"\x02\x02\x0E\x11\x07\x03\x02\x02\x0F\x11\x05\x06\x04\x02\x10\x0E\x03\x02" +
		"\x02\x02\x10\x0F\x03\x02\x02\x02\x11\x03\x03\x02\x02\x02\x12\x15\x07\x04" +
		"\x02\x02\x13\x15\x05\b\x05\x02\x14\x12\x03\x02\x02\x02\x14\x13\x03\x02" +
		"\x02\x02\x15\x05\x03\x02\x02\x02\x16\x17\x07\x05\x02\x02\x17\x18\x05\f" +
		"\x07\x02\x18\x07\x03\x02\x02\x02\x19\x1A\x07\x06\x02\x02\x1A\x1B\x05\f" +
		"\x07\x02\x1B\t\x03\x02\x02\x02\x1C\x1D\t\x02\x02\x02\x1D\v\x03\x02\x02" +
		"\x02\x1E \t\x03\x02\x02\x1F\x1E\x03\x02\x02\x02 #\x03\x02\x02\x02!\x1F" +
		"\x03\x02\x02\x02!\"\x03\x02\x02\x02\"$\x03\x02\x02\x02#!\x03\x02\x02\x02" +
		"$%\x07\x1C\x02\x02%\r\x03\x02\x02\x02\x05\x10\x14!";
	public static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!OSLWordScriptParser.__ATN) {
			OSLWordScriptParser.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(OSLWordScriptParser._serializedATN));
		}

		return OSLWordScriptParser.__ATN;
	}

}

export class WrdLabelReferenceContext extends ParserRuleContext {
	public WRD_SHORT_LABEL_REFERENCE(): TerminalNode | undefined { return this.tryGetToken(OSLWordScriptParser.WRD_SHORT_LABEL_REFERENCE, 0); }
	public wrdLongLabelReference(): WrdLongLabelReferenceContext | undefined {
		return this.tryGetRuleContext(0, WrdLongLabelReferenceContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OSLWordScriptParser.RULE_wrdLabelReference; }
	// @Override
	public enterRule(listener: OSLWordScriptParserListener): void {
		if (listener.enterWrdLabelReference) {
			listener.enterWrdLabelReference(this);
		}
	}
	// @Override
	public exitRule(listener: OSLWordScriptParserListener): void {
		if (listener.exitWrdLabelReference) {
			listener.exitWrdLabelReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OSLWordScriptParserVisitor<Result>): Result {
		if (visitor.visitWrdLabelReference) {
			return visitor.visitWrdLabelReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WrdParameterReferenceContext extends ParserRuleContext {
	public WRD_SHORT_PARAMETER_REFERENCE(): TerminalNode | undefined { return this.tryGetToken(OSLWordScriptParser.WRD_SHORT_PARAMETER_REFERENCE, 0); }
	public wrdLongParameterReference(): WrdLongParameterReferenceContext | undefined {
		return this.tryGetRuleContext(0, WrdLongParameterReferenceContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OSLWordScriptParser.RULE_wrdParameterReference; }
	// @Override
	public enterRule(listener: OSLWordScriptParserListener): void {
		if (listener.enterWrdParameterReference) {
			listener.enterWrdParameterReference(this);
		}
	}
	// @Override
	public exitRule(listener: OSLWordScriptParserListener): void {
		if (listener.exitWrdParameterReference) {
			listener.exitWrdParameterReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OSLWordScriptParserVisitor<Result>): Result {
		if (visitor.visitWrdParameterReference) {
			return visitor.visitWrdParameterReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WrdLongLabelReferenceContext extends ParserRuleContext {
	public WRD_START_LONG_LABEL_REFERENCE(): TerminalNode { return this.getToken(OSLWordScriptParser.WRD_START_LONG_LABEL_REFERENCE, 0); }
	public longReference(): LongReferenceContext {
		return this.getRuleContext(0, LongReferenceContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OSLWordScriptParser.RULE_wrdLongLabelReference; }
	// @Override
	public enterRule(listener: OSLWordScriptParserListener): void {
		if (listener.enterWrdLongLabelReference) {
			listener.enterWrdLongLabelReference(this);
		}
	}
	// @Override
	public exitRule(listener: OSLWordScriptParserListener): void {
		if (listener.exitWrdLongLabelReference) {
			listener.exitWrdLongLabelReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OSLWordScriptParserVisitor<Result>): Result {
		if (visitor.visitWrdLongLabelReference) {
			return visitor.visitWrdLongLabelReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WrdLongParameterReferenceContext extends ParserRuleContext {
	public WRD_START_LONG_PARAMETER_REFERENCE(): TerminalNode { return this.getToken(OSLWordScriptParser.WRD_START_LONG_PARAMETER_REFERENCE, 0); }
	public longReference(): LongReferenceContext {
		return this.getRuleContext(0, LongReferenceContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OSLWordScriptParser.RULE_wrdLongParameterReference; }
	// @Override
	public enterRule(listener: OSLWordScriptParserListener): void {
		if (listener.enterWrdLongParameterReference) {
			listener.enterWrdLongParameterReference(this);
		}
	}
	// @Override
	public exitRule(listener: OSLWordScriptParserListener): void {
		if (listener.exitWrdLongParameterReference) {
			listener.exitWrdLongParameterReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OSLWordScriptParserVisitor<Result>): Result {
		if (visitor.visitWrdLongParameterReference) {
			return visitor.visitWrdLongParameterReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class BooleanRuleContext extends ParserRuleContext {
	public TRUE(): TerminalNode | undefined { return this.tryGetToken(OSLWordScriptParser.TRUE, 0); }
	public FALSE(): TerminalNode | undefined { return this.tryGetToken(OSLWordScriptParser.FALSE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OSLWordScriptParser.RULE_booleanRule; }
	// @Override
	public enterRule(listener: OSLWordScriptParserListener): void {
		if (listener.enterBooleanRule) {
			listener.enterBooleanRule(this);
		}
	}
	// @Override
	public exitRule(listener: OSLWordScriptParserListener): void {
		if (listener.exitBooleanRule) {
			listener.exitBooleanRule(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OSLWordScriptParserVisitor<Result>): Result {
		if (visitor.visitBooleanRule) {
			return visitor.visitBooleanRule(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LongReferenceContext extends ParserRuleContext {
	public END_LONG_REFERENCE(): TerminalNode { return this.getToken(OSLWordScriptParser.END_LONG_REFERENCE, 0); }
	public LONG_REF_ESCAPES(): TerminalNode[];
	public LONG_REF_ESCAPES(i: number): TerminalNode;
	public LONG_REF_ESCAPES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OSLWordScriptParser.LONG_REF_ESCAPES);
		} else {
			return this.getToken(OSLWordScriptParser.LONG_REF_ESCAPES, i);
		}
	}
	public LONG_REF_CHARACTERS(): TerminalNode[];
	public LONG_REF_CHARACTERS(i: number): TerminalNode;
	public LONG_REF_CHARACTERS(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OSLWordScriptParser.LONG_REF_CHARACTERS);
		} else {
			return this.getToken(OSLWordScriptParser.LONG_REF_CHARACTERS, i);
		}
	}
	public LONG_REF_VARIABLE_REFERENCE(): TerminalNode[];
	public LONG_REF_VARIABLE_REFERENCE(i: number): TerminalNode;
	public LONG_REF_VARIABLE_REFERENCE(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OSLWordScriptParser.LONG_REF_VARIABLE_REFERENCE);
		} else {
			return this.getToken(OSLWordScriptParser.LONG_REF_VARIABLE_REFERENCE, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OSLWordScriptParser.RULE_longReference; }
	// @Override
	public enterRule(listener: OSLWordScriptParserListener): void {
		if (listener.enterLongReference) {
			listener.enterLongReference(this);
		}
	}
	// @Override
	public exitRule(listener: OSLWordScriptParserListener): void {
		if (listener.exitLongReference) {
			listener.exitLongReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OSLWordScriptParserVisitor<Result>): Result {
		if (visitor.visitLongReference) {
			return visitor.visitLongReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


