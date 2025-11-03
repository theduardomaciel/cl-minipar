package parser.ast;

/**
 * Literal (número, string, boolean)
 */
public class Literal extends ASTNode {
    public Object value;

    /**
     * Construtor do nó Literal.
     * 
     * @param value Valor literal (número, string, boolean).
     */
    public Literal(Object value) {
        this.value = value;
    }

    /**
     * Retorna uma representação em string do nó Literal.
     * 
     * @return String representando o valor literal.
     */
    @Override
    public String toString() {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }
}