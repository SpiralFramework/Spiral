import {ParseTree, TerminalNode} from "antlr4ts/tree";
import {OpenSpiralParserBaseVisitor} from "./OpenSpiralParserBaseVisitor";
import {
    DialogueDrillContext,
    OpenSpiralParser,
    QuotedStringContext,
    VariableValueContext
} from "../antlr/OpenSpiralParser";

class OSLLabel {
    readonly label: string;

    constructor(label: string) {
        this.label = label;
    }
}
class OSLRawString {
    readonly str: string;
    constructor(str: string) {
        this.str = str;
    }
}

type OSLUnion = number | boolean | undefined | null | OSLLabel | OSLRawString;

export function represent(value: OSLUnion): string {
    if (typeof value === "number") return value.toString();
    else if (typeof value === "boolean") return value.toString();
    else if (value === undefined) return "undefined";
    else if (value === null) return "null";
    else if (value instanceof OSLLabel) return value.label;
}

export class OSLVisitor extends OpenSpiralParserBaseVisitor<OSLUnion> {
    visitDialogueDrill(ctx: DialogueDrillContext): OSLUnion {
        return new OSLLabel(`${ctx.NAME_IDENTIFIER()?.text}: ${this.visitVariableValue(ctx.variableValue())}`);
    }

    visitVariableValue(ctx: VariableValueContext): OSLUnion {
        if (ctx.DECIMAL_NUMBER() != undefined) {
            return parseFloat(ctx.DECIMAL_NUMBER().text);
        } else if (ctx.INTEGER() != undefined) {
            return parseInt(ctx.INTEGER().text);
        } else if (ctx.VARIABLE_REFERENCE() != undefined) {
            return null;
        } else if (ctx.booleanRule() != undefined) {
            return null;
        } else if (ctx.NULL() != undefined) {
            return null;
        } else if (ctx.quotedString() != undefined) {
            return this.visitQuotedString(ctx.quotedString());
        } else {
            return null;
        }
    }

    visitQuotedString(ctx: QuotedStringContext): OSLUnion {
        let string = "";
        let cltOpen = false;

        ctx.children.filter((node: ParseTree) => node instanceof TerminalNode)
            .forEach((node: TerminalNode) => {
                if (node.symbol.type == OpenSpiralParser.ESCAPES) {
                    if (node.text[1] == 'b') {
                        string += '\b';
                    } else if (node.text[1] == 'f') {
                        string += '\f';
                    } else if (node.text[1] == 'n') {
                        string += '\n';
                    } else if (node.text[1] == 'r') {
                        string += '\r';
                    } else if (node.text[1] == 't') {
                        string += '\t';
                    } else if (node.text[1] == 'u') {
                        string += String.fromCharCode(parseInt(node.text.substr(2), 16));
                    }
                } else if (node.symbol.type == OpenSpiralParser.STRING_CHARACTERS) {
                    string += node.text;
                } else if (node.symbol.type == OpenSpiralParser.QUOTED_STRING_VARIABLE_REFERENCE) {
                    // string += getData(node.text.substr(1)).represent();
                } else if (node.symbol.type == OpenSpiralParser.QUOTED_COLOUR_CODE) {
                    //TODO: Figure out how to do colour codes
                }
            });

        if (cltOpen) {
            //TODO: Figure out how to do colour codes
        }

        return new OSLRawString(string);
    }
}