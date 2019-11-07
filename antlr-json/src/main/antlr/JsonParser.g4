parser grammar JsonParser;

options { tokenVocab=JsonLexer; }

file: object | array;

object: OPEN_OBJECT (pair (VALUE_SEPARATOR pair)*)? CLOSE_OBJECT;
array: OPEN_ARRAY (value (VALUE_SEPARATOR value)*)? CLOSE_ARRAY;
pair: string PAIR_SEPARATOR value;

value
    : string
    | number
    | object
    | array
    | booleanRule
    | nullRule
    ;

string
    : BEGIN_STRING
        (ESCAPES | STRING_CHARACTERS)*
      END_STRING
    ;

number: NUMBER;
booleanRule: TRUE | FALSE;
nullRule: NULL;