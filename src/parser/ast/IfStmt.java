package parser.ast;

import java.util.List;

/**
 * If statement
 */
public class IfStmt extends ASTNode {
    public ASTNode condition;
    public List<ASTNode> thenBranch;
    public List<ASTNode> elseBranch;

    /**
     * Construtor do nó IfStmt.
     * 
     * @param condition  Condição do if.
     * @param thenBranch Lista de nós AST do bloco then.
     * @param elseBranch Lista de nós AST do bloco else (pode ser null).
     */
    public IfStmt(ASTNode condition, List<ASTNode> thenBranch, List<ASTNode> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    /**
     * Retorna uma representação em string do nó IfStmt.
     * 
     * @return String representando o if.
     */
    @Override
    public String toString() {
        String s = "if (" + condition.toString() + ") { ... }";
        if (elseBranch != null) {
            s += " else { ... }";
        }
        return s;
    }
}