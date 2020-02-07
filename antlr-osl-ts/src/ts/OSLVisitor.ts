import {ParseTree, TerminalNode} from "antlr4ts/tree";
import {OpenSpiralParserBaseVisitor} from "./OpenSpiralParserBaseVisitor";
import {
    BasicDrillContext,
    DialogueDrillContext, HeaderDeclarationContext,
    OpenSpiralParser,
    QuotedStringContext,
    VariableValueContext
} from "../antlr/OpenSpiralParser";
import "./Extensions";
import {isBlank, pushString, pushVariableInt16, toIntVariable} from "./Extensions";

class OSLLabel {
    readonly label: string;

    constructor(label: string) {
        this.label = label;
    }
}
class OSLLongLabel {
    readonly label: string;

    constructor(label: string) {
        this.label = label;
    }
}
class OSLParameter {
    readonly parameter: string;

    constructor(parameter: string) {
        this.parameter = parameter;
    }
}
class OSLLongParameter {
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

class OSLNoOp {
    private static _instance: OSLNoOp;

    private constructor() {
    }

    public static get Instance(): OSLNoOp {
        // Do you need arguments? Make it a regular static method instead.
        if (this._instance === undefined)
            this._instance = new this();
        return this._instance;
    }
}

type OSLUnion = number | boolean | undefined | null | OSLNoOp | OSLLabel | OSLRawString | OSLParameter;

export function represent(value: OSLUnion): string {
    if (typeof value === "number") return value.toString();
    else if (typeof value === "boolean") return value.toString();
    else if (value === undefined) return "undefined";
    else if (value === null) return "null";
    else if (value instanceof OSLLabel) return value.label;
}

export class OSLVisitor extends OpenSpiralParserBaseVisitor<OSLUnion> {
    readonly output = [0x4F, 0x53, 0x4C, 0x49];

    setVersion(major: number = 0, minor: number = 0, patch: number = 0) {
        this.output.push(0x10);
        this.output.push(major);
        this.output.push(minor);
        this.output.push(patch);
    }

    addOpcode(opcode: number, values: OSLUnion[]) {
        if (values.every(union => typeof union === "number")) {
            return this.addSimpleOpcode(opcode, values as number[])
        } else {
            const output = this.output;
            output.push(0x71);
            output.push(opcode);
            output.push(values.length);

            values.forEach(function (arg: OSLUnion) {
                if (typeof arg === "number") {
                    output.push(0x6F);
                    pushVariableInt16(output, arg);
                } else if (typeof arg === "boolean") {
                    output.push(0x6E);
                    output.push(arg ? 1 : 0);
                } else if (arg instanceof OSLLabel) {
                    output.push(0x60);
                    pushString(output, arg.label);
                } else if (arg instanceof OSLParameter) {
                    output.push(0x61);
                    pushString(output, arg.parameter);
                }
            })
        }
    }

    addSimpleOpcode(opcode: number, values: number[]) {

    }

    visitHeaderDeclaration(ctx: HeaderDeclarationContext): OSLUnion {
        const rawSemanticVersion = ctx.HEADER_DECLARATION().text.substr(10);
        if (isBlank(rawSemanticVersion)) {
            return OSLNoOp.Instance;
        }

        const components = rawSemanticVersion.substr(1)
            .split(".")
            .map(str => parseInt(str.trim()));

        this.setVersion(components[0], components[1], components[2]);

        return OSLNoOp.Instance;
    }

    visitBasicDrill(ctx: BasicDrillContext): OSLUnion {
        const opcode = toIntVariable(ctx.INTEGER().text);
        const values = ctx.basicDrillValue();
        this.addOpcode(opcode, values.map(ctx => this.visitBasicDrillValue(ctx)));

        return OSLNoOp.Instance;
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