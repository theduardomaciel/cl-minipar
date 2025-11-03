package parser.ast;

/**
 * Par chave:valor para dicionário
 */
public class DictEntry {
    public ASTNode key;
    public ASTNode value;

    public DictEntry(ASTNode key, ASTNode value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key.toString() + ": " + value.toString();
    }
}