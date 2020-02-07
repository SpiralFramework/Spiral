import {ErrorNode, ParseTree, ParseTreeVisitor, RuleNode, TerminalNode} from "antlr4ts/tree";

export class BaseVisitor<T> implements ParseTreeVisitor<T> {
    visit(tree: ParseTree): any {
        return tree.accept(this);
    }

    visitChildren(node: RuleNode): any {
        let result: T = this.defaultResult();
        const n = node.childCount;
        for (let i = 0; i < n; i++) {
            if (!this.shouldVisitNextChild(node, result)) {
                break
            }

            const c = node.getChild(i);
            const childResult = c.accept(this);
            result = this.aggregateResult(result, childResult);
        }
        return result;
    }

    visitErrorNode(node: ErrorNode): any {
        return this.defaultResult();
    }

    visitTerminal(node: TerminalNode): any {
        return this.defaultResult();
    }

    defaultResult(): T {
        return null;
    }

    aggregateResult(aggregate: T, nextResult: T): T {
        return nextResult;
    }

    shouldVisitNextChild(node: RuleNode, currentResult: T): boolean {
        return true;
    }
}