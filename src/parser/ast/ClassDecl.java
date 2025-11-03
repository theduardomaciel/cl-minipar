package parser.ast;

import java.util.List;

/**
 * Declaração de classe
 */
public class ClassDecl extends ASTNode {
    public String name;
    public String superClass;
    public List<VarDecl> attributes;
    public List<MethodDecl> methods;

    /**
     * Construtor do nó ClassDecl.
     * 
     * @param name       Nome da classe.
     * @param superClass Nome da superclasse (pode ser null).
     * @param attributes Lista de atributos da classe.
     * @param methods    Lista de métodos da classe.
     */
    public ClassDecl(String name, String superClass, List<VarDecl> attributes, List<MethodDecl> methods) {
        this.name = name;
        this.superClass = superClass;
        this.attributes = attributes;
        this.methods = methods;
    }

    /**
     * Retorna uma representação em string do nó ClassDecl.
     * 
     * @return String representando a declaração de classe.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ").append(name);
        if (superClass != null) {
            sb.append(" extends ").append(superClass);
        }
        sb.append(" {");
        for (VarDecl attr : attributes) {
            sb.append(" ").append(attr.toString());
        }
        for (MethodDecl meth : methods) {
            sb.append(" ").append(meth.toString());
        }
        sb.append(" }");
        return sb.toString();
    }
}