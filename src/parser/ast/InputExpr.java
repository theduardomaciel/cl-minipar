package parser.ast;

/**
 * Expressão de entrada: input([prompt])
 */
public class InputExpr extends ASTNode {
    public ASTNode prompt; // pode ser null

    public InputExpr(ASTNode prompt) {
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        if (prompt != null) {
            return "input(" + prompt.toString() + ")";
        } else {
            return "input()";
        }
    }
}