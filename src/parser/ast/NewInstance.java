package parser.ast;

import java.util.List;

/**
 * Instanciação de objeto
 */
public class NewInstance extends ASTNode {
    public String className;
    public List<ASTNode> arguments;

    /**
     * Construtor do nó NewInstance.
     * 
     * @param className Nome da classe a ser instanciada.
     * @param arguments Lista de argumentos para o construtor.
     */
    public NewInstance(String className, List<ASTNode> arguments) {
        this.className = className;
        this.arguments = arguments;
    }

    /**
     * Retorna uma representação em string do nó NewInstance.
     * 
     * @return String representando a instanciação de objeto.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(className).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(arguments.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }
}