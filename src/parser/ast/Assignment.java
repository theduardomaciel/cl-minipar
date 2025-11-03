package parser.ast;

/**
 * Atribuição
 */
public class Assignment extends ASTNode {
    public String varName;
    public ASTNode value;

    /**
     * Construtor do nó Assignment.
     * 
     * @param varName Nome da variável.
     * @param value   Valor a ser atribuído.
     */
    public Assignment(String varName, ASTNode value) {
        this.varName = varName;
        this.value = value;
    }

    /**
     * Retorna uma representação em string do nó Assignment.
     * 
     * @return String representando a atribuição.
     */
    @Override
    public String toString() {
        return varName + " = " + value.toString() + ";";
    }
}