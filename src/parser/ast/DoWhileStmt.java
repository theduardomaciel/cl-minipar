package parser.ast;

import java.util.List;

/**
 * Do-While statement
 */
public class DoWhileStmt extends ASTNode {
    public List<ASTNode> body;
    public ASTNode condition;

    public DoWhileStmt(List<ASTNode> body, ASTNode condition) {
        this.body = body;
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "do { ... } while (" + condition.toString() + ");";
    }
}