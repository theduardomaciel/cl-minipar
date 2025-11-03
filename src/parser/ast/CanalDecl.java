package parser.ast;

import java.util.List;

/**
 * Declaração de canal
 */
public class CanalDecl extends ASTNode {
    public List<String> nomes;

    public CanalDecl(List<String> nomes) {
        this.nomes = nomes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("c_channel ");
        for (int i = 0; i < nomes.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(nomes.get(i));
        }
        sb.append(";");
        return sb.toString();
    }
}