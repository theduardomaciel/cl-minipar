package parser.ast;

import java.util.List;

/**
 * Envio via canal: canal.send(...)
 */
public class SendStmt extends ASTNode {
    public ASTNode channel;
    public List<ASTNode> arguments;

    public SendStmt(ASTNode channel, List<ASTNode> arguments) {
        this.channel = channel;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(channel.toString()).append(".send(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(arguments.get(i).toString());
        }
        sb.append(");");
        return sb.toString();
    }
}