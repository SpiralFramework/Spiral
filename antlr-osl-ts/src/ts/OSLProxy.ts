import {CharStreams, CodePointCharStream, CommonTokenStream} from 'antlr4ts';
import {OpenSpiralLexer} from "../antlr/OpenSpiralLexer";
import {OpenSpiralParser, ScriptContext} from "../antlr/OpenSpiralParser";
import {OpenSpiralParserBaseVisitor} from "./OpenSpiralParserBaseVisitor";
import {OSLVisitor} from "./OSLVisitor";

function parse(text: string) {
    const inputStream = CharStreams.fromString(text);
    const lexer = new OpenSpiralLexer(inputStream);
    const tokenStream = new CommonTokenStream(lexer);
    const parser = new OpenSpiralParser(tokenStream);
    parser.buildParseTree = true;
    const tree = parser.script();
    const visitor = new OSLVisitor();
    return visitor.visit(tree);
}

// @ts-ignore
window["parse"] = parse;