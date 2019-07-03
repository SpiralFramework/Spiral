parser grammar OpenSpiralParser;
import OSLWordScriptParser, LibParser;
options { tokenVocab=OpenSpiralLexer; }

script: HEADER_DECLARATION ((lineSeparator scriptLine)+ | lineSeparator)?;
lineSeparator: SEMICOLON_SEPARATOR | NL_SEPARATOR;

scriptLine: (basicDrill | basicDrillNamed | metaVariableAssignment);

metaVariableAssignment: ASSIGN_VARIABLE_NAME VARIABLE_ASSIGNMENT variableValue;

basicDrill: BASIC_DRILL_CODE (basicDrillValue (VALUE_SEPARATOR basicDrillValue)*?)?;
basicDrillNamed: BASIC_DRILL_NAME (basicDrillValue (VALUE_SEPARATOR basicDrillValue)*?)?;

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
    | INTEGER
    | DECIMAL_NUMBER
    | VARIABLE_REFERENCE
    | BOOLEAN
    | NULL
    ;