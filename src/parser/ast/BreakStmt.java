package parser.ast;

/**
 * Break statement
 */
public class BreakStmt extends ASTNode {
    /**
     * Retorna uma representação em string do nó BreakStmt.
     * 
     * @return String representando o break.
     */
    @Override
    public String toString() {
        return "break;";
    }
}