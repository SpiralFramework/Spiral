parser grammar OpenSpiralParser;
import OSLWordScriptParser, LibParser;
options { tokenVocab=OpenSpiralLexer; }

headerDeclaration: HEADER_DECLARATION;

script: headerDeclaration ((lineSeparator scriptLine)+ | lineSeparator)? lineSeparator?;
lineSeparator: SEMICOLON_SEPARATOR | NL_SEPARATOR;

scriptLine: (basicDrill | basicDrillNamed | complexDrills | metaVariableAssignment | actionDeclaration);

metaVariableAssignment: ASSIGN_VARIABLE_NAME VARIABLE_ASSIGNMENT variableValue;

basicDrill: INTEGER BASIC_DRILL_SEPARATOR (basicDrillValue (VALUE_SEPARATOR basicDrillValue)*?)?;
basicDrillNamed: NAME_IDENTIFIER BASIC_DRILL_SEPARATOR (basicDrillValue (VALUE_SEPARATOR basicDrillValue)*?)?;

quotedString
    : BEGIN_QUOTED_STRING
        (ESCAPES | STRING_CHARACTERS | QUOTED_STRING_VARIABLE_REFERENCE | longColourReference | QUOTED_COLOUR_CODE)*
      END_QUOTED_STRING
    ;

localisedString
    : BEGIN_LOCALE_STRING (localisedComponent (LOCALE_NAME_SEPARATOR localisedComponent)*)? END_LOCALE_STRING
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
    ;

actionDeclaration
    : BEGIN_ACTION
        (ACTION_ESCAPES | ACTION_CHARACTERS | ACTION_VARIABLE_REFERENCE)*
      END_ACTION
    ;

// This is for complex drills

complexDrills
    : dialogueDrill
    ;

dialogueDrill
    : (VARIABLE_REFERENCE | NAME_IDENTIFIER) DIALOGUE_SEPARATOR variableValue
    ;