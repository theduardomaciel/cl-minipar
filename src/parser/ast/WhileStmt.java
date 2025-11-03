package parser.ast;

import java.util.List;

/**
 * While statement
 */
public class WhileStmt extends ASTNode {
    public ASTNode condition;
    public List<ASTNode> body;

    /**
     * Construtor do nó WhileStmt.
     * 
     * @param condition Condição do while.
     * @param body      Corpo do while (lista de nós AST).
     */
    public WhileStmt(ASTNode condition, List<ASTNode> body) {
        this.condition = condition;
        this.body = body;
    }

    /**
     * Retorna uma representação em string do nó WhileStmt.
     * 
     * @return String representando o while.
     */
    @Override
    public String toString() {
        return "while (" + condition.toString() + ") { ... }";
    }
}