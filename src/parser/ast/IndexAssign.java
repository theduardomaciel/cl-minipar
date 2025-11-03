package parser.ast;

/**
 * Atribuição em índice: alvo[índice] = valor
 */
public class IndexAssign extends ASTNode {
    public ASTNode target;
    public ASTNode index;
    public ASTNode value;

    public IndexAssign(ASTNode target, ASTNode index, ASTNode value) {
        this.target = target;
        this.index = index;
        this.value = value;
    }

    @Override
    public String toString() {
        return target.toString() + "[" + index.toString() + "] = " + value.toString() + ";";
    }
}