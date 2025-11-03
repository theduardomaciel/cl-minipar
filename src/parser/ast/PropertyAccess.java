package parser.ast;

/**
 * Acesso a propriedade: alvo.propriedade
 */
public class PropertyAccess extends ASTNode {
    public ASTNode object;
    public String propertyName;

    public PropertyAccess(ASTNode object, String propertyName) {
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public String toString() {
        return object.toString() + "." + propertyName;
    }
}