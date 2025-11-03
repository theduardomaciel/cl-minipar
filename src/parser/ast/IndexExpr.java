package parser.ast;

/**
 * Indexação: alvo[índice]
 */
public class IndexExpr extends ASTNode {
    public ASTNode target;
    public ASTNode index;

    public IndexExpr(ASTNode target, ASTNode index) {
        this.target = target;
        this.index = index;
    }

    @Override
    public String toString() {
        return target.toString() + "[" + index.toString() + "]";
    }
}