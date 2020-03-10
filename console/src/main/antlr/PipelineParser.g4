parser grammar PipelineParser;
options { tokenVocab=PipelineLexer; }

lineSeparator: SEMICOLON_SEPARATOR | NL_SEPARATOR+;

scope: scriptLine (lineSeparator scriptLine)*? lineSeparator*;

scriptLine: (assignVariable | functionCall | scriptCall | functionDeclaration);

quotedString: BEGIN_QUOTED_STRING quotedStringContent;

quotedStringContent
    : (variableReference | ESCAPES | STRING_CHARACTERS | QUOTED_STRING_LINE_BREAK | QUOTED_STRING_LINE_BREAK_NO_SPACE | STRING_WHITESPACE)*
      END_QUOTED_STRING
    ;

variableReference: VARIABLE_REFERENCE (variableName=IDENTIFIER);

assignVariable: ASSIGN_VARIABLE (variableName=IDENTIFIER) VARIABLE_ASSIGNMENT variableValue;

functionCall: IDENTIFIER BEGIN_FUNCTION_CALL functionCallParameters END_FUNC_CALL;

functionCallParameters: (functionParameter (FUNC_CALL_PARAM_SEPARATOR functionParameter)*?)?;

functionParameter: (IDENTIFIER SET_PARAMETER)? functionVariableValue;

functionVariableValue
    : variableValue
    ;
    
scriptCall: IDENTIFIER BEGIN_SCRIPT_CALL scriptCallParameters END_SCRIPT_CALL;

scriptCallParameters: (scriptParameter (SCRIPT_CALL_PARAM_SEPARATOR scriptParameter)*?)?;

scriptParameter: (IDENTIFIER SET_PARAMETER)? scriptVariableValue;

scriptVariableValue
    : variableValue
    ;

functionDeclaration: FUNCTION_DECLARATION IDENTIFIER START_FUNCTION_DECLARATION ((parameters+=IDENTIFIER (FN_DECL_PARAM_SEPARATOR parameters+=IDENTIFIER)*?))? (functionBody | END_FN_DECL_STUB);
functionBody: END_FN_DECL_BODY scope END_SCOPE;

variableValue
    : quotedString
    | trueLiteral
    | falseLiteral
    | integer
    | decimalNumber
    | variableReference
    | nullLiteral
    | functionCall
    | wrappedScriptCall
    ;

trueLiteral: TRUE;
falseLiteral: FALSE;
nullLiteral: NULL;
integer: INTEGER;
decimalNumber: DECIMAL_NUMBER;
wrappedScriptCall: WRAPPED_SCRIPT_CALL scriptCall;