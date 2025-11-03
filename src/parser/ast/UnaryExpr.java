package parser.ast;

/**
 * Expressões unárias
 */
public class UnaryExpr extends ASTNode {
    public String operator;
    public ASTNode operand;

    /**
     * Construtor do nó UnaryExpr.
     * 
     * @param operator Operador unário.
     * @param operand  Operando da expressão.
     */
    public UnaryExpr(String operator, ASTNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * Retorna uma representação em string do nó UnaryExpr.
     * 
     * @return String representando a expressão unária.
     */
    @Override
    public String toString() {
        return operator + operand.toString();
    }
}