package parser.ast;

import java.util.List;

/**
 * Chamada de função
 */
public class FunctionCall extends ASTNode {
    public String functionName;
    public List<ASTNode> arguments;

    /**
     * Construtor do nó FunctionCall.
     * 
     * @param functionName Nome da função.
     * @param arguments    Lista de argumentos da função.
     */
    public FunctionCall(String functionName, List<ASTNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    /**
     * Retorna uma representação em string do nó FunctionCall.
     * 
     * @return String representando a chamada de função.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(functionName).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(arguments.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }
}