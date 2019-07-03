lexer grammar JsonLexer;
import JsonStringLexer;

OPEN_OBJECT: '{';
CLOSE_OBJECT: '}';

OPEN_ARRAY: '[';
CLOSE_ARRAY: ']';

VALUE_SEPARATOR: ',';
PAIR_SEPARATOR: ':';

TRUE: T R U E;
FALSE: F A L S E;
NULL: N U L L;

NUMBER: ('0' | ([1-9] DIGIT*)) ('.' DIGIT)? ([eE] [+\-]? DIGIT+)?;
fragment DIGIT: [0-9];

WS  :   [ \t\n\r]+ -> skip ;

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