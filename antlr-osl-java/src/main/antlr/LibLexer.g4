lexer grammar LibLexer;

INTEGER: ('0b' BINARY_DIGITS+) | ('0o' OCTAL_DIGITS+) | ('0x' HEX_DIGITS+) | '0d'? DECIMAL_DIGITS+;
DECIMAL_NUMBER: ('0' | ([1-9] DECIMAL_DIGITS*)) ('.' DECIMAL_DIGITS+)? ([eE] [+\-]? DECIMAL_DIGITS+)?;

TRUE: T R U E;
FALSE: F A L S E;
NULL: N U L L;

BEGIN_LOCALE_STRING: '"locale.' -> pushMode(LocaleStringMode);
BEGIN_QUOTED_STRING: '"' -> pushMode(QuotedStringMode);
BEGIN_ACTION: '[' -> pushMode(ActionMode);

fragment BINARY_DIGITS: [0-1];
fragment OCTAL_DIGITS: [0-7];
fragment DECIMAL_DIGITS: [0-9];
fragment HEX_DIGITS: [0-9a-fA-F];

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

fragment INLINE_WHITESPACE_CHARACTERS: [ \t];
fragment VARIABLE_NAME_IDENTIFIER: [a-zA-Z0-9_]+;

mode QuotedStringMode;

ESCAPES: '\\' ('u' HEX_DIGITS HEX_DIGITS HEX_DIGITS HEX_DIGITS | ESCAPE_CHARACTERS);
STRING_CHARACTERS: ~["\\$&]+;
QUOTED_STRING_VARIABLE_REFERENCE: '$' VARIABLE_NAME_IDENTIFIER;
BEGIN_LONG_QUOTED_COLOUR_CODE: '&{' -> pushMode(LongReferenceMode);
QUOTED_COLOUR_CODE: '&' COLOUR_CODE_CHARACTERS+ INLINE_WHITESPACE_CHARACTERS?;

fragment COLOUR_CODE_CHARACTERS: [a-zA-Z0-9_];
fragment ESCAPE_CHARACTERS: ["\\/bfnrt$&];

END_QUOTED_STRING: '"' -> popMode;

mode LocaleStringMode;

LOCALE_NAME_SEPARATOR: '.';
LOCALE_NAME_IDENTIFIER: VARIABLE_NAME_IDENTIFIER;
LOCALE_VARIABLE_REFERENCE: QUOTED_STRING_VARIABLE_REFERENCE;

END_LOCALE_STRING: '"' -> popMode;

mode LongReferenceMode;

LONG_REF_ESCAPES: '\\' ('u' HEX_DIGITS HEX_DIGITS HEX_DIGITS HEX_DIGITS | LONG_REF_ESCAPE_CHARACTERS);
LONG_REF_CHARACTERS: ~[\\$}]+;
LONG_REF_VARIABLE_REFERENCE: '$' [a-zA-Z0-9_]+;

fragment LONG_REF_ESCAPE_CHARACTERS: [\\/bfnrt$}];

END_LONG_REFERENCE: '}' -> popMode;

mode ActionMode;

ACTION_ESCAPES: '\\' ('u' HEX_DIGITS HEX_DIGITS HEX_DIGITS HEX_DIGITS | ACTION_ESCAPE_CHARACTERS);
ACTION_CHARACTERS: ~[\\${\]]+;
ACTION_VARIABLE_REFERENCE: '$' [a-zA-Z0-9_]+;

fragment ACTION_ESCAPE_CHARACTERS: [\\/bfnrt$}];

END_ACTION: ']' -> popMode;

//mode ActionParameterMode;
//
