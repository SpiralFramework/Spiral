parser grammar LibParser;
options { tokenVocab=LibLexer; }

booleanRule: TRUE | FALSE;
funcBooleanRule: FUNC_TRUE | FUNC_FALSE;

longReference: (LONG_REF_ESCAPES | LONG_REF_CHARACTERS | LONG_REF_VARIABLE_REFERENCE)* END_LONG_REFERENCE;