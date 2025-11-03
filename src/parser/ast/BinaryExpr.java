package parser.ast;

/**
 * Expressões binárias
 */
public class BinaryExpr extends ASTNode {
    public ASTNode left;
    public String operator;
    public ASTNode right;

    /**
     * Construtor do nó BinaryExpr.
     * 
     * @param left     Expressão à esquerda.
     * @param operator Operador binário.
     * @param right    Expressão à direita.
     */
    public BinaryExpr(ASTNode left, String operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Retorna uma representação em string do nó BinaryExpr.
     * 
     * @return String representando a expressão binária.
     */
    @Override
    public String toString() {
        return "(" + left.toString() + " " + operator + " " + right.toString() + ")";
    }
}