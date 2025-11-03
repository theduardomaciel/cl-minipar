package parser.ast;

/**
 * Expressão de leitura de linha: readln()
 */
public class ReadlnExpr extends ASTNode {
    public ReadlnExpr() {
    }

    @Override
    public String toString() {
        return "readln()";
    }
}