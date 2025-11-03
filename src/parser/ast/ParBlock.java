package parser.ast;

import java.util.List;

/**
 * Bloco PAR
 */
public class ParBlock extends ASTNode {
    public List<ASTNode> statements;

    /**
     * Construtor do nó ParBlock.
     * 
     * @param statements Lista de instruções do bloco paralelo.
     */
    public ParBlock(List<ASTNode> statements) {
        this.statements = statements;
    }

    /**
     * Retorna uma representação em string do nó ParBlock.
     * 
     * @return String representando o bloco PAR.
     */
    @Override
    public String toString() {
        return "par { ... }";
    }
}