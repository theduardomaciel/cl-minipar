package parser.ast;

import java.util.List;

/**
 * Bloco SEQ
 */
public class SeqBlock extends ASTNode {
    public List<ASTNode> statements;

    /**
     * Construtor do nó SeqBlock.
     * 
     * @param statements Lista de instruções do bloco sequencial.
     */
    public SeqBlock(List<ASTNode> statements) {
        this.statements = statements;
    }

    /**
     * Retorna uma representação em string do nó SeqBlock.
     * 
     * @return String representando o bloco SEQ.
     */
    @Override
    public String toString() {
        return "seq { ... }";
    }
}