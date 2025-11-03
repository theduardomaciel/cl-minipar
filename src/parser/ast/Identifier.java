package parser.ast;

/**
 * Identificador (variável)
 */
public class Identifier extends ASTNode {
    public String name;

    /**
     * Construtor do nó Identifier.
     * 
     * @param name Nome do identificador.
     */
    public Identifier(String name) {
        this.name = name;
    }

    /**
     * Retorna uma representação em string do nó Identifier.
     * 
     * @return String representando o identificador.
     */
    @Override
    public String toString() {
        return name;
    }
}