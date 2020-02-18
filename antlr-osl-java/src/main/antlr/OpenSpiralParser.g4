parser grammar OpenSpiralParser;
import OSLWordScriptParser, LibParser;
options { tokenVocab=OpenSpiralLexer; }

headerDeclaration: HEADER_DECLARATION;

script: headerDeclaration scope;
lineSeparator: SEMICOLON_SEPARATOR | NL_SEPARATOR;

scope: ((lineSeparator scriptLine)+ | lineSeparator)? lineSeparator?;

scriptLine: (basicDrill | basicDrillNamed | dialogueDrill | metaVariableAssignment | actionDeclaration | functionCall | ifCheck) INLINE_WHITESPACE?;

metaVariableAssignment: ASSIGN_VARIABLE_NAME VARIABLE_ASSIGNMENT variableValue;

basicDrill: INTEGER BASIC_DRILL_SEPARATOR (basicDrillValue (VALUE_SEPARATOR basicDrillValue)*?)?;
basicDrillNamed: NAME_IDENTIFIER BASIC_DRILL_SEPARATOR (basicDrillValue (VALUE_SEPARATOR basicDrillValue)*?)?;

funcBooleanRule: FUNC_TRUE | FUNC_FALSE;
ifCheckBooleanRule: IF_CHECK_TRUE | IF_CHECK_FALSE;

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

functionCall: NAME_IDENTIFIER BEGIN_FUNC_CALL functionCallParameters;
recursiveFuncCall: FUNC_CALL_NAME_IDENTIFIER FUNC_CALL_RECURSIVE functionCallParameters;
ifCheckFuncCall: IF_CHECK_NAME_IDENTIFIER IF_CHECK_FUNC_CALL functionCallParameters;
functionCallParameters: (functionParameter (FUNC_CALL_PARAM_SEPARATOR functionParameter)*?)? END_FUNC_CALL;

functionParameter: FUNC_CALL_PARAMETER_NAME? functionVariableValue;

functionVariableValue
    : (FUNC_CALL_BEGIN_LOCALE_STRING localisedStringContent)
    | (FUNC_CALL_BEGIN_QUOTED_STRING quotedStringContent)
    | funcBooleanRule
    | FUNC_CALL_INTEGER
    | FUNC_CALL_DECIMAL_NUMBER
    | FUNC_CALL_VARIABLE_REFERENCE
    | FUNC_CALL_NULL
    | recursiveFuncCall
    ;

dialogueDrill
    : (VARIABLE_REFERENCE | NAME_IDENTIFIER) DIALOGUE_SEPARATOR variableValue
    ;

ifCheck
    : IF_CHECK
        (ifCheckValue (IF_CHECK_EQUALITY) ifCheckValue)
      END_IF_CHECK

      scope

      CLOSE_SCOPE
    ;


ifCheckValue
    : (IF_CHECK_BEGIN_LOCALE_STRING localisedStringContent)
    | (IF_CHECK_BEGIN_QUOTED_STRING quotedStringContent)
    | ifCheckBooleanRule
    | IF_CHECK_INTEGER
    | IF_CHECK_DECIMAL_NUMBER
    | IF_CHECK_VARIABLE_REFERENCE
    | IF_CHECK_NULL
    | ifCheckFuncCall
    ;