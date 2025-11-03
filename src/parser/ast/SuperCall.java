package parser.ast;

import java.util.List;

/**
 * Chamada explícita ao construtor da superclasse: super(args)
 */
public class SuperCall extends ASTNode {
    public List<ASTNode> arguments;

    public SuperCall(List<ASTNode> arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("super(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(arguments.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }
}