lexer grammar PipelineLexer;
tokens { IDENTIFIER }

SINGLE_LINE_COMMENT: '//' ~[\n]+ NEW_LINE* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' NEW_LINE* -> skip ; // .*? matches anything until the first */

ASSIGN_VARIABLE: (V A L | V A R | S E T) INLINE_WHITESPACE_CHARACTERS -> pushMode(Identifier);
VARIABLE_ASSIGNMENT: '=';
VARIABLE_REFERENCE: '$' -> pushMode(Identifier);
WRAPPED_SCRIPT_CALL: '$(' -> pushMode(ScriptCall), pushMode(Identifier);

INTEGER: '-'? ('0b' BINARY_DIGITS+) | ('0o' OCTAL_DIGITS+) | ('0x' HEX_DIGITS+) | '0d'? DECIMAL_DIGITS+;
DECIMAL_NUMBER: '-'? ('0' | ([1-9] DECIMAL_DIGITS*)) ('.' DECIMAL_DIGITS+)? ([eE] [+\-]? DECIMAL_DIGITS+)?;

TRUE: T R U E;
FALSE: F A L S E;
NULL: N U L L;
GLOBAL: G L O B A L;

START_EXPRESSION: '(' -> pushMode(ExpressionMode);

BEGIN_QUOTED_STRING: '"' -> pushMode(QuotedStringMode);

FUNCTION_DECLARATION: (D E F | F N | F U N) INLINE_WHITESPACE_CHARACTERS+ -> pushMode(FunctionDeclaration), pushMode(Identifier);

PIPELINE_IDENTIFIER: NAME_IDENTIFIER -> type(IDENTIFIER), pushMode(IdentifierEnd);

END_SCOPE: '}';

SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;
SEMICOLON_SEPARATOR: (';' NEW_LINE? OPT_WHITESPACE);
NL_SEPARATOR: (NEW_LINE OPT_WHITESPACE);

fragment BINARY_DIGITS: [0-1];
fragment OCTAL_DIGITS: [0-7];
fragment DECIMAL_DIGITS: [0-9];
fragment HEX_DIGITS: [0-9a-fA-F];

fragment INLINE_WHITESPACE_CHARACTERS: [ \t];
fragment NAME_IDENTIFIER: NAME_START_IDENTIFIER NAME_END_IDENTIFIER*;
fragment NAME_START_IDENTIFIER: [a-zA-Z_?];
fragment NAME_END_IDENTIFIER: [a-zA-Z0-9_\-?];

fragment NEW_LINE: '\r'? '\n';

fragment OPT_WHITESPACE: [ \t\r\n]*;
fragment OPT_INLINE_WHITESPACE: INLINE_WHITESPACE_CHARACTERS*;

fragment A : [aA]; // match either an 'a' or 'A'
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

mode QuotedStringMode;

ESCAPES: '\\' ('u' HEX_DIGITS HEX_DIGITS HEX_DIGITS HEX_DIGITS | ESCAPE_CHARACTERS);
STRING_CHARACTERS: ~["\\$& \t]+;
QUOTED_STRING_VARIABLE_REFERENCE: '$' -> type(VARIABLE_REFERENCE), pushMode(Identifier);

QUOTED_STRING_LINE_BREAK: INLINE_WHITESPACE_CHARACTERS* '&br' INLINE_WHITESPACE_CHARACTERS*;
QUOTED_STRING_LINE_BREAK_NO_SPACE: '&{br}';

STRING_WHITESPACE: INLINE_WHITESPACE_CHARACTERS+;

fragment COLOUR_CODE_CHARACTERS: [a-zA-Z0-9_];
fragment ESCAPE_CHARACTERS: ["\\/bfnrt$&];

END_QUOTED_STRING: '"' -> popMode;

mode Identifier;
MODE_IDENTIFIER: NAME_IDENTIFIER -> type(IDENTIFIER), popMode;

mode IdentifierEnd;
BEGIN_FUNCTION_CALL: '(' -> mode(FunctionCall);
SET_PARAMETER: OPT_INLINE_WHITESPACE '=' OPT_INLINE_WHITESPACE -> popMode;
BEGIN_SCRIPT_CALL: INLINE_WHITESPACE_CHARACTERS+ -> mode(ScriptCall);
BEGIN_SCRIPT_CALL_EMPTY: (SEMICOLON_SEPARATOR | NL_SEPARATOR | EOF) -> popMode;

mode FunctionCall;

FUNC_CALL_INTEGER: INTEGER -> type(INTEGER);
FUNC_CALL_DECIMAL_NUMBER: DECIMAL_NUMBER -> type(DECIMAL_NUMBER);
FUNC_CALL_VARIABLE_REFERENCE: '$' -> type(VARIABLE_REFERENCE), pushMode(Identifier);
FUNC_CALL_WRAPPED_SCRIPT_CALL: '$(' -> type(WRAPPED_SCRIPT_CALL), pushMode(IdentifierEnd), pushMode(Identifier);
FUNC_CALL_NULL: NULL -> type(NULL);
FUNC_CALL_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), pushMode(QuotedStringMode);
FUNC_CALL_TRUE: TRUE -> type(TRUE);
FUNC_CALL_FALSE: FALSE -> type(FALSE);
FUNC_CALL_START_EXPRESSION: '(' -> type(START_EXPRESSION), pushMode(ExpressionMode);

FUNC_CALL_IDENTIFIER: NAME_IDENTIFIER -> type(IDENTIFIER), pushMode(IdentifierEnd);

FUNC_CALL_PARAM_SEPARATOR: OPT_INLINE_WHITESPACE ',' OPT_INLINE_WHITESPACE;

END_FUNC_WITH_SCOPE: ')' OPT_INLINE_WHITESPACE '{' -> popMode;
END_FUNC_CALL: ')' -> popMode;

mode ScriptCall;

SCRIPT_CALL_INTEGER: INTEGER -> type(INTEGER);
SCRIPT_CALL_DECIMAL_NUMBER: DECIMAL_NUMBER -> type(DECIMAL_NUMBER);
SCRIPT_CALL_VARIABLE_REFERENCE: '$' -> type(VARIABLE_REFERENCE), pushMode(Identifier);
SCRIPT_CALL_NULL: NULL -> type(NULL);
SCRIPT_CALL_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), pushMode(QuotedStringMode);
SCRIPT_CALL_TRUE: TRUE -> type(TRUE);
SCRIPT_CALL_FALSE: FALSE -> type(FALSE);
SCRIPT_CALL_START_EXPRESSION: '(' -> type(START_EXPRESSION), pushMode(ExpressionMode);

SCRIPT_CALL_IDENTIFIER: NAME_IDENTIFIER -> type(IDENTIFIER), pushMode(IdentifierEnd);
SCRIPT_CALL_RECURSIVE: '$(' -> type(WRAPPED_SCRIPT_CALL), pushMode(IdentifierEnd), pushMode(Identifier);

SCRIPT_CALL_PARAM_SEPARATOR: INLINE_WHITESPACE_CHARACTERS+;
SCRIPT_CALL_FLAG: '--' -> pushMode(Identifier);
SCRIPT_CALL_FLAG_GROUP: '-' -> pushMode(Identifier);

END_SCRIPT_CALL: (NEW_LINE | ')' | EOF) -> popMode;

mode FunctionDeclaration;

START_FUNCTION_DECLARATION: '(';
END_FN_DECL_STUB: ');' -> popMode;
END_FN_DECL_BODY: ')' INLINE_WHITESPACE_CHARACTERS* '{' NEW_LINE -> popMode;
//END_FN_DECL: ')';

FN_DECL_IDENTIFIER: NAME_IDENTIFIER -> type(IDENTIFIER);//, pushMode(IdentifierEnd);
FN_DECL_PARAM_SEPARATOR: ',';

FN_DECL_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;

mode ExpressionMode;

RECURSIVE_EXPRESSION: '(' -> type(START_EXPRESSION), pushMode(ExpressionMode);
END_EXPRESSION: ')' -> popMode;

EXPR_PLUS: '+';
EXPR_MINUS: '-';
EXPR_DIVIDE: '/';
EXPR_MULTIPLY: '*';

EXPR_INTEGER: INTEGER -> type(INTEGER);
EXPR_DECIMAL_NUMBER: DECIMAL_NUMBER -> type(DECIMAL_NUMBER);
EXPR_VARIABLE_REFERENCE: '$' -> type(VARIABLE_REFERENCE), pushMode(Identifier);
EXPR_WRAPPED_SCRIPT_CALL: '$(' -> type(WRAPPED_SCRIPT_CALL), pushMode(IdentifierEnd), pushMode(Identifier);
EXPR_NULL: NULL -> type(NULL);
EXPR_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), pushMode(QuotedStringMode);
EXPR_TRUE: TRUE -> type(TRUE);
EXPR_FALSE: FALSE -> type(FALSE);

EXPR_IDENTIFIER: NAME_IDENTIFIER -> type(IDENTIFIER), pushMode(IdentifierEnd);

EXPRESSION_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;