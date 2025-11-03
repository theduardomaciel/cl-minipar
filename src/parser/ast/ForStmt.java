package parser.ast;

import java.util.List;

/**
 * For-in statement
 */
public class ForStmt extends ASTNode {
    public VarDecl variable; // variável de iteração (nome e tipo)
    public ASTNode iterable; // expressão iterável
    public List<ASTNode> body;

    public ForStmt(VarDecl variable, ASTNode iterable, List<ASTNode> body) {
        this.variable = variable;
        this.iterable = iterable;
        this.body = body;
    }

    @Override
    public String toString() {
        return "for (" + variable.toString() + " in " + iterable.toString() + ") { ... }";
    }
}