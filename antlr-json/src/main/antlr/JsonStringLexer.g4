lexer grammar JsonStringLexer;

BEGIN_STRING: '"' -> pushMode(StringMode);

mode StringMode;

ESCAPES: '\\' ('u' HEX HEX HEX HEX | ESCAPE_CHARACTERS);
STRING_CHARACTERS: ~["\\]+;

fragment ESCAPE_CHARACTERS: ["\\/bfnrt];
fragment HEX: [0-9a-zA-Z];

END_STRING: '"' -> popMode;