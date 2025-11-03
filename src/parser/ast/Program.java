package parser.ast;

import java.util.List;

/**
 * Programa (raiz da AST)
 */
public class Program extends ASTNode {
    public final List<ASTNode> statements;

    public Program(List<ASTNode> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "Program";
    }
}