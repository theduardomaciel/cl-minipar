package parser;

import java.util.List;

/**
 * Classes para representar a Árvore Sintática Abstrata (AST)
 * Tema 2 - Compiladores 2025.1
 */

/**
 * Interface Visitor para implementar diferentes tipos de processamento da AST
 */
interface ASTVisitor<T> {
    T visit(ASTNode node, int depth);
}

/**
 * Classe base para todos os nós da AST
 */
public abstract class ASTNode {
    public abstract String toString();
    public abstract <T> T accept(ASTVisitor<T> visitor, int depth);
}

/**
 * Programa completo
 */
class Program extends ASTNode {
    public List<ASTNode> statements;

    public Program(List<ASTNode> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

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

/**
 * Declaração de classe
 */
class ClassDecl extends ASTNode {
    public String name;
    public String superClass;
    public List<VarDecl> attributes;
    public List<MethodDecl> methods;

    public ClassDecl(String name, String superClass, List<VarDecl> attributes, List<MethodDecl> methods) {
        this.name = name;
        this.superClass = superClass;
        this.attributes = attributes;
        this.methods = methods;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClassDecl(name=").append(name);
        if (superClass != null) {
            sb.append(", extends=").append(superClass);
        }
        sb.append(", attributes=").append(attributes);
        sb.append(", methods=").append(methods);
        sb.append(")");
        return sb.toString();
    }
}

/**
 * Declaração de método
 */
class MethodDecl extends ASTNode {
    public String returnType;
    public String name;
    public List<Parameter> parameters;
    public List<ASTNode> body;

    public MethodDecl(String returnType, String name, List<Parameter> parameters, List<ASTNode> body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "MethodDecl(name=" + name + ", returnType=" + returnType +
               ", params=" + parameters + ", body=" + body + ")";
    }
}

/**
 * Parâmetro de método/função
 */
class Parameter {
    public String name;
    public String type;

    public Parameter(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return name + ":" + type;
    }
}

/**
 * Declaração de variável
 */
class VarDecl extends ASTNode {
    public String name;
    public String type;
    public ASTNode initializer;

    public VarDecl(String name, String type, ASTNode initializer) {
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "VarDecl(name=" + name + ", type=" + type +
               (initializer != null ? ", init=" + initializer : "") + ")";
    }
}

/**
 * Declaração de função
 */
class FuncDecl extends ASTNode {
    public String name;
    public String returnType;
    public List<Parameter> parameters;
    public List<ASTNode> body;

    public FuncDecl(String name, String returnType, List<Parameter> parameters, List<ASTNode> body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "FuncDecl(name=" + name + ", returnType=" + returnType +
               ", params=" + parameters + ")";
    }
}

/**
 * Instanciação de objeto
 */
class NewInstance extends ASTNode {
    public String className;
    public List<ASTNode> arguments;

    public NewInstance(String className, List<ASTNode> arguments) {
        this.className = className;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "NewInstance(class=" + className + ", args=" + arguments + ")";
    }
}

/**
 * Chamada de método
 */
class MethodCall extends ASTNode {
    public ASTNode object;
    public String methodName;
    public List<ASTNode> arguments;

    public MethodCall(ASTNode object, String methodName, List<ASTNode> arguments) {
        this.object = object;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "MethodCall(object=" + object + ", method=" + methodName + ", args=" + arguments + ")";
    }
}

/**
 * Atribuição
 */
class Assignment extends ASTNode {
    public String varName;
    public ASTNode value;

    public Assignment(String varName, ASTNode value) {
        this.varName = varName;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "Assignment(" + varName + " = " + value + ")";
    }
}

/**
 * If statement
 */
class IfStmt extends ASTNode {
    public ASTNode condition;
    public List<ASTNode> thenBranch;
    public List<ASTNode> elseBranch;

    public IfStmt(ASTNode condition, List<ASTNode> thenBranch, List<ASTNode> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "If(condition=" + condition + ", then=" + thenBranch +
               (elseBranch != null ? ", else=" + elseBranch : "") + ")";
    }
}

/**
 * While statement
 */
class WhileStmt extends ASTNode {
    public ASTNode condition;
    public List<ASTNode> body;

    public WhileStmt(ASTNode condition, List<ASTNode> body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "While(condition=" + condition + ", body=" + body + ")";
    }
}

/**
 * Return statement
 */
class ReturnStmt extends ASTNode {
    public ASTNode value;

    public ReturnStmt(ASTNode value) {
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "Return(" + value + ")";
    }
}

/**
 * Expressões binárias
 */
class BinaryExpr extends ASTNode {
    public ASTNode left;
    public String operator;
    public ASTNode right;

    public BinaryExpr(ASTNode left, String operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "BinaryExpr(" + left + " " + operator + " " + right + ")";
    }
}

/**
 * Expressões unárias
 */
class UnaryExpr extends ASTNode {
    public String operator;
    public ASTNode operand;

    public UnaryExpr(String operator, ASTNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "UnaryExpr(" + operator + operand + ")";
    }
}

/**
 * Literal (número, string, boolean)
 */
class Literal extends ASTNode {
    public Object value;

    public Literal(Object value) {
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "Literal(" + value + ")";
    }
}

/**
 * Identificador (variável)
 */
class Identifier extends ASTNode {
    public String name;

    public Identifier(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "Identifier(" + name + ")";
    }
}

/**
 * Chamada de função
 */
class FunctionCall extends ASTNode {
    public String functionName;
    public List<ASTNode> arguments;

    public FunctionCall(String functionName, List<ASTNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "FunctionCall(" + functionName + ", args=" + arguments + ")";
    }
}

/**
 * Bloco SEQ
 */
class SeqBlock extends ASTNode {
    public List<ASTNode> statements;

    public SeqBlock(List<ASTNode> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "SEQ" + statements;
    }
}

/**
 * Bloco PAR
 */
class ParBlock extends ASTNode {
    public List<ASTNode> statements;

    public ParBlock(List<ASTNode> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "PAR" + statements;
    }
}

/**
 * Break statement
 */
class BreakStmt extends ASTNode {
    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "Break";
    }
}

/**
 * Continue statement
 */
class ContinueStmt extends ASTNode {
    @Override
    public <T> T accept(ASTVisitor<T> visitor, int depth) {
        return visitor.visit(this, depth);
    }

    @Override
    public String toString() {
        return "Continue";
    }
}