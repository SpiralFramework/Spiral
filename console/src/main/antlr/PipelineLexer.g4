lexer grammar PipelineLexer;
import KnolusLexer;

SINGLE_LINE_COMMENT: '//' ~[\n]+ NEW_LINE* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' NEW_LINE* -> skip ; // .*? matches anything until the first */

CALL_STRING_SCRIPT: STRING_SCRIPT_LIST INLINE_WHITESPACE_CHARACTERS+ -> type(CALL_SCRIPT), pushMode(StringParamMode);
CALL_SCRIPT: SCRIPT_LIST INLINE_WHITESPACE_CHARACTERS+ -> pushMode(ScriptCall);

BEGIN_FUNCTION_CALL: IDENTIFIER_START IDENTIFIER_END* '(' -> pushMode(FunctionCall);
BEGIN_MEMBER_REFERENCE: IDENTIFIER_START IDENTIFIER_END* '.';

GLOBAL_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);

fragment NEW_LINE: '\r'? '\n';
fragment STRING_SCRIPT_LIST
    : H E L P ((SWS) W I T H)?
    | I D E N T I F Y
    ;
fragment SCRIPT_LIST
    : E X T R A C T (SWS) (F I L E S | M O D E L S | T E X T U R E S)
    | E X T R A C T (SWS) F I L E S (SWS) W I Z A R D
    | S H O W (SWS) E N V (I R O N M E N T)?
    | E X I T | Q U I T
    ;

SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;

fragment SWS: INLINE_WHITESPACE_CHARACTERS*;

mode ScriptCall;

SCRIPT_CALL_INTEGER: INTEGER -> type(INTEGER);
SCRIPT_CALL_DECIMAL_NUMBER: DECIMAL_NUMBER -> type(DECIMAL_NUMBER);

SCRIPT_CALL_NULL: NULL -> type(NULL);
SCRIPT_CALL_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), pushMode(QuotedStringMode);
SCRIPT_CALL_TRUE: TRUE -> type(TRUE);
SCRIPT_CALL_FALSE: FALSE -> type(FALSE);
SCRIPT_CALL_START_EXPRESSION: '(' -> type(BEGIN_EXPRESSION), pushMode(ExpressionMode);

SCRIPT_CALL_RECURSIVE: '$(' -> pushMode(DEFAULT_MODE);

SCRIPT_CALL_PARAM_SEPARATOR: INLINE_WHITESPACE_CHARACTERS+;

SCRIPT_CALL_FUNCTION_CALL: IDENTIFIER_START IDENTIFIER_END* '(' -> type(BEGIN_FUNCTION_CALL), pushMode(FunctionCall);
SCRIPT_CALL_VARIABLE_REFERENCE: '$' IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);

SCRIPT_CALL_FLAG: '--' IDENTIFIER_START IDENTIFIER_END*;
SCRIPT_CALL_FLAG_GROUP: '-' IDENTIFIER_START IDENTIFIER_END*;

END_SCRIPT_CALL: (NEW_LINE | ')' | EOF) -> popMode;

mode StringParamMode;

END_STRING_CALL: (NL_SEPARATOR | NEW_LINE | ')' | EOF) -> type(END_SCRIPT_CALL), popMode;

//Matches a double quote, pushes to the Quoted String Mode, then pops back to the default mode
STRING_PARAM_COMMAND_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), popMode, pushMode(QuotedStringMode);
//Matches an open parenthesis, pushes to the expression mode, then pops back to the default mode
STRING_PARAM_COMMAND_START_EXPRESSION: '(' -> type(BEGIN_EXPRESSION), popMode, pushMode(ExpressionMode);

//Identifier has to go LAST!!!
//Matches an identifier, then an open parenthesis; pushes to the function call mode, then pops back to the default mode
STRING_PARAM_COMMAND_FUNCTION_CALL: IDENTIFIER_START IDENTIFIER_END* '(' -> type(BEGIN_FUNCTION_CALL), popMode, pushMode(FunctionCall);
//Matches an identifier, then a single dot; this doesn't change modes, since we will need to follow it up with a function call or a variable reference
STRING_PARAM_COMMAND_MEMBER_REFERENCE: IDENTIFIER_START IDENTIFIER_END* '.' -> type(BEGIN_MEMBER_REFERENCE);
//Matches a dollar sign, then an identifier, then pops back to the default mode
STRING_PARAM_COMMAND_VARIABLE_REFERENCE: '$' IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER), popMode;

// The following three lexer rules are for matching 'plain strings'; strings outside of quotes.
// Most strings *should* work, but they a) don't support interpolation, and b) form one string for the whole contents

//Matches a backspace, then either the letter u followed by 4 hex digits, or a character from the ESCAPE_CHARACTERS fragment listed below
STRING_PARAM_COMMAND_ESCAPES: '\\' ('u' HEX_DIGITS HEX_DIGITS HEX_DIGITS HEX_DIGITS | STRING_PARAM_COMMAND_ESCAPE_CHARACTERS) -> type(CHARACTER_ESCAPES);
//Matches any character that isn't a single quote, double quote, dollar sign, open parenthesis, open bracket, or backslash
//NOTE: It is ***very*** important that this is a ***single*** match, and not a multi-match via * or the likes.
// By only matching one character at a time, we ensure that when the conditions for the next parameter are met, we break and switch
STRING_PARAM_COMMAND_PLAIN_STRING: ~['"$(\\[] -> type(PLAIN_CHARACTERS);
//A list of escape characters
fragment STRING_PARAM_COMMAND_ESCAPE_CHARACTERS: ['"\\/bfnrt$([];
//If you want whitespace to be skipped, uncomment this. Note: this *will* screw with plain strings
//STRING_PARAM_COMMAND_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;