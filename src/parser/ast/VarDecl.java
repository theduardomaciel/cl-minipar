package parser.ast;

/**
 * Declaração de variável
 */
public class VarDecl extends ASTNode {
    public String name;
    public String type;
    public ASTNode initializer;

    /**
     * Construtor do nó VarDecl.
     * 
     * @param name        Nome da variável.
     * @param type        Tipo da variável.
     * @param initializer Inicializador da variável (pode ser null).
     */
    public VarDecl(String name, String type, ASTNode initializer) {
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    /**
     * Retorna uma representação em string do nó VarDecl.
     * 
     * @return String representando a declaração de variável.
     */
    @Override
    public String toString() {
        String s = type + " " + name;
        if (initializer != null) {
            s += " = " + initializer.toString();
        }
        s += ";";
        return s;
    }
}