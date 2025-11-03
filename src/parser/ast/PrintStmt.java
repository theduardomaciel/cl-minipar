package parser.ast;

import java.util.List;

/**
 * Comando de impressão: print(arg1, arg2, ...)
 */
public class PrintStmt extends ASTNode {
    public List<ASTNode> arguments;
    public boolean newline;

    public PrintStmt(List<ASTNode> arguments, boolean newline) {
        this.arguments = arguments;
        this.newline = newline;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("print(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(arguments.get(i).toString());
        }
        sb.append(")");
        if (!newline)
            sb.append("!");
        sb.append(";");
        return sb.toString();
    }
}