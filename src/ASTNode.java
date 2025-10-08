/**
 * Classes para representar a Árvore Sintática Abstrata (AST)
 * Tema 2 - Compiladores 2025.1
 */

import java.util.List;

/**
 * Classe base para todos os nós da AST
 */
abstract class ASTNode {
    public abstract String toString();
}

/**
 * Programa completo
 */
class Program extends ASTNode {
    List<ASTNode> statements;

    public Program(List<ASTNode> statements) {
        this.statements = statements;
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
    String name;
    String superClass;
    List<VarDecl> attributes;
    List<MethodDecl> methods;

    public ClassDecl(String name, String superClass, List<VarDecl> attributes, List<MethodDecl> methods) {
        this.name = name;
        this.superClass = superClass;
        this.attributes = attributes;
        this.methods = methods;
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
    String returnType;
    String name;
    List<Parameter> parameters;
    List<ASTNode> body;

    public MethodDecl(String returnType, String name, List<Parameter> parameters, List<ASTNode> body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
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
    String name;
    String type;

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
    String name;
    String type;
    ASTNode initializer;

    public VarDecl(String name, String type, ASTNode initializer) {
        this.name = name;
        this.type = type;
        this.initializer = initializer;
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
    String name;
    String returnType;
    List<Parameter> parameters;
    List<ASTNode> body;

    public FuncDecl(String name, String returnType, List<Parameter> parameters, List<ASTNode> body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
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
    String className;
    List<ASTNode> arguments;

    public NewInstance(String className, List<ASTNode> arguments) {
        this.className = className;
        this.arguments = arguments;
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
    ASTNode object;
    String methodName;
    List<ASTNode> arguments;

    public MethodCall(ASTNode object, String methodName, List<ASTNode> arguments) {
        this.object = object;
        this.methodName = methodName;
        this.arguments = arguments;
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
    String varName;
    ASTNode value;

    public Assignment(String varName, ASTNode value) {
        this.varName = varName;
        this.value = value;
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
    ASTNode condition;
    List<ASTNode> thenBranch;
    List<ASTNode> elseBranch;

    public IfStmt(ASTNode condition, List<ASTNode> thenBranch, List<ASTNode> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
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
    ASTNode condition;
    List<ASTNode> body;

    public WhileStmt(ASTNode condition, List<ASTNode> body) {
        this.condition = condition;
        this.body = body;
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
    ASTNode value;

    public ReturnStmt(ASTNode value) {
        this.value = value;
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
    ASTNode left;
    String operator;
    ASTNode right;

    public BinaryExpr(ASTNode left, String operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
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
    String operator;
    ASTNode operand;

    public UnaryExpr(String operator, ASTNode operand) {
        this.operator = operator;
        this.operand = operand;
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
    Object value;

    public Literal(Object value) {
        this.value = value;
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
    String name;

    public Identifier(String name) {
        this.name = name;
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
    String functionName;
    List<ASTNode> arguments;

    public FunctionCall(String functionName, List<ASTNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
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
    List<ASTNode> statements;

    public SeqBlock(List<ASTNode> statements) {
        this.statements = statements;
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
    List<ASTNode> statements;

    public ParBlock(List<ASTNode> statements) {
        this.statements = statements;
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
    public String toString() {
        return "Break";
    }
}

/**
 * Continue statement
 */
class ContinueStmt extends ASTNode {
    @Override
    public String toString() {
        return "Continue";
    }
}

