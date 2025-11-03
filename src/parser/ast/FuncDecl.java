package parser.ast;

import java.util.List;

/**
 * Declaração de função
 */
public class FuncDecl extends ASTNode {
    public String name;
    public String returnType;
    public List<Parameter> parameters;
    public List<ASTNode> body;

    /**
     * Construtor do nó FuncDecl.
     * 
     * @param name       Nome da função.
     * @param returnType Tipo de retorno da função.
     * @param parameters Lista de parâmetros da função.
     * @param body       Corpo da função (lista de nós AST).
     */
    public FuncDecl(String name, String returnType, List<Parameter> parameters, List<ASTNode> body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
    }

    /**
     * Retorna uma representação em string do nó FuncDecl.
     * 
     * @return String representando a declaração de função.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("func ").append(name).append("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(parameters.get(i).toString());
        }
        sb.append(") { ... }");
        return sb.toString();
    }
}