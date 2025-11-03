package parser.ast;

/**
 * Return statement
 */
public class ReturnStmt extends ASTNode {
    public ASTNode value;

    /**
     * Construtor do nó ReturnStmt.
     * 
     * @param value Valor de retorno.
     */
    public ReturnStmt(ASTNode value) {
        this.value = value;
    }

    /**
     * Retorna uma representação em string do nó ReturnStmt.
     * 
     * @return String representando o retorno.
     */
    @Override
    public String toString() {
        if (value != null) {
            return "return " + value.toString() + ";";
        } else {
            return "return;";
        }
    }
}