package parser.ast;

/**
 * Parâmetro de método/função
 */
public class Parameter {
    public String name;
    public String type;

    /**
     * Construtor do nó Parameter.
     * 
     * @param name Nome do parâmetro.
     * @param type Tipo do parâmetro.
     */
    public Parameter(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Retorna uma representação em string do parâmetro.
     * 
     * @return String representando o parâmetro.
     */
    @Override
    public String toString() {
        return type + " " + name;
    }
}