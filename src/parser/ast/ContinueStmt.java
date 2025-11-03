package parser.ast;

/**
 * Continue statement
 */
public class ContinueStmt extends ASTNode {
    /**
     * Retorna uma representação em string do nó ContinueStmt.
     * 
     * @return String representando o continue.
     */
    @Override
    public String toString() {
        return "continue;";
    }
}