parser grammar OSLWordScriptParser;
import LibParser;
options { tokenVocab=OSLWordScriptLexer; }

wrdLabelReference: WRD_SHORT_LABEL_REFERENCE | wrdLongLabelReference;
wrdParameterReference: WRD_SHORT_PARAMETER_REFERENCE | wrdLongParameterReference;

wrdLongLabelReference
    : WRD_START_LONG_LABEL_REFERENCE longReference;

wrdLongParameterReference
    : WRD_START_LONG_PARAMETER_REFERENCE longReference;
    