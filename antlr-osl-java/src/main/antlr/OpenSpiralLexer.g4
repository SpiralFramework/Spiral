lexer grammar OpenSpiralLexer;
import LibLexer, OSLBacktickLexer, OSLCommonLexer, OSLWordScriptLexer;

HEADER_DECLARATION: O S L INLINE_WHITESPACE S C R I P T (INLINE_WHITESPACE SEMANTIC_VERSION)?;

SEMICOLON_SEPARATOR: (';' NEW_LINE? OPT_WHITESPACE);
NL_SEPARATOR: (NEW_LINE OPT_WHITESPACE);

ASSIGN_VARIABLE_NAME: (V A L | V A R) INLINE_WHITESPACE VARIABLE_NAME_IDENTIFIER OPT_INLINE_WHITESPACE;
VARIABLE_ASSIGNMENT: OPT_INLINE_WHITESPACE '=' OPT_INLINE_WHITESPACE;

VARIABLE_REFERENCE: '$' VARIABLE_NAME_IDENTIFIER;
SEMANTIC_VERSION: V OPT_INLINE_WHITESPACE INTEGER (OPT_INLINE_WHITESPACE '.' OPT_INLINE_WHITESPACE INTEGER (OPT_INLINE_WHITESPACE '.' OPT_INLINE_WHITESPACE INTEGER)?)?;

VALUE_SEPARATOR: OPT_INLINE_WHITESPACE ',' OPT_INLINE_WHITESPACE;

NAME_IDENTIFIER: (BACKTICK | NAME_START_IDENTIFIER) (BACKTICK | NAME_END_IDENTIFIER)*;

DIALOG_DRILL_NAME: D I A L O G INLINE_WHITESPACE F O R INLINE_WHITESPACE;

BASIC_DRILL_SEPARATOR: OPT_INLINE_WHITESPACE '|' OPT_INLINE_WHITESPACE;
DIALOGUE_SEPARATOR: OPT_INLINE_WHITESPACE ':' OPT_INLINE_WHITESPACE;

IF_CHECK: I F OPT_INLINE_WHITESPACE '(' -> pushMode(IfCheckMode);
CHECK_FLAG: C H E C K (INLINE_WHITESPACE | '_')? F L A G OPT_INLINE_WHITESPACE '(' -> pushMode(IfCheckMode);
CHECK_CHARACTER: C H E C K (INLINE_WHITESPACE | '_')? C H A R (A C T E R)? OPT_INLINE_WHITESPACE '(' -> pushMode(IfCheckMode);
CHECK_OBJECT: C H E C K (INLINE_WHITESPACE | '_')? O B J (E C T)? OPT_INLINE_WHITESPACE '(' -> pushMode(IfCheckMode);
BEGIN_FUNC_CALL: '(' -> pushMode(FunctionCallMode);

ELIF: '}' OPT_INLINE_WHITESPACE E L S E OPT_INLINE_WHITESPACE;
ELSE: '}' OPT_INLINE_WHITESPACE E L S E OPT_INLINE_WHITESPACE '{';

CLOSE_SCOPE: '}';

fragment NAME_START_IDENTIFIER: [a-zA-Z_?];
fragment NAME_END_IDENTIFIER: [a-zA-Z0-9_\- ?]+;
fragment LOCALISED_NAME_IDENTIFIER: VARIABLE_NAME_IDENTIFIER ('.' VARIABLE_NAME_IDENTIFIER)*;

mode FunctionCallMode;

FUNC_CALL_PARAMETER_NAME: VARIABLE_NAME_IDENTIFIER OPT_INLINE_WHITESPACE '=' OPT_INLINE_WHITESPACE;
FUNC_CALL_INTEGER: INTEGER;
FUNC_CALL_DECIMAL_NUMBER: DECIMAL_NUMBER;
FUNC_CALL_VARIABLE_REFERENCE: '$' VARIABLE_NAME_IDENTIFIER;
FUNC_CALL_NULL: NULL;
FUNC_CALL_BEGIN_LOCALE_STRING: '"locale.' -> pushMode(LocaleStringMode);
FUNC_CALL_BEGIN_QUOTED_STRING: '"' -> pushMode(QuotedStringMode);
FUNC_CALL_NAME_IDENTIFIER: NAME_IDENTIFIER;
FUNC_CALL_RECURSIVE: '(' -> pushMode(FunctionCallMode);
FUNC_TRUE: TRUE;
FUNC_FALSE: FALSE;
FUNC_CALL_PARAM_SEPARATOR: OPT_INLINE_WHITESPACE ',' OPT_INLINE_WHITESPACE;

END_FUNC_CALL: ')' -> popMode;

mode IfCheckMode;

IF_CHECK_EQUALITY_NOT_EQUAL: OPT_INLINE_WHITESPACE '!=' OPT_INLINE_WHITESPACE;
IF_CHECK_EQUALITY_EQUAL: OPT_INLINE_WHITESPACE '==' OPT_INLINE_WHITESPACE;
IF_CHECK_EQUALITY_LESS_THAN_EQUAL_TO: OPT_INLINE_WHITESPACE '<=' OPT_INLINE_WHITESPACE;
IF_CHECK_EQUALITY_GREATER_THAN_EQUAL_TO: OPT_INLINE_WHITESPACE '>=' OPT_INLINE_WHITESPACE;
IF_CHECK_EQUALITY_LESS_THAN: OPT_INLINE_WHITESPACE '<' OPT_INLINE_WHITESPACE;
IF_CHECK_EQUALITY_GREATER_THAN: OPT_INLINE_WHITESPACE '>' OPT_INLINE_WHITESPACE;

IF_CHECK_LOGICAL_AND: OPT_INLINE_WHITESPACE '&&' OPT_INLINE_WHITESPACE;
IF_CHECK_LOGICAL_OR: OPT_INLINE_WHITESPACE '||' OPT_INLINE_WHITESPACE;

IF_CHECK_INTEGER: INTEGER;
IF_CHECK_DECIMAL_NUMBER: DECIMAL_NUMBER;
IF_CHECK_VARIABLE_REFERENCE: '$' VARIABLE_NAME_IDENTIFIER;
IF_CHECK_NULL: NULL;
IF_CHECK_BEGIN_LOCALE_STRING: '"locale.' -> pushMode(LocaleStringMode);
IF_CHECK_BEGIN_QUOTED_STRING: '"' -> pushMode(QuotedStringMode);
IF_CHECK_NAME_IDENTIFIER: NAME_IDENTIFIER;
IF_CHECK_FUNC_CALL: '(' -> pushMode(FunctionCallMode);
IF_CHECK_TRUE: TRUE;
IF_CHECK_FALSE: FALSE;
IF_CHECK_PARAM_SEPARATOR: OPT_INLINE_WHITESPACE ',' OPT_INLINE_WHITESPACE;

END_IF_CHECK: ')' OPT_INLINE_WHITESPACE '{' -> popMode;