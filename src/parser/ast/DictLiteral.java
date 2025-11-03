package parser.ast;

import java.util.List;

/**
 * Literal de dicionário: { key: value, ... }
 */
public class DictLiteral extends ASTNode {
    public List<DictEntry> entries;

    public DictLiteral(List<DictEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(entries.get(i).toString());
        }
        sb.append("}");
        return sb.toString();
    }
}