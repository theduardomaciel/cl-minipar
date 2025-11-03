package parser.ast;

import java.util.List;

/**
 * Literal de lista: [expr, expr, ...]
 */
public class ListLiteral extends ASTNode {
    public List<ASTNode> elements;

    public ListLiteral(List<ASTNode> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(elements.get(i).toString());
        }
        sb.append("]");
        return sb.toString();
    }
}