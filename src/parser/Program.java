package parser;

import java.util.List;

/**
 * Programa completo
 */
public class Program extends ASTNode {
    public List<ASTNode> statements;

    /**
     * Construtor do nó Program.
     * @param statements Lista de nós AST representando as instruções do programa.
     */
    public Program(List<ASTNode> statements) {
        this.statements = statements;
    }

    /**
     * Retorna uma representação em string do nó Program.
     * @return String representando o programa.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Program(\n");
        for (ASTNode stmt : statements) {
            sb.append("  ").append(stmt.toString()).append("\n");
        }
        sb.append(")");
        return sb.toString();
    }
}

