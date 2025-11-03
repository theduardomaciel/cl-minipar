package parser.ast;

import java.util.List;

/**
 * Declaração de método
 */
public class MethodDecl extends ASTNode {
    public String returnType;
    public String name;
    public List<Parameter> parameters;
    public List<ASTNode> body;

    /**
     * Construtor do nó MethodDecl.
     * 
     * @param returnType Tipo de retorno do método.
     * @param name       Nome do método.
     * @param parameters Lista de parâmetros do método.
     * @param body       Corpo do método (lista de nós AST).
     */
    public MethodDecl(String returnType, String name, List<Parameter> parameters, List<ASTNode> body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    /**
     * Retorna uma representação em string do nó MethodDecl.
     * 
     * @return String representando a declaração de método.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType).append(" ").append(name).append("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(parameters.get(i).toString());
        }
        sb.append(") { ... }");
        return sb.toString();
    }
}