parser grammar PipelineParser;
options { tokenVocab=PipelineLexer; }

lineSeparator: SEMICOLON_SEPARATOR | NL_SEPARATOR+ | EOF;

file: scope EOF;

scope: scriptLine*?;

scriptLine
    : scriptCall
    | (assignVariable | functionCall | functionDeclaration) lineSeparator
    ;

quotedString
    : BEGIN_QUOTED_STRING
        (variableReference | ESCAPES | STRING_CHARACTERS | QUOTED_STRING_LINE_BREAK | QUOTED_STRING_LINE_BREAK_NO_SPACE | STRING_WHITESPACE)*
      END_QUOTED_STRING
    ;

variableReference: VARIABLE_REFERENCE (variableName=IDENTIFIER);

assignVariable: GLOBAL? ASSIGN_VARIABLE (variableName=IDENTIFIER) VARIABLE_ASSIGNMENT variableValue;

functionCall: (functionName=IDENTIFIER) BEGIN_FUNCTION_CALL functionCallParameters END_FUNC_CALL;

functionCallParameters: (functionParameter (FUNC_CALL_PARAM_SEPARATOR functionParameter)*?)?;

functionParameter: (parameterName=IDENTIFIER SET_PARAMETER)? functionVariableValue;

functionVariableValue
    : variableValue
    ;
    
scriptCall: (scriptName=IDENTIFIER) ((BEGIN_SCRIPT_CALL scriptCallParameters END_SCRIPT_CALL) | (BEGIN_SCRIPT_CALL_EMPTY));

scriptCallParameters: (scriptParameter (SCRIPT_CALL_PARAM_SEPARATOR scriptParameter)*?)?;

scriptParameter
    : scriptFlag
    | scriptFlagGroup
    | (parameterName=IDENTIFIER SET_PARAMETER)? scriptVariableValue
    ;

scriptFlag: SCRIPT_CALL_FLAG IDENTIFIER;
scriptFlagGroup: SCRIPT_CALL_FLAG_GROUP IDENTIFIER;

scriptVariableValue
    : variableValue
    ;

functionDeclaration: GLOBAL? FUNCTION_DECLARATION (functionName=IDENTIFIER) START_FUNCTION_DECLARATION ((parameters+=IDENTIFIER (FN_DECL_PARAM_SEPARATOR parameters+=IDENTIFIER)*?))? (functionBody | END_FN_DECL_STUB);
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
    | expression
    ;

trueLiteral: TRUE;
falseLiteral: FALSE;
nullLiteral: NULL;
integer: INTEGER;
decimalNumber: DECIMAL_NUMBER;
wrappedScriptCall: WRAPPED_SCRIPT_CALL scriptCall;
expression
    : START_EXPRESSION
        (startingValue=variableValue) ((exprOps+=expressionOperation) (exprVals+=variableValue))+?
      END_EXPRESSION
    ;

expressionOperation
    : EXPR_PLUS
    | EXPR_MINUS
    | EXPR_DIVIDE
    | EXPR_MULTIPLY
    ;