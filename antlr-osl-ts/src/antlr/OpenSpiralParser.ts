// Generated from ..\grammar\OpenSpiralParser.g4 by ANTLR 4.7.3-SNAPSHOT


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

import { OpenSpiralParserListener } from "./OpenSpiralParserListener";
import { OpenSpiralParserVisitor } from "./OpenSpiralParserVisitor";


export class OpenSpiralParser extends Parser {
	public static readonly HEADER_DECLARATION = 1;
	public static readonly SEMICOLON_SEPARATOR = 2;
	public static readonly NL_SEPARATOR = 3;
	public static readonly ASSIGN_VARIABLE_NAME = 4;
	public static readonly VARIABLE_ASSIGNMENT = 5;
	public static readonly VARIABLE_REFERENCE = 6;
	public static readonly SEMANTIC_VERSION = 7;
	public static readonly VALUE_SEPARATOR = 8;
	public static readonly NAME_IDENTIFIER = 9;
	public static readonly DIALOG_DRILL_NAME = 10;
	public static readonly BASIC_DRILL_SEPARATOR = 11;
	public static readonly DIALOGUE_SEPARATOR = 12;
	public static readonly INTEGER = 13;
	public static readonly DECIMAL_NUMBER = 14;
	public static readonly TRUE = 15;
	public static readonly FALSE = 16;
	public static readonly NULL = 17;
	public static readonly BEGIN_LOCALE_STRING = 18;
	public static readonly BEGIN_QUOTED_STRING = 19;
	public static readonly BEGIN_ACTION = 20;
	public static readonly BACKTICK = 21;
	public static readonly BACKTICK_START = 22;
	public static readonly WRD_SHORT_LABEL_REFERENCE = 23;
	public static readonly WRD_SHORT_PARAMETER_REFERENCE = 24;
	public static readonly WRD_START_LONG_LABEL_REFERENCE = 25;
	public static readonly WRD_START_LONG_PARAMETER_REFERENCE = 26;
	public static readonly ESCAPES = 27;
	public static readonly STRING_CHARACTERS = 28;
	public static readonly QUOTED_STRING_VARIABLE_REFERENCE = 29;
	public static readonly BEGIN_LONG_QUOTED_COLOUR_CODE = 30;
	public static readonly QUOTED_COLOUR_CODE = 31;
	public static readonly END_QUOTED_STRING = 32;
	public static readonly LOCALE_NAME_SEPARATOR = 33;
	public static readonly LOCALE_NAME_IDENTIFIER = 34;
	public static readonly LOCALE_VARIABLE_REFERENCE = 35;
	public static readonly END_LOCALE_STRING = 36;
	public static readonly LONG_REF_ESCAPES = 37;
	public static readonly LONG_REF_CHARACTERS = 38;
	public static readonly LONG_REF_VARIABLE_REFERENCE = 39;
	public static readonly END_LONG_REFERENCE = 40;
	public static readonly ACTION_ESCAPES = 41;
	public static readonly ACTION_CHARACTERS = 42;
	public static readonly ACTION_VARIABLE_REFERENCE = 43;
	public static readonly END_ACTION = 44;
	public static readonly BACKTICKED_ESCAPES = 45;
	public static readonly BACKTICKED_STRING_CHARACTERS = 46;
	public static readonly BACKTICK_END = 47;
	public static readonly RULE_headerDeclaration = 0;
	public static readonly RULE_script = 1;
	public static readonly RULE_lineSeparator = 2;
	public static readonly RULE_scriptLine = 3;
	public static readonly RULE_metaVariableAssignment = 4;
	public static readonly RULE_basicDrill = 5;
	public static readonly RULE_basicDrillNamed = 6;
	public static readonly RULE_quotedString = 7;
	public static readonly RULE_localisedString = 8;
	public static readonly RULE_localisedComponent = 9;
	public static readonly RULE_longColourReference = 10;
	public static readonly RULE_basicDrillValue = 11;
	public static readonly RULE_variableValue = 12;
	public static readonly RULE_actionDeclaration = 13;
	public static readonly RULE_complexDrills = 14;
	public static readonly RULE_dialogueDrill = 15;
	public static readonly RULE_wrdLabelReference = 16;
	public static readonly RULE_wrdParameterReference = 17;
	public static readonly RULE_wrdLongLabelReference = 18;
	public static readonly RULE_wrdLongParameterReference = 19;
	public static readonly RULE_booleanRule = 20;
	public static readonly RULE_longReference = 21;
	// tslint:disable:no-trailing-whitespace
	public static readonly ruleNames: string[] = [
		"headerDeclaration", "script", "lineSeparator", "scriptLine", "metaVariableAssignment", 
		"basicDrill", "basicDrillNamed", "quotedString", "localisedString", "localisedComponent", 
		"longColourReference", "basicDrillValue", "variableValue", "actionDeclaration", 
		"complexDrills", "dialogueDrill", "wrdLabelReference", "wrdParameterReference", 
		"wrdLongLabelReference", "wrdLongParameterReference", "booleanRule", "longReference",
	];

	private static readonly _LITERAL_NAMES: Array<string | undefined> = [
		undefined, undefined, undefined, undefined, undefined, undefined, undefined, 
		undefined, undefined, undefined, undefined, undefined, undefined, undefined, 
		undefined, undefined, undefined, undefined, "'\"locale.'", undefined, 
		"'['", undefined, undefined, undefined, undefined, "'@{'", "'%{'", undefined, 
		undefined, undefined, "'&{'", undefined, undefined, "'.'", undefined, 
		undefined, undefined, undefined, undefined, undefined, "'}'", undefined, 
		undefined, undefined, "']'",
	];
	private static readonly _SYMBOLIC_NAMES: Array<string | undefined> = [
		undefined, "HEADER_DECLARATION", "SEMICOLON_SEPARATOR", "NL_SEPARATOR", 
		"ASSIGN_VARIABLE_NAME", "VARIABLE_ASSIGNMENT", "VARIABLE_REFERENCE", "SEMANTIC_VERSION", 
		"VALUE_SEPARATOR", "NAME_IDENTIFIER", "DIALOG_DRILL_NAME", "BASIC_DRILL_SEPARATOR", 
		"DIALOGUE_SEPARATOR", "INTEGER", "DECIMAL_NUMBER", "TRUE", "FALSE", "NULL", 
		"BEGIN_LOCALE_STRING", "BEGIN_QUOTED_STRING", "BEGIN_ACTION", "BACKTICK", 
		"BACKTICK_START", "WRD_SHORT_LABEL_REFERENCE", "WRD_SHORT_PARAMETER_REFERENCE", 
		"WRD_START_LONG_LABEL_REFERENCE", "WRD_START_LONG_PARAMETER_REFERENCE", 
		"ESCAPES", "STRING_CHARACTERS", "QUOTED_STRING_VARIABLE_REFERENCE", "BEGIN_LONG_QUOTED_COLOUR_CODE", 
		"QUOTED_COLOUR_CODE", "END_QUOTED_STRING", "LOCALE_NAME_SEPARATOR", "LOCALE_NAME_IDENTIFIER", 
		"LOCALE_VARIABLE_REFERENCE", "END_LOCALE_STRING", "LONG_REF_ESCAPES", 
		"LONG_REF_CHARACTERS", "LONG_REF_VARIABLE_REFERENCE", "END_LONG_REFERENCE", 
		"ACTION_ESCAPES", "ACTION_CHARACTERS", "ACTION_VARIABLE_REFERENCE", "END_ACTION", 
		"BACKTICKED_ESCAPES", "BACKTICKED_STRING_CHARACTERS", "BACKTICK_END",
	];
	public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(OpenSpiralParser._LITERAL_NAMES, OpenSpiralParser._SYMBOLIC_NAMES, []);

	// @Override
	// @NotNull
	public get vocabulary(): Vocabulary {
		return OpenSpiralParser.VOCABULARY;
	}
	// tslint:enable:no-trailing-whitespace

	// @Override
	public get grammarFileName(): string { return "OpenSpiralParser.g4"; }

	// @Override
	public get ruleNames(): string[] { return OpenSpiralParser.ruleNames; }

	// @Override
	public get serializedATN(): string { return OpenSpiralParser._serializedATN; }

	constructor(input: TokenStream) {
		super(input);
		this._interp = new ParserATNSimulator(OpenSpiralParser._ATN, this);
	}
	// @RuleVersion(0)
	public headerDeclaration(): HeaderDeclarationContext {
		let _localctx: HeaderDeclarationContext = new HeaderDeclarationContext(this._ctx, this.state);
		this.enterRule(_localctx, 0, OpenSpiralParser.RULE_headerDeclaration);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 44;
			this.match(OpenSpiralParser.HEADER_DECLARATION);
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
	public script(): ScriptContext {
		let _localctx: ScriptContext = new ScriptContext(this._ctx, this.state);
		this.enterRule(_localctx, 2, OpenSpiralParser.RULE_script);
		let _la: number;
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 46;
			this.headerDeclaration();
			this.state = 55;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 1, this._ctx) ) {
			case 1:
				{
				this.state = 50;
				this._errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						this.state = 47;
						this.lineSeparator();
						this.state = 48;
						this.scriptLine();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					this.state = 52;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 0, this._ctx);
				} while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER);
				}
				break;

			case 2:
				{
				this.state = 54;
				this.lineSeparator();
				}
				break;
			}
			this.state = 58;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === OpenSpiralParser.SEMICOLON_SEPARATOR || _la === OpenSpiralParser.NL_SEPARATOR) {
				{
				this.state = 57;
				this.lineSeparator();
				}
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
	public lineSeparator(): LineSeparatorContext {
		let _localctx: LineSeparatorContext = new LineSeparatorContext(this._ctx, this.state);
		this.enterRule(_localctx, 4, OpenSpiralParser.RULE_lineSeparator);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 60;
			_la = this._input.LA(1);
			if (!(_la === OpenSpiralParser.SEMICOLON_SEPARATOR || _la === OpenSpiralParser.NL_SEPARATOR)) {
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
	public scriptLine(): ScriptLineContext {
		let _localctx: ScriptLineContext = new ScriptLineContext(this._ctx, this.state);
		this.enterRule(_localctx, 6, OpenSpiralParser.RULE_scriptLine);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 67;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 3, this._ctx) ) {
			case 1:
				{
				this.state = 62;
				this.basicDrill();
				}
				break;

			case 2:
				{
				this.state = 63;
				this.basicDrillNamed();
				}
				break;

			case 3:
				{
				this.state = 64;
				this.complexDrills();
				}
				break;

			case 4:
				{
				this.state = 65;
				this.metaVariableAssignment();
				}
				break;

			case 5:
				{
				this.state = 66;
				this.actionDeclaration();
				}
				break;
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
	public metaVariableAssignment(): MetaVariableAssignmentContext {
		let _localctx: MetaVariableAssignmentContext = new MetaVariableAssignmentContext(this._ctx, this.state);
		this.enterRule(_localctx, 8, OpenSpiralParser.RULE_metaVariableAssignment);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 69;
			this.match(OpenSpiralParser.ASSIGN_VARIABLE_NAME);
			this.state = 70;
			this.match(OpenSpiralParser.VARIABLE_ASSIGNMENT);
			this.state = 71;
			this.variableValue();
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
	public basicDrill(): BasicDrillContext {
		let _localctx: BasicDrillContext = new BasicDrillContext(this._ctx, this.state);
		this.enterRule(_localctx, 10, OpenSpiralParser.RULE_basicDrill);
		let _la: number;
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 73;
			this.match(OpenSpiralParser.INTEGER);
			this.state = 74;
			this.match(OpenSpiralParser.BASIC_DRILL_SEPARATOR);
			this.state = 83;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if ((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << OpenSpiralParser.VARIABLE_REFERENCE) | (1 << OpenSpiralParser.INTEGER) | (1 << OpenSpiralParser.DECIMAL_NUMBER) | (1 << OpenSpiralParser.TRUE) | (1 << OpenSpiralParser.FALSE) | (1 << OpenSpiralParser.NULL) | (1 << OpenSpiralParser.BEGIN_LOCALE_STRING) | (1 << OpenSpiralParser.BEGIN_QUOTED_STRING) | (1 << OpenSpiralParser.WRD_SHORT_LABEL_REFERENCE) | (1 << OpenSpiralParser.WRD_SHORT_PARAMETER_REFERENCE) | (1 << OpenSpiralParser.WRD_START_LONG_LABEL_REFERENCE) | (1 << OpenSpiralParser.WRD_START_LONG_PARAMETER_REFERENCE))) !== 0)) {
				{
				this.state = 75;
				this.basicDrillValue();
				this.state = 80;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 4, this._ctx);
				while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1 + 1) {
						{
						{
						this.state = 76;
						this.match(OpenSpiralParser.VALUE_SEPARATOR);
						this.state = 77;
						this.basicDrillValue();
						}
						}
					}
					this.state = 82;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 4, this._ctx);
				}
				}
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
	public basicDrillNamed(): BasicDrillNamedContext {
		let _localctx: BasicDrillNamedContext = new BasicDrillNamedContext(this._ctx, this.state);
		this.enterRule(_localctx, 12, OpenSpiralParser.RULE_basicDrillNamed);
		let _la: number;
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 85;
			this.match(OpenSpiralParser.NAME_IDENTIFIER);
			this.state = 86;
			this.match(OpenSpiralParser.BASIC_DRILL_SEPARATOR);
			this.state = 95;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if ((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << OpenSpiralParser.VARIABLE_REFERENCE) | (1 << OpenSpiralParser.INTEGER) | (1 << OpenSpiralParser.DECIMAL_NUMBER) | (1 << OpenSpiralParser.TRUE) | (1 << OpenSpiralParser.FALSE) | (1 << OpenSpiralParser.NULL) | (1 << OpenSpiralParser.BEGIN_LOCALE_STRING) | (1 << OpenSpiralParser.BEGIN_QUOTED_STRING) | (1 << OpenSpiralParser.WRD_SHORT_LABEL_REFERENCE) | (1 << OpenSpiralParser.WRD_SHORT_PARAMETER_REFERENCE) | (1 << OpenSpiralParser.WRD_START_LONG_LABEL_REFERENCE) | (1 << OpenSpiralParser.WRD_START_LONG_PARAMETER_REFERENCE))) !== 0)) {
				{
				this.state = 87;
				this.basicDrillValue();
				this.state = 92;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 6, this._ctx);
				while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1 + 1) {
						{
						{
						this.state = 88;
						this.match(OpenSpiralParser.VALUE_SEPARATOR);
						this.state = 89;
						this.basicDrillValue();
						}
						}
					}
					this.state = 94;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 6, this._ctx);
				}
				}
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
	public quotedString(): QuotedStringContext {
		let _localctx: QuotedStringContext = new QuotedStringContext(this._ctx, this.state);
		this.enterRule(_localctx, 14, OpenSpiralParser.RULE_quotedString);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 97;
			this.match(OpenSpiralParser.BEGIN_QUOTED_STRING);
			this.state = 105;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while ((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << OpenSpiralParser.ESCAPES) | (1 << OpenSpiralParser.STRING_CHARACTERS) | (1 << OpenSpiralParser.QUOTED_STRING_VARIABLE_REFERENCE) | (1 << OpenSpiralParser.BEGIN_LONG_QUOTED_COLOUR_CODE) | (1 << OpenSpiralParser.QUOTED_COLOUR_CODE))) !== 0)) {
				{
				this.state = 103;
				this._errHandler.sync(this);
				switch (this._input.LA(1)) {
				case OpenSpiralParser.ESCAPES:
					{
					this.state = 98;
					this.match(OpenSpiralParser.ESCAPES);
					}
					break;
				case OpenSpiralParser.STRING_CHARACTERS:
					{
					this.state = 99;
					this.match(OpenSpiralParser.STRING_CHARACTERS);
					}
					break;
				case OpenSpiralParser.QUOTED_STRING_VARIABLE_REFERENCE:
					{
					this.state = 100;
					this.match(OpenSpiralParser.QUOTED_STRING_VARIABLE_REFERENCE);
					}
					break;
				case OpenSpiralParser.BEGIN_LONG_QUOTED_COLOUR_CODE:
					{
					this.state = 101;
					this.longColourReference();
					}
					break;
				case OpenSpiralParser.QUOTED_COLOUR_CODE:
					{
					this.state = 102;
					this.match(OpenSpiralParser.QUOTED_COLOUR_CODE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				this.state = 107;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 108;
			this.match(OpenSpiralParser.END_QUOTED_STRING);
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
	public localisedString(): LocalisedStringContext {
		let _localctx: LocalisedStringContext = new LocalisedStringContext(this._ctx, this.state);
		this.enterRule(_localctx, 16, OpenSpiralParser.RULE_localisedString);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 110;
			this.match(OpenSpiralParser.BEGIN_LOCALE_STRING);
			this.state = 119;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === OpenSpiralParser.LOCALE_NAME_IDENTIFIER || _la === OpenSpiralParser.LOCALE_VARIABLE_REFERENCE) {
				{
				this.state = 111;
				this.localisedComponent();
				this.state = 116;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === OpenSpiralParser.LOCALE_NAME_SEPARATOR) {
					{
					{
					this.state = 112;
					this.match(OpenSpiralParser.LOCALE_NAME_SEPARATOR);
					this.state = 113;
					this.localisedComponent();
					}
					}
					this.state = 118;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				}
			}

			this.state = 121;
			this.match(OpenSpiralParser.END_LOCALE_STRING);
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
	public localisedComponent(): LocalisedComponentContext {
		let _localctx: LocalisedComponentContext = new LocalisedComponentContext(this._ctx, this.state);
		this.enterRule(_localctx, 18, OpenSpiralParser.RULE_localisedComponent);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 123;
			_la = this._input.LA(1);
			if (!(_la === OpenSpiralParser.LOCALE_NAME_IDENTIFIER || _la === OpenSpiralParser.LOCALE_VARIABLE_REFERENCE)) {
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
	public longColourReference(): LongColourReferenceContext {
		let _localctx: LongColourReferenceContext = new LongColourReferenceContext(this._ctx, this.state);
		this.enterRule(_localctx, 20, OpenSpiralParser.RULE_longColourReference);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 125;
			this.match(OpenSpiralParser.BEGIN_LONG_QUOTED_COLOUR_CODE);
			this.state = 126;
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
	public basicDrillValue(): BasicDrillValueContext {
		let _localctx: BasicDrillValueContext = new BasicDrillValueContext(this._ctx, this.state);
		this.enterRule(_localctx, 22, OpenSpiralParser.RULE_basicDrillValue);
		try {
			this.state = 131;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case OpenSpiralParser.WRD_SHORT_LABEL_REFERENCE:
			case OpenSpiralParser.WRD_START_LONG_LABEL_REFERENCE:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 128;
				this.wrdLabelReference();
				}
				break;
			case OpenSpiralParser.WRD_SHORT_PARAMETER_REFERENCE:
			case OpenSpiralParser.WRD_START_LONG_PARAMETER_REFERENCE:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 129;
				this.wrdParameterReference();
				}
				break;
			case OpenSpiralParser.VARIABLE_REFERENCE:
			case OpenSpiralParser.INTEGER:
			case OpenSpiralParser.DECIMAL_NUMBER:
			case OpenSpiralParser.TRUE:
			case OpenSpiralParser.FALSE:
			case OpenSpiralParser.NULL:
			case OpenSpiralParser.BEGIN_LOCALE_STRING:
			case OpenSpiralParser.BEGIN_QUOTED_STRING:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 130;
				this.variableValue();
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
	public variableValue(): VariableValueContext {
		let _localctx: VariableValueContext = new VariableValueContext(this._ctx, this.state);
		this.enterRule(_localctx, 24, OpenSpiralParser.RULE_variableValue);
		try {
			this.state = 140;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case OpenSpiralParser.BEGIN_LOCALE_STRING:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 133;
				this.localisedString();
				}
				break;
			case OpenSpiralParser.BEGIN_QUOTED_STRING:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 134;
				this.quotedString();
				}
				break;
			case OpenSpiralParser.TRUE:
			case OpenSpiralParser.FALSE:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 135;
				this.booleanRule();
				}
				break;
			case OpenSpiralParser.INTEGER:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 136;
				this.match(OpenSpiralParser.INTEGER);
				}
				break;
			case OpenSpiralParser.DECIMAL_NUMBER:
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 137;
				this.match(OpenSpiralParser.DECIMAL_NUMBER);
				}
				break;
			case OpenSpiralParser.VARIABLE_REFERENCE:
				this.enterOuterAlt(_localctx, 6);
				{
				this.state = 138;
				this.match(OpenSpiralParser.VARIABLE_REFERENCE);
				}
				break;
			case OpenSpiralParser.NULL:
				this.enterOuterAlt(_localctx, 7);
				{
				this.state = 139;
				this.match(OpenSpiralParser.NULL);
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
	public actionDeclaration(): ActionDeclarationContext {
		let _localctx: ActionDeclarationContext = new ActionDeclarationContext(this._ctx, this.state);
		this.enterRule(_localctx, 26, OpenSpiralParser.RULE_actionDeclaration);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 142;
			this.match(OpenSpiralParser.BEGIN_ACTION);
			this.state = 146;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (((((_la - 41)) & ~0x1F) === 0 && ((1 << (_la - 41)) & ((1 << (OpenSpiralParser.ACTION_ESCAPES - 41)) | (1 << (OpenSpiralParser.ACTION_CHARACTERS - 41)) | (1 << (OpenSpiralParser.ACTION_VARIABLE_REFERENCE - 41)))) !== 0)) {
				{
				{
				this.state = 143;
				_la = this._input.LA(1);
				if (!(((((_la - 41)) & ~0x1F) === 0 && ((1 << (_la - 41)) & ((1 << (OpenSpiralParser.ACTION_ESCAPES - 41)) | (1 << (OpenSpiralParser.ACTION_CHARACTERS - 41)) | (1 << (OpenSpiralParser.ACTION_VARIABLE_REFERENCE - 41)))) !== 0))) {
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
				this.state = 148;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 149;
			this.match(OpenSpiralParser.END_ACTION);
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
	public complexDrills(): ComplexDrillsContext {
		let _localctx: ComplexDrillsContext = new ComplexDrillsContext(this._ctx, this.state);
		this.enterRule(_localctx, 28, OpenSpiralParser.RULE_complexDrills);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 151;
			this.dialogueDrill();
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
	public dialogueDrill(): DialogueDrillContext {
		let _localctx: DialogueDrillContext = new DialogueDrillContext(this._ctx, this.state);
		this.enterRule(_localctx, 30, OpenSpiralParser.RULE_dialogueDrill);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 153;
			_la = this._input.LA(1);
			if (!(_la === OpenSpiralParser.VARIABLE_REFERENCE || _la === OpenSpiralParser.NAME_IDENTIFIER)) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			this.state = 154;
			this.match(OpenSpiralParser.DIALOGUE_SEPARATOR);
			this.state = 155;
			this.variableValue();
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
	public wrdLabelReference(): WrdLabelReferenceContext {
		let _localctx: WrdLabelReferenceContext = new WrdLabelReferenceContext(this._ctx, this.state);
		this.enterRule(_localctx, 32, OpenSpiralParser.RULE_wrdLabelReference);
		try {
			this.state = 159;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case OpenSpiralParser.WRD_SHORT_LABEL_REFERENCE:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 157;
				this.match(OpenSpiralParser.WRD_SHORT_LABEL_REFERENCE);
				}
				break;
			case OpenSpiralParser.WRD_START_LONG_LABEL_REFERENCE:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 158;
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
		this.enterRule(_localctx, 34, OpenSpiralParser.RULE_wrdParameterReference);
		try {
			this.state = 163;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case OpenSpiralParser.WRD_SHORT_PARAMETER_REFERENCE:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 161;
				this.match(OpenSpiralParser.WRD_SHORT_PARAMETER_REFERENCE);
				}
				break;
			case OpenSpiralParser.WRD_START_LONG_PARAMETER_REFERENCE:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 162;
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
		this.enterRule(_localctx, 36, OpenSpiralParser.RULE_wrdLongLabelReference);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 165;
			this.match(OpenSpiralParser.WRD_START_LONG_LABEL_REFERENCE);
			this.state = 166;
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
		this.enterRule(_localctx, 38, OpenSpiralParser.RULE_wrdLongParameterReference);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 168;
			this.match(OpenSpiralParser.WRD_START_LONG_PARAMETER_REFERENCE);
			this.state = 169;
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
		this.enterRule(_localctx, 40, OpenSpiralParser.RULE_booleanRule);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 171;
			_la = this._input.LA(1);
			if (!(_la === OpenSpiralParser.TRUE || _la === OpenSpiralParser.FALSE)) {
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
		this.enterRule(_localctx, 42, OpenSpiralParser.RULE_longReference);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 176;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (((((_la - 37)) & ~0x1F) === 0 && ((1 << (_la - 37)) & ((1 << (OpenSpiralParser.LONG_REF_ESCAPES - 37)) | (1 << (OpenSpiralParser.LONG_REF_CHARACTERS - 37)) | (1 << (OpenSpiralParser.LONG_REF_VARIABLE_REFERENCE - 37)))) !== 0)) {
				{
				{
				this.state = 173;
				_la = this._input.LA(1);
				if (!(((((_la - 37)) & ~0x1F) === 0 && ((1 << (_la - 37)) & ((1 << (OpenSpiralParser.LONG_REF_ESCAPES - 37)) | (1 << (OpenSpiralParser.LONG_REF_CHARACTERS - 37)) | (1 << (OpenSpiralParser.LONG_REF_VARIABLE_REFERENCE - 37)))) !== 0))) {
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
				this.state = 178;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 179;
			this.match(OpenSpiralParser.END_LONG_REFERENCE);
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
		"\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x031\xB8\x04\x02" +
		"\t\x02\x04\x03\t\x03\x04\x04\t\x04\x04\x05\t\x05\x04\x06\t\x06\x04\x07" +
		"\t\x07\x04\b\t\b\x04\t\t\t\x04\n\t\n\x04\v\t\v\x04\f\t\f\x04\r\t\r\x04" +
		"\x0E\t\x0E\x04\x0F\t\x0F\x04\x10\t\x10\x04\x11\t\x11\x04\x12\t\x12\x04" +
		"\x13\t\x13\x04\x14\t\x14\x04\x15\t\x15\x04\x16\t\x16\x04\x17\t\x17\x03" +
		"\x02\x03\x02\x03\x03\x03\x03\x03\x03\x03\x03\x06\x035\n\x03\r\x03\x0E" +
		"\x036\x03\x03\x05\x03:\n\x03\x03\x03\x05\x03=\n\x03\x03\x04\x03\x04\x03" +
		"\x05\x03\x05\x03\x05\x03\x05\x03\x05\x05\x05F\n\x05\x03\x06\x03\x06\x03" +
		"\x06\x03\x06\x03\x07\x03\x07\x03\x07\x03\x07\x03\x07\x07\x07Q\n\x07\f" +
		"\x07\x0E\x07T\v\x07\x05\x07V\n\x07\x03\b\x03\b\x03\b\x03\b\x03\b\x07\b" +
		"]\n\b\f\b\x0E\b`\v\b\x05\bb\n\b\x03\t\x03\t\x03\t\x03\t\x03\t\x03\t\x07" +
		"\tj\n\t\f\t\x0E\tm\v\t\x03\t\x03\t\x03\n\x03\n\x03\n\x03\n\x07\nu\n\n" +
		"\f\n\x0E\nx\v\n\x05\nz\n\n\x03\n\x03\n\x03\v\x03\v\x03\f\x03\f\x03\f\x03" +
		"\r\x03\r\x03\r\x05\r\x86\n\r\x03\x0E\x03\x0E\x03\x0E\x03\x0E\x03\x0E\x03" +
		"\x0E\x03\x0E\x05\x0E\x8F\n\x0E\x03\x0F\x03\x0F\x07\x0F\x93\n\x0F\f\x0F" +
		"\x0E\x0F\x96\v\x0F\x03\x0F\x03\x0F\x03\x10\x03\x10\x03\x11\x03\x11\x03" +
		"\x11\x03\x11\x03\x12\x03\x12\x05\x12\xA2\n\x12\x03\x13\x03\x13\x05\x13" +
		"\xA6\n\x13\x03\x14\x03\x14\x03\x14\x03\x15\x03\x15\x03\x15\x03\x16\x03" +
		"\x16\x03\x17\x07\x17\xB1\n\x17\f\x17\x0E\x17\xB4\v\x17\x03\x17\x03\x17" +
		"\x03\x17\x04R^\x02\x02\x18\x02\x02\x04\x02\x06\x02\b\x02\n\x02\f\x02\x0E" +
		"\x02\x10\x02\x12\x02\x14\x02\x16\x02\x18\x02\x1A\x02\x1C\x02\x1E\x02 " +
		"\x02\"\x02$\x02&\x02(\x02*\x02,\x02\x02\b\x03\x02\x04\x05\x03\x02$%\x03" +
		"\x02+-\x04\x02\b\b\v\v\x03\x02\x11\x12\x03\x02\')\x02\xC0\x02.\x03\x02" +
		"\x02\x02\x040\x03\x02\x02\x02\x06>\x03\x02\x02\x02\bE\x03\x02\x02\x02" +
		"\nG\x03\x02\x02\x02\fK\x03\x02\x02\x02\x0EW\x03\x02\x02\x02\x10c\x03\x02" +
		"\x02\x02\x12p\x03\x02\x02\x02\x14}\x03\x02\x02\x02\x16\x7F\x03\x02\x02" +
		"\x02\x18\x85\x03\x02\x02\x02\x1A\x8E\x03\x02\x02\x02\x1C\x90\x03\x02\x02" +
		"\x02\x1E\x99\x03\x02\x02\x02 \x9B\x03\x02\x02\x02\"\xA1\x03\x02\x02\x02" +
		"$\xA5\x03\x02\x02\x02&\xA7\x03\x02\x02\x02(\xAA\x03\x02\x02\x02*\xAD\x03" +
		"\x02\x02\x02,\xB2\x03\x02\x02\x02./\x07\x03\x02\x02/\x03\x03\x02\x02\x02" +
		"09\x05\x02\x02\x0212\x05\x06\x04\x0223\x05\b\x05\x0235\x03\x02\x02\x02" +
		"41\x03\x02\x02\x0256\x03\x02\x02\x0264\x03\x02\x02\x0267\x03\x02\x02\x02" +
		"7:\x03\x02\x02\x028:\x05\x06\x04\x0294\x03\x02\x02\x0298\x03\x02\x02\x02" +
		"9:\x03\x02\x02\x02:<\x03\x02\x02\x02;=\x05\x06\x04\x02<;\x03\x02\x02\x02" +
		"<=\x03\x02\x02\x02=\x05\x03\x02\x02\x02>?\t\x02\x02\x02?\x07\x03\x02\x02" +
		"\x02@F\x05\f\x07\x02AF\x05\x0E\b\x02BF\x05\x1E\x10\x02CF\x05\n\x06\x02" +
		"DF\x05\x1C\x0F\x02E@\x03\x02\x02\x02EA\x03\x02\x02\x02EB\x03\x02\x02\x02" +
		"EC\x03\x02\x02\x02ED\x03\x02\x02\x02F\t\x03\x02\x02\x02GH\x07\x06\x02" +
		"\x02HI\x07\x07\x02\x02IJ\x05\x1A\x0E\x02J\v\x03\x02\x02\x02KL\x07\x0F" +
		"\x02\x02LU\x07\r\x02\x02MR\x05\x18\r\x02NO\x07\n\x02\x02OQ\x05\x18\r\x02" +
		"PN\x03\x02\x02\x02QT\x03\x02\x02\x02RS\x03\x02\x02\x02RP\x03\x02\x02\x02" +
		"SV\x03\x02\x02\x02TR\x03\x02\x02\x02UM\x03\x02\x02\x02UV\x03\x02\x02\x02" +
		"V\r\x03\x02\x02\x02WX\x07\v\x02\x02Xa\x07\r\x02\x02Y^\x05\x18\r\x02Z[" +
		"\x07\n\x02\x02[]\x05\x18\r\x02\\Z\x03\x02\x02\x02]`\x03\x02\x02\x02^_" +
		"\x03\x02\x02\x02^\\\x03\x02\x02\x02_b\x03\x02\x02\x02`^\x03\x02\x02\x02" +
		"aY\x03\x02\x02\x02ab\x03\x02\x02\x02b\x0F\x03\x02\x02\x02ck\x07\x15\x02" +
		"\x02dj\x07\x1D\x02\x02ej\x07\x1E\x02\x02fj\x07\x1F\x02\x02gj\x05\x16\f" +
		"\x02hj\x07!\x02\x02id\x03\x02\x02\x02ie\x03\x02\x02\x02if\x03\x02\x02" +
		"\x02ig\x03\x02\x02\x02ih\x03\x02\x02\x02jm\x03\x02\x02\x02ki\x03\x02\x02" +
		"\x02kl\x03\x02\x02\x02ln\x03\x02\x02\x02mk\x03\x02\x02\x02no\x07\"\x02" +
		"\x02o\x11\x03\x02\x02\x02py\x07\x14\x02\x02qv\x05\x14\v\x02rs\x07#\x02" +
		"\x02su\x05\x14\v\x02tr\x03\x02\x02\x02ux\x03\x02\x02\x02vt\x03\x02\x02" +
		"\x02vw\x03\x02\x02\x02wz\x03\x02\x02\x02xv\x03\x02\x02\x02yq\x03\x02\x02" +
		"\x02yz\x03\x02\x02\x02z{\x03\x02\x02\x02{|\x07&\x02\x02|\x13\x03\x02\x02" +
		"\x02}~\t\x03\x02\x02~\x15\x03\x02\x02\x02\x7F\x80\x07 \x02\x02\x80\x81" +
		"\x05,\x17\x02\x81\x17\x03\x02\x02\x02\x82\x86\x05\"\x12\x02\x83\x86\x05" +
		"$\x13\x02\x84\x86\x05\x1A\x0E\x02\x85\x82\x03\x02\x02\x02\x85\x83\x03" +
		"\x02\x02\x02\x85\x84\x03\x02\x02\x02\x86\x19\x03\x02\x02\x02\x87\x8F\x05" +
		"\x12\n\x02\x88\x8F\x05\x10\t\x02\x89\x8F\x05*\x16\x02\x8A\x8F\x07\x0F" +
		"\x02\x02\x8B\x8F\x07\x10\x02\x02\x8C\x8F\x07\b\x02\x02\x8D\x8F\x07\x13" +
		"\x02\x02\x8E\x87\x03\x02\x02\x02\x8E\x88\x03\x02\x02\x02\x8E\x89\x03\x02" +
		"\x02\x02\x8E\x8A\x03\x02\x02\x02\x8E\x8B\x03\x02\x02\x02\x8E\x8C\x03\x02" +
		"\x02\x02\x8E\x8D\x03\x02\x02\x02\x8F\x1B\x03\x02\x02\x02\x90\x94\x07\x16" +
		"\x02\x02\x91\x93\t\x04\x02\x02\x92\x91\x03\x02\x02\x02\x93\x96\x03\x02" +
		"\x02\x02\x94\x92\x03\x02\x02\x02\x94\x95\x03\x02\x02\x02\x95\x97\x03\x02" +
		"\x02\x02\x96\x94\x03\x02\x02\x02\x97\x98\x07.\x02\x02\x98\x1D\x03\x02" +
		"\x02\x02\x99\x9A\x05 \x11\x02\x9A\x1F\x03\x02\x02\x02\x9B\x9C\t\x05\x02" +
		"\x02\x9C\x9D\x07\x0E\x02\x02\x9D\x9E\x05\x1A\x0E\x02\x9E!\x03\x02\x02" +
		"\x02\x9F\xA2\x07\x19\x02\x02\xA0\xA2\x05&\x14\x02\xA1\x9F\x03\x02\x02" +
		"\x02\xA1\xA0\x03\x02\x02\x02\xA2#\x03\x02\x02\x02\xA3\xA6\x07\x1A\x02" +
		"\x02\xA4\xA6\x05(\x15\x02\xA5\xA3\x03\x02\x02\x02\xA5\xA4\x03\x02\x02" +
		"\x02\xA6%\x03\x02\x02\x02\xA7\xA8\x07\x1B\x02\x02\xA8\xA9\x05,\x17\x02" +
		"\xA9\'\x03\x02\x02\x02\xAA\xAB\x07\x1C\x02\x02\xAB\xAC\x05,\x17\x02\xAC" +
		")\x03\x02\x02\x02\xAD\xAE\t\x06\x02\x02\xAE+\x03\x02\x02\x02\xAF\xB1\t" +
		"\x07\x02\x02\xB0\xAF\x03\x02\x02\x02\xB1\xB4\x03\x02\x02\x02\xB2\xB0\x03" +
		"\x02\x02\x02\xB2\xB3\x03\x02\x02\x02\xB3\xB5\x03\x02\x02\x02\xB4\xB2\x03" +
		"\x02\x02\x02\xB5\xB6\x07*\x02\x02\xB6-\x03\x02\x02\x02\x1469<ERU^aikv" +
		"y\x85\x8E\x94\xA1\xA5\xB2";
	public static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!OpenSpiralParser.__ATN) {
			OpenSpiralParser.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(OpenSpiralParser._serializedATN));
		}

		return OpenSpiralParser.__ATN;
	}

}

export class HeaderDeclarationContext extends ParserRuleContext {
	public HEADER_DECLARATION(): TerminalNode { return this.getToken(OpenSpiralParser.HEADER_DECLARATION, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_headerDeclaration; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterHeaderDeclaration) {
			listener.enterHeaderDeclaration(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitHeaderDeclaration) {
			listener.exitHeaderDeclaration(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitHeaderDeclaration) {
			return visitor.visitHeaderDeclaration(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ScriptContext extends ParserRuleContext {
	public headerDeclaration(): HeaderDeclarationContext {
		return this.getRuleContext(0, HeaderDeclarationContext);
	}
	public lineSeparator(): LineSeparatorContext[];
	public lineSeparator(i: number): LineSeparatorContext;
	public lineSeparator(i?: number): LineSeparatorContext | LineSeparatorContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LineSeparatorContext);
		} else {
			return this.getRuleContext(i, LineSeparatorContext);
		}
	}
	public scriptLine(): ScriptLineContext[];
	public scriptLine(i: number): ScriptLineContext;
	public scriptLine(i?: number): ScriptLineContext | ScriptLineContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ScriptLineContext);
		} else {
			return this.getRuleContext(i, ScriptLineContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_script; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterScript) {
			listener.enterScript(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitScript) {
			listener.exitScript(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitScript) {
			return visitor.visitScript(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LineSeparatorContext extends ParserRuleContext {
	public SEMICOLON_SEPARATOR(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.SEMICOLON_SEPARATOR, 0); }
	public NL_SEPARATOR(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.NL_SEPARATOR, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_lineSeparator; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterLineSeparator) {
			listener.enterLineSeparator(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitLineSeparator) {
			listener.exitLineSeparator(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitLineSeparator) {
			return visitor.visitLineSeparator(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ScriptLineContext extends ParserRuleContext {
	public basicDrill(): BasicDrillContext | undefined {
		return this.tryGetRuleContext(0, BasicDrillContext);
	}
	public basicDrillNamed(): BasicDrillNamedContext | undefined {
		return this.tryGetRuleContext(0, BasicDrillNamedContext);
	}
	public complexDrills(): ComplexDrillsContext | undefined {
		return this.tryGetRuleContext(0, ComplexDrillsContext);
	}
	public metaVariableAssignment(): MetaVariableAssignmentContext | undefined {
		return this.tryGetRuleContext(0, MetaVariableAssignmentContext);
	}
	public actionDeclaration(): ActionDeclarationContext | undefined {
		return this.tryGetRuleContext(0, ActionDeclarationContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_scriptLine; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterScriptLine) {
			listener.enterScriptLine(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitScriptLine) {
			listener.exitScriptLine(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitScriptLine) {
			return visitor.visitScriptLine(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class MetaVariableAssignmentContext extends ParserRuleContext {
	public ASSIGN_VARIABLE_NAME(): TerminalNode { return this.getToken(OpenSpiralParser.ASSIGN_VARIABLE_NAME, 0); }
	public VARIABLE_ASSIGNMENT(): TerminalNode { return this.getToken(OpenSpiralParser.VARIABLE_ASSIGNMENT, 0); }
	public variableValue(): VariableValueContext {
		return this.getRuleContext(0, VariableValueContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_metaVariableAssignment; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterMetaVariableAssignment) {
			listener.enterMetaVariableAssignment(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitMetaVariableAssignment) {
			listener.exitMetaVariableAssignment(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitMetaVariableAssignment) {
			return visitor.visitMetaVariableAssignment(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class BasicDrillContext extends ParserRuleContext {
	public INTEGER(): TerminalNode { return this.getToken(OpenSpiralParser.INTEGER, 0); }
	public BASIC_DRILL_SEPARATOR(): TerminalNode { return this.getToken(OpenSpiralParser.BASIC_DRILL_SEPARATOR, 0); }
	public basicDrillValue(): BasicDrillValueContext[];
	public basicDrillValue(i: number): BasicDrillValueContext;
	public basicDrillValue(i?: number): BasicDrillValueContext | BasicDrillValueContext[] {
		if (i === undefined) {
			return this.getRuleContexts(BasicDrillValueContext);
		} else {
			return this.getRuleContext(i, BasicDrillValueContext);
		}
	}
	public VALUE_SEPARATOR(): TerminalNode[];
	public VALUE_SEPARATOR(i: number): TerminalNode;
	public VALUE_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.VALUE_SEPARATOR);
		} else {
			return this.getToken(OpenSpiralParser.VALUE_SEPARATOR, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_basicDrill; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterBasicDrill) {
			listener.enterBasicDrill(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitBasicDrill) {
			listener.exitBasicDrill(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitBasicDrill) {
			return visitor.visitBasicDrill(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class BasicDrillNamedContext extends ParserRuleContext {
	public NAME_IDENTIFIER(): TerminalNode { return this.getToken(OpenSpiralParser.NAME_IDENTIFIER, 0); }
	public BASIC_DRILL_SEPARATOR(): TerminalNode { return this.getToken(OpenSpiralParser.BASIC_DRILL_SEPARATOR, 0); }
	public basicDrillValue(): BasicDrillValueContext[];
	public basicDrillValue(i: number): BasicDrillValueContext;
	public basicDrillValue(i?: number): BasicDrillValueContext | BasicDrillValueContext[] {
		if (i === undefined) {
			return this.getRuleContexts(BasicDrillValueContext);
		} else {
			return this.getRuleContext(i, BasicDrillValueContext);
		}
	}
	public VALUE_SEPARATOR(): TerminalNode[];
	public VALUE_SEPARATOR(i: number): TerminalNode;
	public VALUE_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.VALUE_SEPARATOR);
		} else {
			return this.getToken(OpenSpiralParser.VALUE_SEPARATOR, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_basicDrillNamed; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterBasicDrillNamed) {
			listener.enterBasicDrillNamed(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitBasicDrillNamed) {
			listener.exitBasicDrillNamed(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitBasicDrillNamed) {
			return visitor.visitBasicDrillNamed(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QuotedStringContext extends ParserRuleContext {
	public BEGIN_QUOTED_STRING(): TerminalNode { return this.getToken(OpenSpiralParser.BEGIN_QUOTED_STRING, 0); }
	public END_QUOTED_STRING(): TerminalNode { return this.getToken(OpenSpiralParser.END_QUOTED_STRING, 0); }
	public ESCAPES(): TerminalNode[];
	public ESCAPES(i: number): TerminalNode;
	public ESCAPES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.ESCAPES);
		} else {
			return this.getToken(OpenSpiralParser.ESCAPES, i);
		}
	}
	public STRING_CHARACTERS(): TerminalNode[];
	public STRING_CHARACTERS(i: number): TerminalNode;
	public STRING_CHARACTERS(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.STRING_CHARACTERS);
		} else {
			return this.getToken(OpenSpiralParser.STRING_CHARACTERS, i);
		}
	}
	public QUOTED_STRING_VARIABLE_REFERENCE(): TerminalNode[];
	public QUOTED_STRING_VARIABLE_REFERENCE(i: number): TerminalNode;
	public QUOTED_STRING_VARIABLE_REFERENCE(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.QUOTED_STRING_VARIABLE_REFERENCE);
		} else {
			return this.getToken(OpenSpiralParser.QUOTED_STRING_VARIABLE_REFERENCE, i);
		}
	}
	public longColourReference(): LongColourReferenceContext[];
	public longColourReference(i: number): LongColourReferenceContext;
	public longColourReference(i?: number): LongColourReferenceContext | LongColourReferenceContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LongColourReferenceContext);
		} else {
			return this.getRuleContext(i, LongColourReferenceContext);
		}
	}
	public QUOTED_COLOUR_CODE(): TerminalNode[];
	public QUOTED_COLOUR_CODE(i: number): TerminalNode;
	public QUOTED_COLOUR_CODE(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.QUOTED_COLOUR_CODE);
		} else {
			return this.getToken(OpenSpiralParser.QUOTED_COLOUR_CODE, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_quotedString; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterQuotedString) {
			listener.enterQuotedString(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitQuotedString) {
			listener.exitQuotedString(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitQuotedString) {
			return visitor.visitQuotedString(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LocalisedStringContext extends ParserRuleContext {
	public BEGIN_LOCALE_STRING(): TerminalNode { return this.getToken(OpenSpiralParser.BEGIN_LOCALE_STRING, 0); }
	public END_LOCALE_STRING(): TerminalNode { return this.getToken(OpenSpiralParser.END_LOCALE_STRING, 0); }
	public localisedComponent(): LocalisedComponentContext[];
	public localisedComponent(i: number): LocalisedComponentContext;
	public localisedComponent(i?: number): LocalisedComponentContext | LocalisedComponentContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LocalisedComponentContext);
		} else {
			return this.getRuleContext(i, LocalisedComponentContext);
		}
	}
	public LOCALE_NAME_SEPARATOR(): TerminalNode[];
	public LOCALE_NAME_SEPARATOR(i: number): TerminalNode;
	public LOCALE_NAME_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.LOCALE_NAME_SEPARATOR);
		} else {
			return this.getToken(OpenSpiralParser.LOCALE_NAME_SEPARATOR, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_localisedString; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterLocalisedString) {
			listener.enterLocalisedString(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitLocalisedString) {
			listener.exitLocalisedString(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitLocalisedString) {
			return visitor.visitLocalisedString(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LocalisedComponentContext extends ParserRuleContext {
	public LOCALE_NAME_IDENTIFIER(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.LOCALE_NAME_IDENTIFIER, 0); }
	public LOCALE_VARIABLE_REFERENCE(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.LOCALE_VARIABLE_REFERENCE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_localisedComponent; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterLocalisedComponent) {
			listener.enterLocalisedComponent(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitLocalisedComponent) {
			listener.exitLocalisedComponent(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitLocalisedComponent) {
			return visitor.visitLocalisedComponent(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LongColourReferenceContext extends ParserRuleContext {
	public BEGIN_LONG_QUOTED_COLOUR_CODE(): TerminalNode { return this.getToken(OpenSpiralParser.BEGIN_LONG_QUOTED_COLOUR_CODE, 0); }
	public longReference(): LongReferenceContext {
		return this.getRuleContext(0, LongReferenceContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_longColourReference; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterLongColourReference) {
			listener.enterLongColourReference(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitLongColourReference) {
			listener.exitLongColourReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitLongColourReference) {
			return visitor.visitLongColourReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class BasicDrillValueContext extends ParserRuleContext {
	public wrdLabelReference(): WrdLabelReferenceContext | undefined {
		return this.tryGetRuleContext(0, WrdLabelReferenceContext);
	}
	public wrdParameterReference(): WrdParameterReferenceContext | undefined {
		return this.tryGetRuleContext(0, WrdParameterReferenceContext);
	}
	public variableValue(): VariableValueContext | undefined {
		return this.tryGetRuleContext(0, VariableValueContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_basicDrillValue; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterBasicDrillValue) {
			listener.enterBasicDrillValue(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitBasicDrillValue) {
			listener.exitBasicDrillValue(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitBasicDrillValue) {
			return visitor.visitBasicDrillValue(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class VariableValueContext extends ParserRuleContext {
	public localisedString(): LocalisedStringContext | undefined {
		return this.tryGetRuleContext(0, LocalisedStringContext);
	}
	public quotedString(): QuotedStringContext | undefined {
		return this.tryGetRuleContext(0, QuotedStringContext);
	}
	public booleanRule(): BooleanRuleContext | undefined {
		return this.tryGetRuleContext(0, BooleanRuleContext);
	}
	public INTEGER(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.INTEGER, 0); }
	public DECIMAL_NUMBER(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.DECIMAL_NUMBER, 0); }
	public VARIABLE_REFERENCE(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.VARIABLE_REFERENCE, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.NULL, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_variableValue; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterVariableValue) {
			listener.enterVariableValue(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitVariableValue) {
			listener.exitVariableValue(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitVariableValue) {
			return visitor.visitVariableValue(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ActionDeclarationContext extends ParserRuleContext {
	public BEGIN_ACTION(): TerminalNode { return this.getToken(OpenSpiralParser.BEGIN_ACTION, 0); }
	public END_ACTION(): TerminalNode { return this.getToken(OpenSpiralParser.END_ACTION, 0); }
	public ACTION_ESCAPES(): TerminalNode[];
	public ACTION_ESCAPES(i: number): TerminalNode;
	public ACTION_ESCAPES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.ACTION_ESCAPES);
		} else {
			return this.getToken(OpenSpiralParser.ACTION_ESCAPES, i);
		}
	}
	public ACTION_CHARACTERS(): TerminalNode[];
	public ACTION_CHARACTERS(i: number): TerminalNode;
	public ACTION_CHARACTERS(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.ACTION_CHARACTERS);
		} else {
			return this.getToken(OpenSpiralParser.ACTION_CHARACTERS, i);
		}
	}
	public ACTION_VARIABLE_REFERENCE(): TerminalNode[];
	public ACTION_VARIABLE_REFERENCE(i: number): TerminalNode;
	public ACTION_VARIABLE_REFERENCE(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.ACTION_VARIABLE_REFERENCE);
		} else {
			return this.getToken(OpenSpiralParser.ACTION_VARIABLE_REFERENCE, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_actionDeclaration; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterActionDeclaration) {
			listener.enterActionDeclaration(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitActionDeclaration) {
			listener.exitActionDeclaration(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitActionDeclaration) {
			return visitor.visitActionDeclaration(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ComplexDrillsContext extends ParserRuleContext {
	public dialogueDrill(): DialogueDrillContext {
		return this.getRuleContext(0, DialogueDrillContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_complexDrills; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterComplexDrills) {
			listener.enterComplexDrills(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitComplexDrills) {
			listener.exitComplexDrills(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitComplexDrills) {
			return visitor.visitComplexDrills(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class DialogueDrillContext extends ParserRuleContext {
	public DIALOGUE_SEPARATOR(): TerminalNode { return this.getToken(OpenSpiralParser.DIALOGUE_SEPARATOR, 0); }
	public variableValue(): VariableValueContext {
		return this.getRuleContext(0, VariableValueContext);
	}
	public VARIABLE_REFERENCE(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.VARIABLE_REFERENCE, 0); }
	public NAME_IDENTIFIER(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.NAME_IDENTIFIER, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_dialogueDrill; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterDialogueDrill) {
			listener.enterDialogueDrill(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitDialogueDrill) {
			listener.exitDialogueDrill(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitDialogueDrill) {
			return visitor.visitDialogueDrill(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WrdLabelReferenceContext extends ParserRuleContext {
	public WRD_SHORT_LABEL_REFERENCE(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.WRD_SHORT_LABEL_REFERENCE, 0); }
	public wrdLongLabelReference(): WrdLongLabelReferenceContext | undefined {
		return this.tryGetRuleContext(0, WrdLongLabelReferenceContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_wrdLabelReference; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterWrdLabelReference) {
			listener.enterWrdLabelReference(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitWrdLabelReference) {
			listener.exitWrdLabelReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitWrdLabelReference) {
			return visitor.visitWrdLabelReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WrdParameterReferenceContext extends ParserRuleContext {
	public WRD_SHORT_PARAMETER_REFERENCE(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.WRD_SHORT_PARAMETER_REFERENCE, 0); }
	public wrdLongParameterReference(): WrdLongParameterReferenceContext | undefined {
		return this.tryGetRuleContext(0, WrdLongParameterReferenceContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_wrdParameterReference; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterWrdParameterReference) {
			listener.enterWrdParameterReference(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitWrdParameterReference) {
			listener.exitWrdParameterReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitWrdParameterReference) {
			return visitor.visitWrdParameterReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WrdLongLabelReferenceContext extends ParserRuleContext {
	public WRD_START_LONG_LABEL_REFERENCE(): TerminalNode { return this.getToken(OpenSpiralParser.WRD_START_LONG_LABEL_REFERENCE, 0); }
	public longReference(): LongReferenceContext {
		return this.getRuleContext(0, LongReferenceContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_wrdLongLabelReference; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterWrdLongLabelReference) {
			listener.enterWrdLongLabelReference(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitWrdLongLabelReference) {
			listener.exitWrdLongLabelReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitWrdLongLabelReference) {
			return visitor.visitWrdLongLabelReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WrdLongParameterReferenceContext extends ParserRuleContext {
	public WRD_START_LONG_PARAMETER_REFERENCE(): TerminalNode { return this.getToken(OpenSpiralParser.WRD_START_LONG_PARAMETER_REFERENCE, 0); }
	public longReference(): LongReferenceContext {
		return this.getRuleContext(0, LongReferenceContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_wrdLongParameterReference; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterWrdLongParameterReference) {
			listener.enterWrdLongParameterReference(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitWrdLongParameterReference) {
			listener.exitWrdLongParameterReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitWrdLongParameterReference) {
			return visitor.visitWrdLongParameterReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class BooleanRuleContext extends ParserRuleContext {
	public TRUE(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.TRUE, 0); }
	public FALSE(): TerminalNode | undefined { return this.tryGetToken(OpenSpiralParser.FALSE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_booleanRule; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterBooleanRule) {
			listener.enterBooleanRule(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitBooleanRule) {
			listener.exitBooleanRule(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitBooleanRule) {
			return visitor.visitBooleanRule(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LongReferenceContext extends ParserRuleContext {
	public END_LONG_REFERENCE(): TerminalNode { return this.getToken(OpenSpiralParser.END_LONG_REFERENCE, 0); }
	public LONG_REF_ESCAPES(): TerminalNode[];
	public LONG_REF_ESCAPES(i: number): TerminalNode;
	public LONG_REF_ESCAPES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.LONG_REF_ESCAPES);
		} else {
			return this.getToken(OpenSpiralParser.LONG_REF_ESCAPES, i);
		}
	}
	public LONG_REF_CHARACTERS(): TerminalNode[];
	public LONG_REF_CHARACTERS(i: number): TerminalNode;
	public LONG_REF_CHARACTERS(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.LONG_REF_CHARACTERS);
		} else {
			return this.getToken(OpenSpiralParser.LONG_REF_CHARACTERS, i);
		}
	}
	public LONG_REF_VARIABLE_REFERENCE(): TerminalNode[];
	public LONG_REF_VARIABLE_REFERENCE(i: number): TerminalNode;
	public LONG_REF_VARIABLE_REFERENCE(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(OpenSpiralParser.LONG_REF_VARIABLE_REFERENCE);
		} else {
			return this.getToken(OpenSpiralParser.LONG_REF_VARIABLE_REFERENCE, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return OpenSpiralParser.RULE_longReference; }
	// @Override
	public enterRule(listener: OpenSpiralParserListener): void {
		if (listener.enterLongReference) {
			listener.enterLongReference(this);
		}
	}
	// @Override
	public exitRule(listener: OpenSpiralParserListener): void {
		if (listener.exitLongReference) {
			listener.exitLongReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: OpenSpiralParserVisitor<Result>): Result {
		if (visitor.visitLongReference) {
			return visitor.visitLongReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


