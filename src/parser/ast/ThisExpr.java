package parser.ast;

/**
 * Referência ao objeto atual (this)
 */
public class ThisExpr extends ASTNode {
    public ThisExpr() {
    }

    @Override
    public String toString() {
        return "this";
    }
}