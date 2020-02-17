parser grammar OpenSpiralParser;
import OSLWordScriptParser, LibParser;
options { tokenVocab=OpenSpiralLexer; }

headerDeclaration: HEADER_DECLARATION;

script: headerDeclaration ((lineSeparator scriptLine)+ | lineSeparator)? lineSeparator?;
lineSeparator: SEMICOLON_SEPARATOR | NL_SEPARATOR;

scriptLine: (basicDrill | basicDrillNamed | complexDrills | metaVariableAssignment | actionDeclaration | functionCall);

metaVariableAssignment: ASSIGN_VARIABLE_NAME VARIABLE_ASSIGNMENT variableValue;

basicDrill: INTEGER BASIC_DRILL_SEPARATOR (basicDrillValue (VALUE_SEPARATOR basicDrillValue)*?)?;
basicDrillNamed: NAME_IDENTIFIER BASIC_DRILL_SEPARATOR (basicDrillValue (VALUE_SEPARATOR basicDrillValue)*?)?;

quotedString: BEGIN_QUOTED_STRING quotedStringContent;

quotedStringContent
    : (ESCAPES | STRING_CHARACTERS | QUOTED_STRING_VARIABLE_REFERENCE | longColourReference | QUOTED_STRING_LINE_BREAK | QUOTED_STRING_LINE_BREAK_NO_SPACE | QUOTED_COLOUR_CODE | STRING_WHITESPACE)*
      END_QUOTED_STRING
    ;

localisedString: BEGIN_LOCALE_STRING localisedStringContent;

localisedStringContent
    : (localisedComponent (LOCALE_NAME_SEPARATOR localisedComponent)*)? END_LOCALE_STRING
    ;

localisedComponent
    : LOCALE_NAME_IDENTIFIER
    | LOCALE_VARIABLE_REFERENCE
    ;

longColourReference: BEGIN_LONG_QUOTED_COLOUR_CODE longReference;

basicDrillValue
    : wrdLabelReference
    | wrdParameterReference
    | variableValue
    ;

variableValue
    : localisedString
    | quotedString
    | booleanRule
    | INTEGER
    | DECIMAL_NUMBER
    | VARIABLE_REFERENCE
    | NULL
    | functionCall
    ;

actionDeclaration
    : BEGIN_ACTION
        (ACTION_ESCAPES | ACTION_CHARACTERS | ACTION_VARIABLE_REFERENCE)*
      END_ACTION
    ;

functionCall
    : NAME_IDENTIFIER BEGIN_FUNC_CALL
        (functionParameter (FUNC_CALL_PARAM_SEPARATOR functionParameter)*?)?
      END_FUNC_CALL
    ;

functionParameter: FUNC_CALL_PARAMETER_NAME? functionVariableValue;

functionVariableValue
    : (FUNC_CALL_BEGIN_LOCALE_STRING localisedStringContent)
    | (FUNC_CALL_BEGIN_QUOTED_STRING quotedStringContent)
    | funcBooleanRule
    | FUNC_CALL_INTEGER
    | FUNC_CALL_DECIMAL_NUMBER
    | FUNC_CALL_VARIABLE_REFERENCE
    | FUNC_CALL_NULL
    ;

// This is for complex drills

complexDrills
    : dialogueDrill
    ;

dialogueDrill
    : (VARIABLE_REFERENCE | NAME_IDENTIFIER) DIALOGUE_SEPARATOR variableValue
    ;