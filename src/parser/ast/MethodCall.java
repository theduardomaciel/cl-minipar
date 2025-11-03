package parser.ast;

import java.util.List;

/**
 * Chamada de método
 */
public class MethodCall extends ASTNode {
    public ASTNode object;
    public String methodName;
    public List<ASTNode> arguments;

    /**
     * Construtor do nó MethodCall.
     * 
     * @param object     Objeto alvo da chamada de método.
     * @param methodName Nome do método.
     * @param arguments  Lista de argumentos do método.
     */
    public MethodCall(ASTNode object, String methodName, List<ASTNode> arguments) {
        this.object = object;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    /**
     * Retorna uma representação em string do nó MethodCall.
     * 
     * @return String representando a chamada de método.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(object.toString()).append(".").append(methodName).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(arguments.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }
}