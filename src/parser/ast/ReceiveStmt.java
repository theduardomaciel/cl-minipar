package parser.ast;

import java.util.List;

/**
 * Recepção via canal: canal.receive(...)
 */
public class ReceiveStmt extends ASTNode {
    public ASTNode channel;
    public List<ASTNode> arguments;

    public ReceiveStmt(ASTNode channel, List<ASTNode> arguments) {
        this.channel = channel;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "Receive(channel=" + channel + ", args=" + arguments + ")";
    }
}