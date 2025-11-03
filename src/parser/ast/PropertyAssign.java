package parser.ast;

/**
 * Atribuição a propriedade: alvo.propriedade = valor
 */
public class PropertyAssign extends ASTNode {
    public ASTNode object;
    public String propertyName;
    public ASTNode value;

    public PropertyAssign(ASTNode object, String propertyName, ASTNode value) {
        this.object = object;
        this.propertyName = propertyName;
        this.value = value;
    }

    @Override
    public String toString() {
        return object.toString() + "." + propertyName + " = " + value.toString() + ";";
    }
}