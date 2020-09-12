parser grammar PipelineParser;
import KnolusParser;
options { tokenVocab=PipelineLexer; }

line: scriptCall | declareVariable | declareFunction | setVariableValue | functionCall | memberFunctionCall;

scriptCall: (scriptName=CALL_SCRIPT) scriptCallParameters END_SCRIPT_CALL;

scriptCallParameters: (scriptParameter (SCRIPT_CALL_PARAM_SEPARATOR scriptParameter)*? SCRIPT_CALL_PARAM_SEPARATOR?)?;

scriptParameter
    : SCRIPT_CALL_FLAG
    | SCRIPT_CALL_FLAG_GROUP
    | variableValue
    ;